package jip.server

/**
 * Job definition as it comes in for submission
 */
class JobDefinition{
    /**
     * The command used to run the job.
     *
     */
    String command

    /**
     * The working directory used to run the job
     */
    String workingDirectory

    /**
     * Name of the job
     */
    String name

    /**
     * Job environment configuration
     */
    Map environment

    /**
     * Additional job attributes
     */
    Map configuration
    /**
     * User id of the job owner
     */
    String owner
    /**
     * Set to true if the job should not be submitted directly
     */
    boolean hold = false;

    JobDefinition(Map job) {
        if(!job.containsKey("command")){
            throw new IllegalArgumentException("No command specified!")
        }else{
            this.command = job.command
        }
        this.workingDirectory = job.get("cwd", null)
        this.environment = job.get("environment", null)
        this.name = job.get("name", null)
        this.configuration = job.get("configuration", null)
        this.hold = job.get("hold", false)
    }
}
