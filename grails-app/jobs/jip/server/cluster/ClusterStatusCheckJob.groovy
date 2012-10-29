package jip.server.cluster

import jip.server.Cluster
import jip.server.ClusterService



class ClusterStatusCheckJob {
    static triggers = {
      simple name:"statusChecker", startDelay: 60000l, repeatInterval: 60000l
    }

    ClusterService clusterService

    def execute() {
        Cluster.findAll().each{Cluster cluster->
            log.debug("Checking status for ${cluster.name}, current state: ${cluster.state}")
            if(cluster.state != "Provisioning"){
                def oldState = cluster.state
                try {
                    def runner = cluster.createRunner()
                    def ls = runner.run("ls ${cluster.home}", null, null)
                    if(ls.waitFor() != 0){
                        log.warn("Cluster ${cluster.name} is not provisioned")
                        cluster.provisioned = false;
                        cluster.state = "Unprovisioned"
                        cluster.save(flush:true)
                        ClusterProvisionJob.triggerNow([cluster: cluster])
                    }else{
                        cluster.state = "Online"
                        cluster.provisioned = true
                        cluster.save(flush: true)
                    }
                } catch (Exception e) {
                    if(oldState != "Offline"){
                        log.warn("Cluster ${cluster.name} is not reachable!")
                    }
                    cluster.state = "Offline"
                    cluster.save(flush: true)
                }

            }
        }
    }
}
