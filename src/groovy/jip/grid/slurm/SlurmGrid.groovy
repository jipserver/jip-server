/*
 * Copyright (C) 2012 Thasso Griebel
 *
 * This file is part of JIP.
 *
 * JIP is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JIP is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with JIP.  If not, see <http://www.gnu.org/licenses/>.
 */



package jip.grid.slurm

import jip.grid.Grid
import jip.grid.commands.CommandRunner
import jip.grid.model.GridJobState
import jip.server.Environment
import jip.server.Job
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * Slurm grid implementation
 *
 * @author Thasso Griebel <thasso.griebel@gmail.com>
 */
class SlurmGrid implements Grid{
    /**
     * Slurm grid type
     */
    public static final String TYPE = "slurm"

    /**
     * All possible slurm states and the mapping to the job state
     POSSIBLE SLURM STATES
     CA  CANCELLED       Job was explicitly cancelled by the user or system administrator.  The job may or may not have been initiated.
     CD  COMPLETED       Job has terminated all processes on all nodes.
     CF  CONFIGURING     Job has been allocated resources, but are waiting for them to become ready for use (e.g. booting).
     CG  COMPLETING      Job is in the process of completing. Some processes on some nodes may still be active.
     F   FAILED          Job terminated with non-zero exit code or other failure condition.
     NF  NODE_FAIL       Job terminated due to failure of one or more allocated nodes.
     PD  PENDING         Job is awaiting resource allocation.
     R   RUNNING         Job currently has an allocation.
     S   SUSPENDED       Job has an allocation, but execution has been suspended.
     TO  TIMEOUT         Job terminated upon reaching its time limit.
     */
    public static Map<String, GridJobState> STATE_MAP = new HashMap<String, GridJobState>(){{
        put("CANCELLED", GridJobState.Canceled);
        put("CA", GridJobState.Canceled);
        put("COMPLETED", GridJobState.Done);
        put("CD", GridJobState.Done);
        put("CONFIGURING", GridJobState.Queued);
        put("CF", GridJobState.Queued);
        put("COMPLETING", GridJobState.Queued);
        put("CG", GridJobState.Queued);
        put("PENDING", GridJobState.Queued);
        put("PD", GridJobState.Queued);
        put("FAILED", GridJobState.Error);
        put("F", GridJobState.Error);
        put("NODE_FAIL", GridJobState.Error);
        put("NF", GridJobState.Error);
        put("TIMEOUT", GridJobState.Error);
        put("TO", GridJobState.Error);
        put("RUNNING", GridJobState.Running);
        put("R", GridJobState.Running);
        put("SUSPENDED", GridJobState.Running); // todo : what are the transisitons for suspended ? do we need a special state ?
        put("S", GridJobState.Running);
    }};


    /**
     * General slurmd log pattern. If it matches, you will get three groups.
     * <pre>
     *     1. the node
     *     2. the job id
     *     3. the timestamp
     *     4. the message (optional, might be empty)
     * </pre>
     */
    public static final Pattern SLURM_LOG_PATTERN = Pattern.compile(".*slurmd\\[(.*)\\]: \\*\\*\\* JOB (\\d+) CANCELLED AT ([0-9\\-:T]+) (.*)\\*\\*\\*\$");
    /**
     * Date format for the slurm timestamp
     */
    public static final DateFormat LOG_TIMESTAMP = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    /**
     * Job submit pattern
     */
    static Pattern SUBMIT_PATTERN = Pattern.compile(".*Submitted batch job (\\d+).*", Pattern.MULTILINE | Pattern.DOTALL);


    /**
     * The logger
     */
    private final Logger log = LoggerFactory.getLogger(getClass())
    /**
     * Path to the sbatch command
     */
    String sbatch
    /**
     * Path to the scancel command
     */
    String scancel
    /**
     * Path to the squeue command
     */
    String squeue

    /**
     * The run directory
     */
    String directory;
    /**
     * The configuration
     */
    Map configuration

