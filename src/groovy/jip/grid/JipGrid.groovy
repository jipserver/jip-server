package jip.grid

import groovy.json.JsonOutput
import jip.grid.commands.CommandRunner
import jip.grid.commands.JipRunner
import jip.grid.model.GridJobState
import jip.server.Job
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.codehaus.groovy.grails.web.pages.GroovyPagesTemplateEngine

/**
 * Wrapper implementation of a grid that uses a JipRunner to run jobs
 */
class JipGrid implements Grid {

    /**
     * The logger
     */
    private Logger log = LoggerFactory.getLogger(JipGrid.class)

    /**
     * The wrapped grid
     */
    private Grid grid

    /**
     * Absolute path to the jip warpper helper
     */
    String jipPath

    /**
     * Base url of the jip server
     */
    String baseUrl

    /**
     * JIP jome
     */
    String home

    /**
     * Create a new jip grid with the absolute remote
     * path to the jip.py helper.
     *
     * @param jipPath absolute path to the jip.py helper
     * @param baseUrl the base url of the jip server or null
     * @param grid the wrapped grid implementation
     */
    JipGrid(String home, String baseUrl, Grid grid) {
        this.home = home
        this.jipPath = "${home}/jip-environment/bin/jip-wrapper.sh"
        this.grid = grid
        this.baseUrl = baseUrl
    }

    @Override
    String getId() {
        return grid.id
    }

    @Override
    void cancel(List<Job> jobs, CommandRunner runner) {
        grid.cancel(jobs, runner)
    }

    @Override
    void submit(List<Job> jobs, CommandRunner runner) {
        // instead of directly passing the jobs, we write a
        // temporary script, update the job command to the script,
        // submit the job, delete the temporary script and
        // reset the old command
        def jipRunner = new JipRunner(runner, jipPath)
        def templateEngine = new GroovyPagesTemplateEngine()

        for (Job job : jobs) {
            Map jobDef = [
                    id: job.id,
                    command: job.command,
                    url: this.baseUrl,
                    cluster: grid.id,
                    environment: job.jobEnvironment,
                    token: job.token,
                    cwd: job.workingDirectory
            ]

            // render job script
            def templateTarget = new StringWriter()
            def startTemplate = '''#!/bin/bash
# jip environment
export JIP_URL=${jip.url}
export JIP_CLUSTER=${jip.cluster}
export JIP_JOB=${jip.jobid}
export JIP_TOKEN=${jip.token}


# add an alias to jip
alias jip=${jip.home}/jip-environment/bin/jip

${job.command}
'''
            def templateModel = [
                    'jip':[
                            'jobid':job.id,
                            'url':this.baseUrl,
                            'cluster':grid.id,
                            'token':job.token,
                    ],
                    'job': jobDef
            ]
            templateEngine.createTemplate(startTemplate,"job").make(templateModel).writeTo(templateTarget)
            job.jobScript = templateTarget.toString()
            jobDef['script'] = job.jobScript

            // render start script
            job.submitScript = """#!/bin/bash
${jipPath} exec << JOBDEFEND
${JsonOutput.toJson(jobDef)}
JOBDEFEND
"""
            def runscript = File.createTempFile("jip", "job.sh")
            runscript.write(job.submitScript)
            def originalCommand = job.command
            runner.copy(runscript, "${home}/${runscript.name}")
            job.command = "${home}/${runscript.name}"
            grid.submit([job], jipRunner)
            //runner.run("rm ${home}/${runscript.name}", null, null)
            job.command = originalCommand
        }

    }

    @Override
    Map<String, GridJobState> list(CommandRunner runner) {
        return grid.list(runner)
    }

    @Override
    void copy(File source, String target, CommandRunner runner) {
        grid.copy(source, target, runner)
    }
}
