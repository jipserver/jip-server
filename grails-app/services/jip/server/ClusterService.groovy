package jip.server

import jip.grid.Grid
import jip.grid.JipGrid
import jip.grid.commands.CommandRunner
import jip.grid.local.LocalGrid
import jip.grid.slurm.SlurmGrid
import jip.utils.KeyUtils
import jip.grid.JipGrids

class ClusterService {
    /**
     * Path to the jip client package
     */
    private static final JIP_CLIENT = "/jip-client.zip"
    /**
     * We handle transactions manually
     */
    static transactional = false
    /**
     * Access the grails configuration to get the jip home
     */
    def grailsApplication
    /**
     * Local grids
     */
    def localGridService
    /**
     * Access jip configuration
     */
    def jipService

    /**
     * Initialize the cluster, check the connection using the default user
     * and upload all necessary binaries
     *
     * @param cluster the cluster
     * @throws RuntimeException in case the cluster could not be initialized
     */
    void initializeCuster(Cluster cluster) {
        if(cluster == null) throw new NullPointerException("NULL cluster not permitted")
        if(cluster.state == "Provisioning") return
        if(cluster.provisioned) return

        log.info("Initializing ${cluster.name}")
        cluster.state = "Provisioning"
        cluster.save(flush: true)

        try{
            // get the connection url
            def url = grailsApplication.config.grails.serverURL
            if(cluster.serverUrl){
                url = cluster.serverUrl
            }
            provisionCluster(cluster, url)
            log.info("Provisioned ${cluster.name}")
            cluster.state = "Online"
            cluster.provisioned = true
        }catch (Exception e){
            cluster.provisioned = false
            cluster.state = "Offline"
            if(e.getCause() != null && e.getCause() instanceof java.net.SocketTimeoutException){
                log.error("Unable to connection to cluster ${cluster.name} : ${e.cause.message}")
            }else{
                log.error("Error while initializing ${cluster.name} : ${e.cause?.message}", e)
            }
        }
        cluster.save(flush: true)
    }

    private void provisionCluster(Cluster cluster, String url){
        log.info("Provisioning ${cluster.name}")
        CommandRunner runner = cluster.createRunner()
        // create the home structure
        if(runner.run("mkdir -p ${cluster.home}/jip-environment", null, null).waitFor() != 0){
            throw new RuntimeException("Creating jip-environment dir in ${cluster.home} failed !")
        }
        if(runner.run("mkdir -p ${cluster.home}/jobscripts && chmod 0777 ${cluster.home}/jobscripts", null, null).waitFor() != 0){
            throw new RuntimeException("Creating jobscripts dir in ${cluster.home} failed !")
        }
        if(runner.run("mkdir -p ${cluster.home}/states && chmod 0777 ${cluster.home}/jobscripts", null, null).waitFor() != 0){
            throw new RuntimeException("Creating jobscripts dir in ${cluster.home} failed !")
        }
        // copy files
        log.info("PROVISION ${cluster.name}: Copy client")
        runner.copy(JipGrids.copyToDisk(JIP_CLIENT), "${cluster.home}/jip-environment/jip-client.zip")
        log.info("PROVISION ${cluster.name}: Extract client")
        def unzipProcess = runner.run("unzip -o jip-client.zip", "${cluster.home}/jip-environment", [:])
        if(unzipProcess.waitFor() != 0){
            throw new RuntimeException(
                    "Unable to unzip JIP client distribution: \n Error Stream:\n ${unzipProcess.errorStream.text}\n\n Output stream : ${unzipProcess.inputStream.text}" )
        }
        log.info("PROVISION ${cluster.name}: Bootstrap client")
        def bootstrapper = runner.run("sh bootstrap.sh ${cluster.name} ${url}", "${cluster.home}/jip-environment", [:])
        BufferedReader stream = new BufferedReader(new InputStreamReader(bootstrapper.inputStream))
        String line = null
        while((line = stream.readLine()) != null){
            log.info("PROVISION ${cluster.name} BOOTSTRAP: ${line}")
        }
        stream.close()
        if(bootstrapper.waitFor() != 0){
            throw new RuntimeException(
                    "Unable to bootstrap JIP client distribution: \n Error Stream:\n ${bootstrapper.errorStream.text}\n\n Output stream : ${bootstrapper.inputStream.text}" )
        }
    }

    /**
     * Create a grid implementation based on the cluster type
     *
     * @param cluster the cluster
     * @return grid the grid
     */
    Grid createGrid(Cluster cluster){
        Grid baseGrid = null
        switch (cluster.clusterType){
            case LocalGrid.TYPE: baseGrid =  localGridService.get(cluster.name); break;
            case SlurmGrid.TYPE: baseGrid =  new SlurmGrid([:]); break;
        }
        if(baseGrid == null)
            throw new RuntimeException("No grid implementation found for cluster type ${cluster.clusterType}")
        return baseGrid
    }

    /**
     * Create public/private key pair and and add the public
     * key to the set of authorized keys for that user
     *
     * @param cluster the cluster
     * @param user the user
     */
    void authorizeUser(Cluster cluster, ClusterUser user){
        String publicKey = null

        def keyDir = jipService.keys()
        // check if we have public key file
        File publicKeyFile = new File(keyDir, "${user.id}.pub")
        File privateKeyFile = new File(keyDir, "${user.id}.pem")

        if(publicKeyFile.exists()){
            publicKey = publicKeyFile.readLines().join("\n")
        }else{
            log.info("Generating keys for ${user.name}")
            def keys = KeyUtils.generate()
            // write private key
            privateKeyFile.write(keys['private'])
            if("chmod 0600 ${privateKeyFile.absolutePath}".execute().waitFor() != 0){
                throw new RuntimeException("Unable change permissions for key file ${privateKeyFile.absolutePath}")
            }
            // write public key
            publicKeyFile.write(keys['public'])
            if("chmod 0600 ${publicKeyFile.absolutePath}".execute().waitFor() != 0){
                throw new RuntimeException("Unable change permissions for key file ${publicKeyFile.absolutePath}")
            }
            publicKey = keys['public']
        }


        // write the public key to authorized hosts
        def runner = cluster.createRunner(user)
        if(runner.run("grep \"${publicKey}\" ~/.ssh/authorizedKeys", null, null) != 0){
            // ensure we have .ssh folder
            if(runner.run("mkdir -p ~/.ssh", null, null).waitFor() != 0){
                throw new RuntimeException("Unable to create .ssh directory for ${user.name}@${cluster.host}")
            }
            // append the public key
            if(runner.run("echo \"${publicKey}\" > ~/.ssh/authorized_keys", null, null).waitFor() != 0){
                throw new RuntimeException("Unable to add public key for ${user.name}@${cluster.host}")
            }
            // set permissions
            if(runner.run("chmod 0600 ~/.ssh/authorized_keys", null, null).waitFor() != 0){
                throw new RuntimeException("Unable change permissions for ${user.name}@${cluster.host}")
            }
        }


        // update user
        ClusterUser.withTransaction {
            user.password = null
            user.keyfile = privateKeyFile.absolutePath
            user.save()
        }
    }

}
