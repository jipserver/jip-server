package jip.server

import jip.grid.commands.CommandRunner

/**
 * The cluster represents an execution target that can be used to
 * submit jobs to. A cluster is identified uniquely by its name.
 *
 * The cluster must define a home folder that is used to store cluster specific data
 * and a state that describes the current status of the cluster.
 *
 *
 */
class Cluster {
    static enum ConnectionType {
        /**
         * Local connections
         */
        Local,
        /**
         * Remote connection through ssh
         */
        SSH
    }

    static constraints = {
        name(nullable: false, unique: true, blank: false)
        clusterType(nullable:false)
        connectionType(nullable: false)
        host(nullable: true)
        home(nullable: false)
        state(nullable: true)
        serverUrl(nullable: true)
        clusterUser nullable: true
    }

    /**
     * The unique cluster id
     */
    String name

    /**
     * The connection type
     */
    ConnectionType connectionType = ConnectionType.Local

    /**
     * Cluster type, local or some grid engine identifier
     */
    String clusterType = "local"

    /**
     * Optional host name
     */
    String host

    /**
     * Optional connection port
     */
    int port = 22

    /**
     * Home directory for the cluster
     */
    String home

    /**
     * Cluster state
     */
    String state;

    /**
     * Alternative url to the JIP server
     */
    String serverUrl

    /**
     * Mark a cluster as provisioned
     */
    boolean provisioned

    /**
     * The cluster user that is used to provision the remote system and
     * performs default system tasks
     */
    ClusterUser clusterUser

    @Override
    String toString() {
        return "${name} - Host: ${host} Port: ${port} - ${connectionType}/${clusterType} - ${home}"
    }

    /**
     * Set home folder and remove any trailing slashes
     *
     * @param home the home folder
     */
    void setHome(String home){
        if(home != null){
            home = home.replaceAll(/\/*$/, "")
        }
        this.home = home
    }

    /**
     * Get absolute path to the remote jip executable
     *
     * @return jip absolute path to the remote jip executable
     */
    String getJip(){
        return "${home}/jip-environment/bin/jip-wrapper.sh"
    }

    /**
     * Create a command runner for the default cluster user
     *
     * @return runner the command runner
     */
    CommandRunner createRunner(){
        return createRunner(clusterUser)
    }

    /**
     * Create a command runner for a specified user
     *
     * @return runner the command runner
     */
    CommandRunner createRunner(ClusterUser clusterUser){
        if(clusterUser == null) throw new NullPointerException("No user specified, unable to create runner")
        return clusterUser.createRunner(this)
    }
}
