package jip.grid

import jip.grid.commands.CommandRunner

class JipGrids {

    private static final JIP_CLIENT = "/jip-client.zip"
    /**
     * Initialize the Jip grid home using the given home directory and returns
     * a map with the paths relative to the home dir.
     * If home dir is null, $HOME/.jip is used
     *
     * @param runner the runner
     * @param home the home directory
     */
    public static void initialize(CommandRunner runner, String home, String clusterURL, String clusterName){
        // create the home structure
        if(home == null){
            def homeRun = runner.run("echo \$HOME", null, null)
            if(homeRun.waitFor() != 0){
                throw new RuntimeException("Unable to determine home directory !")
            }
            home = homeRun.inputStream.text.trim()
        }
        if(runner.run("mkdir -p ${home}/jip-environment", null, null).waitFor() != 0){
            throw new RuntimeException("Creating jip-environment dir in ${home} failed !")
        }
        // copy files
        runner.copy(copyToDisk(JIP_CLIENT), "${home}/jip-environment/jip-client.zip")
        def unzipProcess = runner.run("unzip -o jip-client.zip", "${home}/jip-environment", [:])
        if(unzipProcess.waitFor() != 0){
            throw new RuntimeException(
                    "Unable to unzip JIP client distribution: \n Error Stream:\n ${unzipProcess.errorStream.text}\n\n Output stream : ${unzipProcess.inputStream.text}" )
        }

        def bootstrapper = runner.run("sh bootstrap.sh ${clusterName} ${clusterURL}", "${home}/jip-environment", [:])
        if(bootstrapper.waitFor() != 0){
            throw new RuntimeException(
                    "Unable to bootstrap JIP client distribution: \n Error Stream:\n ${bootstrapper.errorStream.text}\n\n Output stream : ${bootstrapper.inputStream.text}" )

        }
    }

    static File copyToDisk(String resource){
        def file = File.createTempFile("jip-jar-tmp", "")
        file.deleteOnExit()
        def url = JipGrids.class.getResource(resource)
        if(!url){
            throw new RuntimeException("Resource ${resource} not found!")
        }
        def writer = new BufferedOutputStream(new FileOutputStream(file))
        def reader = new BufferedInputStream(url.openStream())
        byte[] bytes = new byte[4096]
        int read = 0
        while((read = reader.read(bytes)) > 0){
            writer.write(bytes, 0, read)
        }
        reader.close()
        writer.close()
        return file
    }
}