    SlurmGrid(Map attr) {
        this.sbatch = attr?.sbatch ? attr.sbatch : "sbatch"
        this.scancel = attr?.scancel ? attr.scancel : "scancel"
        this.squeue = attr?.squeue ? attr.squeue : "squeue"
        this.directory = attr?.directory ? attr.directory : "/tmp"
        this.configuration = attr;
    }

    @Override
    Map<String, GridJobState> list(final CommandRunner runner) throws Exception{
        def output = runner.run("""${squeue} -h -o "%i %T %N %l %S""", ".", [:]);
        def result = parseSqueueOutput(output.inputStream)
        if (output.waitFor() != 0) {
            throw new RuntimeException("Slurm polling failed! Error Message: ${output.errorStream.text}");
        }
        return result;
    }

    Map<String, GridJobState> parseSqueueOutput(InputStream output) throws IOException {
        log.debug("Parsing squeue output");
        final Map<String, GridJobState> states = new HashMap<String, GridJobState>();
        BufferedReader commandOutput = new BufferedReader(new InputStreamReader(output));
        String l = null;
        String[] split = null;
        while ((l = commandOutput.readLine()) != null) {
            if(l.startsWith("\"")) l = l.substring(1);
            if(l.endsWith("\""))l = l.substring(0, l.length()-1);
            split = l.split(" ");
            if (split.length != 5){
                log.warn("squeue output does not contain 5 fields: " + l);
                continue;
            }
            log.debug("squeue add grid job " + split[0]);
            states.put(split[0], STATE_MAP.get(split[1]));
        }
        commandOutput.close();
        return states;
    }


    @Override
    void cancel(List<Job> jobs, CommandRunner runner) {
        def jobids = jobs.collect {it.clusterId}
        log.debug("Slurm cancel jobs ${jobids}")
        runner.run("""${scancel} ${jobids.join(' ')}""", null, null).waitFor()
    }

    @Override
    void submit(List<Job> jobs, CommandRunner runner) {
        for (Job job : jobs) {
            def params = [[sbatch]]
            if(job.jobEnvironment){
                def environment = job.jobEnvironment
                if(environment.cpus > 0) params<<['-c', "${environment.cpus}"]
                if(environment.nodes > 0) params<<["-N", "${environment.nodes}"]
                if(environment.qos) params<<["--qos=${environment.qos}"]
                if(environment.partition) params<<["-p", "${environment.partition}"]
                if(environment.maxMemory > 0) params<<["--mem-per-cpu=${environment.maxMemory}"]
                if(environment.freeTempSpace > 0) params<<["--tmp=${environment.freeTempSpace}"]
                if(environment.time && environment.time > 0) params<<["-t", "${environment.time/60}"]
                if(environment.additionalProperties){
                    params << environment.additionalProperties
                }
            }else{
                job.jobEnvironment = new Environment()
            }
            // set log files
            if(!job.jobEnvironment.stdoutFile){
                job.jobEnvironment.stdoutFile = "jip-${job.id}.out"
            }
            if(!job.jobEnvironment.stderrFile){
                job.jobEnvironment.stderrFile = "jip-${job.id}.err"
            }
            params << ['-o', job.jobEnvironment.stdoutFile, '-e', job.jobEnvironment.stderrFile]

            if (job.dependsOn && job.dependsOn.size() > 0){
                 params << ['-d', "afterok:${job.dependsOn.collect {Job.findById(it).clusterId}.join(':')}"]
            }
            if (job.workingDirectory){
                 params << ['-D', job.workingDirectory]
            }
            params << job.command

            def process = runner.run(params.flatten().join(" "), job.workingDirectory, [:])
            def res = process.inputStream.text
            Matcher m = SUBMIT_PATTERN.matcher(res);
            if(process.waitFor() != 0 || !m.matches()){
                throw new RuntimeException("Unable to submit job : ${process.errorStream.text}")
            }
            def jobId = m.group(1)
            job.clusterId = jobId
        }
    }

    @Override
    void copy(File source, String target, CommandRunner runner) {
        runner.copy(source, target)
    }
}
