package jip.server

class Environment {
    static constraints = {
        stdoutFile(nullable: true)
        stdinFile(nullable: true)
        stderrFile(nullable: true)
        partition(nullable: true)
        qos(nullable: true)
        additionalProperties(nullable:true)

    }
    /**
     * Max time in seconds
     */
    long time

    /**
     * Maximum memory in MB
     */
    long maxMemory

    /**
     * Free temp space requested
     */
    long freeTempSpace;

    /**
     * Number of requested nodes
     */
    int nodes

    /**
     * Number of requested CPUs
     */
    int cpus

    /**
     * Path to the stdout logfile
     */
    String stdoutFile

    /**
     * Path to the stderr logfile
     */
    String stderrFile

    /**
     * Path to the stdin file
     */
    String stdinFile

    /**
     * Partition requested by the job. This is the general partition/queue requested
     */
    String partition

    /**
     * Quality of service class
     */
    String qos

    /**
     * List of additional properties that are passed to the grid engine
     */
    List<String> additionalProperties
}
