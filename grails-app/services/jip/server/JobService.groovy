package jip.server

import java.security.MessageDigest
import jip.grid.commands.JipRunner
import org.codehaus.groovy.grails.web.pages.GroovyPagesTemplateEngine
import groovy.json.JsonOutput

class JobService {

    def jmsService

    def renderTemplateService

    def jipService

    def clusterService

    /**
     * Create a new job. The cluster must be known to the
     * system and the command can not be null or empty
     *
     * @param command the command
     * @param clusterName the cluster id
     */
    def createJob(JobDefinition jobDefinition, String clusterName) {
        if(jobDefinition == null) throw new NullPointerException("NULL job definition not permitted")

        def cluster = Cluster.findByName(clusterName)
        if(cluster == null){
            throw new RuntimeException("Cluster ${clusterName} not found !")
        }
        log.info("Creating new job for cluster ${cluster} with command : ${jobDefinition.command}")
        def job = new Job(
                command: jobDefinition.command,
                cluster: cluster.name,
                ownerId: jobDefinition.owner,
                workingDirectory: jobDefinition.workingDirectory,
                configuration: jobDefinition.configuration,
        )

        if(jobDefinition.environment){
            job.jobEnvironment = new Environment(jobDefinition.environment)
        }

        // create token
        String tokenSource = "${jobDefinition.owner}/${job.createDate.time}/${job.command.toString()}/${Random.newInstance().nextGaussian()}".toString()
        job.token = MessageDigest.getInstance("MD5").
                digest(tokenSource.getBytes("UTF-8")).
                encodeHex().
                toString()

        if(jobDefinition.hold){
            job.state = State.Hold
        }
        if(!job.validate()){
            log.error("Unable to store job : " + job.errors.allErrors)
            throw new RuntimeException("Job is not valid and can not be stored !")
        }

        if(job.jobEnvironment){
            job.jobEnvironment.save(flush: true)
        }
        def storedJob = job.save(flush: true)
        if(!jobDefinition.hold){
            log.info("Sending job ${job.id} to ${cluster.name} for submission")
            jmsService.send(queue:'job.submit', [id:storedJob.id])
        }
        return storedJob
    }

    void updateState(Long jobId, State newState, FinishState finishState) {
        Job job = Job.findById(jobId)
        if(!job){
            throw new RuntimeException("Job ${jobId} not found!")
        }
        if(!job.state.nextStates.contains(newState)){
            throw new RuntimeException("Illage state ${newState} for ${job.id} with state ${job.state}".toString())
        }

        job.state = newState
        switch(newState){
            case State.Hold:
                job.finishState = null
                break // todo : cancel running jobs ?
            case State.Queued: break
            case State.Submitted:
                job.submitDate = new Date()
                job.finishState = null
                break
            case State.Running:
                job.startDate = new Date()
                job.finishState = null
                break
            case State.Done:
                job.finishDate = new Date()
                job.finishState = finishState
                break
        }
        job.save(flush: true)
        switch(newState){
            case State.Hold: break
            case State.Queued:
                jmsService.send(queue:'job.submit', [id:job.id])
                break
            case State.Submitted: break
            case State.Running: break
            case State.Done:
                if(job.finishState && job.finishState == FinishState.Cancel){
                    jmsService.send(queue:'job.cancel', [id:job.id])
                }
                // todo send message to job checker to check for fail state
                break
        }

    }

    void cancel(Long jobId) {
        Job job = Job.findById(jobId)
        if(!job){
            throw new RuntimeException("Job ${jobId} not found!")
        }
        job.finishDate = new Date()
        job.finishState = FinishState.Cancel
        job.state = State.Done
        job.save(flush: true)
        // todo : send message to get job info
        jmsService.send(queue:'job.cancel', [id:job.id])
    }

    /**
     * Submit the list of jobs in order
     *
     * @param jobs the list of jobs
     * @param cluster the cluster
     */
    void submit(List<Job> jobs, Cluster cluster, ClusterUser clusterUser){
        def baseUrl = jipService.baseUrl()
        for (Job job : jobs) {
            def url = cluster.serverUrl && !cluster.serverUrl.isEmpty() ? cluster.serverUrl : baseUrl
            Map jobDef = [
                    id: job.id,
                    url: url,
                    cluster: cluster.name,
                    environment: job.jobEnvironment,
                    token: job.token,
                    cwd: job.workingDirectory
            ]

            // render job command
            jobDef['command'] = renderTemplateService.renderString(job.command, [job:jobDef])

            // render job script
            def startTemplate = '''#!/bin/bash
# jip environment
export JIP_URL=${job.url}
export JIP_CLUSTER=${job.cluster}
export JIP_JOB=${job.id}
export JIP_TOKEN=${job.token}


# add an alias to jip
export PATH=$PATH:${jip.home}/jip-environment/bin/

${job.command}
'''
            job.jobScript = renderTemplateService.renderString(startTemplate, [
                    job: jobDef,
                    jip:['home': cluster.home]

            ])
            jobDef['script'] = job.jobScript

            // render start script
            job.submitScript = """#!/bin/bash
${cluster.jip} exec << JOBDEFEND
${JsonOutput.toJson(jobDef)}
JOBDEFEND
"""
            def runscript = File.createTempFile("jip", "job.sh")
            runscript.write(job.submitScript)
            def originalCommand = job.command
            def runner = cluster.createRunner(clusterUser)
            def remoteScript = "${cluster.home}/jobscripts/${runscript.name}"
            log.info("Copy job script ${runscript.absolutePath} to ${remoteScript}")
            runner.copy(runscript, remoteScript)
            job.command = remoteScript
            log.info("Submitting job to grid")
            def grid = clusterService.createGrid(cluster)
            grid.submit([job], runner)
            //runner.run("rm ${home}/${runscript.name}", null, null)
            job.command = originalCommand
        }


    }
}
