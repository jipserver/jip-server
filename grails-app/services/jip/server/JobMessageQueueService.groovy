package jip.server

import grails.plugin.jms.Queue

/**
 * JMS Message queues that handle job messages
 */
class JobMessageQueueService {
    /**
     * The jms server
     */
    def jmsService
    /**
     * The clsuter service
     */
    def clusterService
    /**
     * This service is not transactional, we
     * use our own transactions or mongo
     * atomic operations
     */
    static transactional = false
    /**
     * Expose the jms queues
     */
    static exposes = ['jms']
    /**
     * The job service
     */
    def jobService

    /**
     * Resolve input to job
     * @param input wither a job, a long a map with id entry or a string
     * @return job the resolved job or null
     */
    private Job resolveJob(input){
        try {
            if(input instanceof Job){
                return input
            }else if(input instanceof Long){
                return Job.get(input)
            }else if(input instanceof Map && ((Map)input).containsKey("id")){
                return Job.get(input.id)
            }else if(input instanceof String){
                return Job.get(Long.parseLong(input))
            }
        } catch (Exception e) {
            log.error("Unable to get job for input : ${input}")
        }
        return null
    }

    /**
     * Job submission queue takes either a job or
     * a map with an id entry or the id itself that can be used to load a
     * job from the database
     *
     * @param job either a job instance, a job id or a map with a job id
     * @return null returns null in any case
     */
    @Queue(name='job.submit')
    def submitJob(input) {
        log.debug("Preparing job submission")
        Job job = resolveJob(input)
        if(!job){
            log.error("Unable to submit job from input ${input}! No job instance could be resolved")
            return null
        }
        log.info("Submit job ${job.id} to ${job.cluster}")
        def cluster = Cluster.findByName(job.cluster)
        if(!cluster){
            log.error("Cluster ${job.cluster} not found, unable to submit job")
            return null
        }

        User user = User.findById(job.ownerId)
        if(!user){
            log.error("No cluster user ${job.ownerId} found, unable to submit job!")
            return null
        }
        if(cluster.state != "Online" || !cluster.provisioned){
            log.error("Cluster ${cluster.name} is not online or not provisioned! State ${cluster.state}, Provisioned:${cluster.provisioned}")
            return null
        }
        ClusterUser clusterUser = user.getRemoteUser(cluster)
        if(!clusterUser){
            log.error("No remote user associated with this account!")
            job.addMessage(Message.Type.WARNING, "Non remote user associated, unable to submit the job to the remote cluster!")
            return null
        }

        try{
            jobService.submit([job], cluster, clusterUser)
            log.info("Job ${job.id} submitted, cluster id : ${job.clusterId}")
            Job.withTransaction{
                job.state = State.Submitted
                job.submissions++
                job.submitDate = new Date()
                job.save()
            }
        }catch (Exception error){
            log.error("Error while submitting job ${job.id} : ${error.message}", error)
            Job.withTransaction {
                job.addMessage(Message.Type.ERROR, "Submission error : ${error.message}", false)
                job.state = State.Done
                job.finishState = FinishState.Error
                job.finishDate = new Date()
                job.save()
            }
        }
        return null
    }

    @Queue(name='job.cancel')
    def cancelJob(job) {
        Job jobInstance = null
        String message = null
        if(job instanceof Map) {
            jobInstance = Job.get(job.id)
            if(job.containsKey("message")){
                message = job.message
            }
        }else if (job instanceof Job){
            jobInstance = job
        }

        if(jobInstance) {
            if(!jobInstance.state.nextStates.contains(State.Done)){
                log.info("Skipping cancel job ${jobInstance.id} with current state ${jobInstance.state}")
                return null
            }
            log.info("Cancel job ${jobInstance.id} from ${jobInstance.cluster}")
            def cluster = Cluster.findByName(jobInstance.cluster)
            if(!cluster){
                log.error("Cluster ${jobInstance.cluster} not found!")
                return null
            }
            User user = User.findById(jobInstance.ownerId)
            if(!user){
                log.error("No cluster user ${job.user} found!")
                return null
            }
            def runner = clusterService.createRunner(cluster, user)
            def jipGrid = clusterService.createGrid(cluster)
            jobInstance.finishDate = new Date()
            jobInstance.finishState = FinishState.Cancel
            jobInstance.state = State.Done
            if(message){
                if(!jobInstance.messages){
                    jobInstance.messages = []
                }
                Message m = new Message()
                m.message = message
                m.createDate = new Date()
                m.type = Message.Type.WARNING
                jobInstance.messages.add(m)
            }
            jobInstance.save(flush: true)
            try {
                jipGrid.cancel([gridJob], runner)
            } catch (Exception e) {
                log.warn("Unable to cancel job ${jobInstance.id} : ${e.message}")
            }

            // delete higher level jobs
            if(jobInstance.dependsOn && jobInstance.dependsOn.size() > 0){
                jobInstance.dependsOn.each {jobid->
                    jmsService.send(queue:'job.cancel', [id:jobid, message: "Canceled due to dependency"])
                }
            }

            // delete jobs that depend on this one
            def deps = Job.collection.find(["dependsOn": jobInstance.id]).collect{it._id}
            if(deps && deps.size() > 0){
                deps.each { jobid->
                    jmsService.send(queue:'job.cancel', [id:jobid, message: "Canceled due to dependency"])
                }
            }

        } else {
            log.warn "Could not determine what to do with ${job}"
        }
        return null
    }

}
