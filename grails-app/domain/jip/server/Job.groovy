package jip.server

class Job {
    static constraints = {
        command(nullable: true,
                blank: true,
                maxSize: Integer.MAX_VALUE)
        cluster(nullable: false, blank: false)
        state(nullable: false)
        clusterId(nullable: true)
        finishDate(nullable: true)
        name(nullable: true)
        startDate(nullable: true)
        submitDate(nullable: true)
        token(nullable: true)
        finishState(nullable: true)
        workingDirectory(nullable: true)
        jobEnvironment(nullable: true)
        stderrPreview(nullable: true)
        stdoutPreview(nullable: true)
        submitScript(nullable: true)
        jobScript(nullable: true)
    }

    static embedded = ['messages', 'jobEnvironment']

    /**
     * The job id
     */
    Long id

    /**
     * The command used to run the job.
     *
     */
    String command

    /**
     * Job submission script
     */
    String submitScript

    /**
     * The job script that executes the commands
     */
    String jobScript

    /**
     * The working directory used to run the job
     */
    String workingDirectory

    /**
     * The cluster id set when the job is submitted to the remote cluster
     */
    String clusterId


    /**
     * Name of the cluster that runs the job
     */
    String cluster

    /**
     * The jobs state
     */
    State state = State.Queued

    /**
     * The jobs finish state
     */
    FinishState finishState

    /**
     * List of job ids this job depends on
     */
    List<Long> dependsOn

    /**
     * Name of the job
     */
    String name

    /**
     * Secure token associated with this job
     */
    String token

    /**
     * Job environment configuration
     */
    Environment jobEnvironment

    /**
     * Additional job attributes
     */
    Map configuration = [:]

    /**
     * Date of initial job submission to the jip server
     */
    Date createDate = new Date()

    /**
     * Date of submission to the remote cluster
     */
    Date submitDate

    /**
     * Start date of the job. Set when state goes to running
     */
    Date startDate

    /**
     * Date of the job when finished running
     */
    Date finishDate

    /**
     * List of messages associated with the job
     */
    List<Message> messages

    /**
     * Number of submissions to the remote cluster
     */
    int submissions

    /**
     * Mark a job as archived
     */
    boolean archived = false

    /**
     * ID of the owning user
     */
    String ownerId
    /**
     * Progress information
     */
    int progress = -1
    /**
     * Std error preview
     */
    String stderrPreview
    /**
     * Std out preview
     */
    String stdoutPreview

    /**
     * True if this job is part of a pipeline
     */
    boolean inPipeline = false

    /**
     * Contains jobs associated with this pipeline
     */
    List<Long> pipelineJobs

    /**
     * Add message to the list of messages. The message
     * is pushed to the mongodb array using low level $push
     *
     * @param type the type
     * @param message the message
     */
    public void addMessage(Message.Type type, String message){
        addMessage(type, message, true)
    }

    /**
     * Add message to the list of messages. If push is true, the message
     * is pushed to the mongodb array using low level $push
     *
     * @param type the type
     * @param message the message
     */
    public void addMessage(Message.Type type, String message, boolean push){
        if(messages == null){
            messages = []
        }
        if(type == null){
            type = Message.Type.INFO
        }
        if(message == null){
            message = ""
        }
        messages << new Message(type: type, message:message)
        // do mongo push
        if(id > 0 && push){
            log.info("Pushing Job message to mongo DB ${type}: ${message}")
            Job.collection.update(['_id':id], ['$push':[
                    'messages':[
                            'type':type.toString(),
                            'message':message,
                            'createDate':new Date()
                    ]
            ]])
        }
    }
    /**
     * Push progress update to db using low level update
     * @param progress the progres
     */
    public void updateProgress(int progress){
        this.progress = progress
        // do mongo push
        if(id > 0){
            log.info("Pushing Job ${id} progress to mongo DB : ${progress}")
            Job.collection.update(['_id':id], ['$set':['progress':progress]])
        }
    }

}
