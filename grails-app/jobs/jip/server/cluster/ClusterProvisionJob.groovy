package jip.server.cluster

import jip.server.Cluster
import jip.server.ClusterService



class ClusterProvisionJob {
    static triggers = {
        cron name:"timedTrigger", startDelay: 60000l, cronExpression: "0 0 * * * ?"
    }

    ClusterService clusterService

    def execute(context) {
        if(context == null || !context.mergedJobDataMap.containsKey("cluster")){
            def unprovisionedCluster = Cluster.findByProvisioned(false)
            for (Cluster cluster : unprovisionedCluster) {
                if(!cluster.provisioned){
                    log.info("Trigger provisioning for ${cluster.name}")
                    clusterService.initializeCuster(cluster)
                }
            }
        }else{
            Cluster cluster = context.mergedJobDataMap.get("cluster")
            if(!cluster.provisioned){
                log.info("Trigger provisioning for ${cluster.name}")
                clusterService.initializeCuster(cluster)
            }
        }
    }
}
