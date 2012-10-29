package jip.server

import org.springframework.dao.DataIntegrityViolationException
import grails.plugins.springsecurity.Secured

@Secured(["ROLE_USER","ROLE_ADMIN"])
class JobController {

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def index() {
        redirect(action: "list", params: params)
    }

    def list(Integer max) {
        params.max = 100
        log.info(params)
        def jobs = fetchJobs(params)
        def jobCount = Job.countByArchived(false)
        def queuedCount = Job.countByStateAndArchived(State.Queued, false)
        def runningCount = Job.countByStateAndArchived(State.Running, false)
        def successCount = Job.countByStateAndFinishStateAndArchived(State.Done, FinishState.Success, false)
        def errorCount = Job.countByStateAndFinishStateAndArchived(State.Done, FinishState.Error, false)
        def cancelCount = Job.countByStateAndFinishStateAndArchived(State.Done, FinishState.Cancel, false)
        def holdCount = Job.countByStateAndArchived(State.Hold, false)

        def counts = [
                all: jobCount,
                queued: queuedCount,
                running: runningCount,
                success: successCount,
                error: errorCount,
                cancel: cancelCount,
                hold: holdCount
        ]

        def jobList = prepareJobModel(jobs)
        [jobInstanceList: jobList, counts: counts, sort:params.sort, order:params.order]
    }

    /**
     * Render job list
     *
     * @param offset the page number
     * @return table renders the table cells
     */
    def listJobs(Integer offset){
        log.info("Ajav job list ${params}")
        params.max = 100
        params.offset = offset * params.max
        def jobs = fetchJobs(params)
        def jobList = prepareJobModel(jobs)
        render template: "job_list_rows", model: [jobInstanceList: jobList]
    }

    def fetchJobs(params) {
        def max = Math.min(params.max?.toInteger() ?: 10, 100)
        def offset = params.offset?.toInteger() ?: 0
        def sort = params.sort ? params.sort : 'id'
        def ord = params.order ? params.order : 'asc'
        String term = "%${params?.q}%"
        String rawTerm = params?.q

        State qstate = null
        FinishState fstate = null
        if(rawTerm){
            term = term.replaceAll(/\s+/, "%")
            def lc = rawTerm.toLowerCase()
            def stateTerm = lc[0].toUpperCase() + lc[1..lc.length() - 1]
            if(stateTerm == "Error" || stateTerm == "Cancel" || stateTerm == "Done"){
                qstate = State.Done
                fstate = FinishState.valueOf(stateTerm)
            }else{
                try {
                    qstate = State.valueOf(stateTerm)
                } catch (Exception ignore) {
                }
            }

        }
        def c = Job.createCriteria()
        def jobs = c.list {
            and{
                eq('archived', false)
                eq('inPipeline', false)
                if(rawTerm && !rawTerm.isEmpty()){
                    or{
                        ilike('name', term)
                        like('cluster', term)
                        eq('id', rawTerm)
                        eq('clusterId', rawTerm)
                        and{
                            if(qstate){
                                eq("state", qstate)
                            }
                            if(fstate){
                                eq('finishState', fstate)
                            }
                        }
                    }
                }
                if (sort == "state"){
                    order('state', ord)
                    order('finishState', ord)
                }else{
                    order(sort, ord)
                }
            }
            maxResults(max)
            firstResult(offset)
        }
        log.info("searched with criteria ${params} ${ord} ${sort}")
        return jobs
    }

    private List prepareJobModel(List<Job> jobs) {
        def jobList = []
        jobs?.each { Job j ->
            def jj = [:]
            jj.name = j.name ? j.name : j.command
            jj.cluster = j.cluster
            jj.clusterId = j.clusterId ? j.clusterId : "-"
            jj.id = j.id
            jj.owner = User.get(j.ownerId)?.username
            jj.jobState = j.state
            jj.state = j.state.toString().toLowerCase()
            def dateFormat = "dd/MM/yy - HH:mm"
            jj.createDate = j.createDate ? j.createDate.format(dateFormat) : "-"
            jj.submitDate = j.submitDate ? j.submitDate.format(dateFormat) : "-"
            jj.startDate = j.startDate ? j.startDate.format(dateFormat) : "-"
            jj.finishDate = j.finishDate ? j.finishDate.format(dateFormat) : "-"
            if (j.state == State.Done) {
                jj.state = j.finishState.toString().toLowerCase()
            }
            jj.stateText = j.state == State.Done ? j.finishState.toString() : j.state.toString()
            jj.stdout = j.stdoutPreview ? j.stdoutPreview : ""
            jj.stderr = j.stderrPreview ? j.stderrPreview : ""
            jj.inPipeline = j.inPipeline
            def tasks = j.pipelineJobs?.collect { Job.get(it)}
            jj.pipelineJobs = prepareJobModel(tasks)
            if (j.inPipeline){
                //jj.pipeline = Job.findWhere()
            }

            def messageCounts = [
                    info: 0,
                    warning: 0,
                    error: 0
            ]
            jj.progress = j.progress
            if(j.pipelineJobs){
                jj.progress = 100 * (tasks.findAll {it.state == State.Done}.size() / j.pipelineJobs.size())
            }
            if(j.progress >= 0 && j.state == State.Done){
                jj.progress = 100
            }
            if (j.messages) {

                def lastMessage = j.messages.last()
                if (lastMessage && lastMessage.message) {
                    jj.lastMessage = [
                            type: lastMessage.type.toString().toLowerCase(),
                            message: lastMessage.message
                    ]
                }
                jj.messages = j.messages
            }
            if (j.state == State.Running) {
                def diff = System.currentTimeMillis() - j.startDate.time
                def age = new Time((long) (diff / 1000l)).toString()
                jj.age = "Time ${age}"
                if (j.jobEnvironment?.time) {
                    jj.age = "${jj.age} / ${new Time(j.jobEnvironment.time).toString()}"
                }
            }else if (j.state == State.Done){
                def time = j.startDate ? j.startDate : j.submitDate
                def diff = j.finishDate.time - time.time
                def age = new Time((long) (diff / 1000l)).toString()
                jj.age = "Duration ${age}"
            }else {
                def time = j.submitDate ? j.submitDate : j.createDate
                def diff = System.currentTimeMillis() - time.time
                def age = new Time((long) (diff / 1000l)).toString()
                jj.age = "Age ${age}"
            }

            jobList << jj
        }
        return jobList
    }


    def show(Long id) {
        def jobInstance = Job.get(id)
        if (!jobInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'job.label', default: 'Job'), id])
            redirect(action: "list")
            return
        }

        [jobInstance: prepareJobModel([jobInstance])[0]]
    }

    def delete(Long id) {
        def jobInstance = Job.get(id)
        if (!jobInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'job.label', default: 'Job'), id])
            redirect(action: "list")
            return
        }

        try {
            jobInstance.delete(flush: true)
            flash.message = message(code: 'default.deleted.message', args: [message(code: 'job.label', default: 'Job'), id])
            redirect(action: "list")
        }
        catch (DataIntegrityViolationException e) {
            flash.message = message(code: 'default.not.deleted.message', args: [message(code: 'job.label', default: 'Job'), id])
            redirect(action: "show", id: id)
        }
    }

    def loadMessages(Long id){
        def jobInstance = Job.get(id)
        if (!jobInstance) {
            render ""
        }
        render template: "job_message", model: [job:jobInstance]
    }
}
