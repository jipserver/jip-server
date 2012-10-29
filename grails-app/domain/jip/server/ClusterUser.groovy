package jip.server

import jip.grid.commands.CommandRunner
import jip.grid.commands.SSHRunner

/**
 * This domain stores information about a cluster/remote user. The
 * domain does not carry information about the host though and can
 * therefore be used for multiple hosts.
 */
class ClusterUser {

    static constraints = {
        name nullable: false, blank:  false
        password nullable: true
        keyfile nullable: true
    }

    /**
     * The remote user name
     */
    String name

    /**
     * The user password
     */
    String password

    /**
     * The private keyfile
     */
    String keyfile

    /**
     * Create a runner that runs commands
     * as the specified user
     *
     * @param cluster the cluster
     * @return runner the runner
     */
    CommandRunner createRunner(Cluster cluster){
        if(cluster == null) throw new NullPointerException()
        String host = cluster.host
        int port = cluster.port > 0 ? cluster.port : 22
        if(!host && cluster.connectionType == Cluster.ConnectionType.Local){
            host = "localhost"
        }else if(host == null){
            throw new RuntimeException("No host name specified, unable to create ssh command runner!")
        }
        log.debug("Creating SSH runner with ${host}:${port} user:${name} passwd:${password != null} keyfile:${keyfile}")
        return new SSHRunner(host, port, name, password, keyfile)
    }

}
