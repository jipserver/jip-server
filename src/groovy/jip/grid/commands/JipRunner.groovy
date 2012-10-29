package jip.grid.commands

import groovy.json.JsonOutput

/**
 * Wrap around a given runner and use the
 * jip python wrapper to execute commands and run jobs
 */
class JipRunner implements CommandRunner{
    private CommandRunner delegate
    /**
     * Path to the jip client
     */
    private String jip

    /**
     * Create a new runner that uses the given delegate runner to
     * execute commands and uses the given path to the jip.py
     * module to trigger jip jobs
     *
     * @param delegate the delegate
     * @param home absolute path to the jip home folder
     */
    JipRunner(CommandRunner delegate, String home) {
        this.delegate = delegate
        this.jip = "${home}/jip-environment/bin/jip-wrapper.sh"
    }

    @Override
    CommandProcess run(String command, String workingDir, Map environment) {
        return delegate.run(command, workingDir, environment)
    }

    @Override
    void copy(File source, String target) {
        delegate.copy(source, target)
    }

    @Override
    void get(String source, File target) {
        delegate.get(source, target)
    }

    CommandProcess run(Map job){
    /*
        self.id = jobdef['id']
        self.command = jobdef['command']
        self.url = jobdef.get("url", None)
        self.cluster = jobdef.get("cluster", None)
        self.token = jobdef.get("token", None)
        self.stdin = jobdef.get("stdin", None)
        self.cwd = jobdef.get("cwd", ".")
    */
        if(!job.containsKey('command')) throw new IllegalArgumentException("No command id specified")
        def process = run("${jip} exec", null, null)
        def read = process.errorStream.read()
        if (read == '1'){
            process.outputStream.write("${JsonOutput.toJson(job)}\n".toString().bytes)
            process.outputStream.close()
        }else{
            process.destroy()
            throw new RuntimeException("Unable to run script! Unknown response ${read}: " + process.errorStream.text)
        }
        return process
    }

    String createKeys(){
        def process = run("${jip} create-keys", null, null)
        if (process.waitFor() != 0){
            throw new RuntimeException("Error while creating keys: ${process.errorStream.text}")
        }
        return process.inputStream.text
    }

    String write(String string){
        def process = run("${jip} write", null, null)
        def read = process.errorStream.read()
        if (read == '1'){
            process.outputStream.write(string.bytes)
            process.outputStream.close()
        }else{
            process.destroy()
            throw new RuntimeException("Unable to write tmp file, unknow response ${read}: " + process.errorStream.text)
        }
        if(process.waitFor() != 0){
            throw new RuntimeException("Unable to write tmp file : " + process.errorStream.text)
        }
        def text = process.inputStream.text
        return text.trim()
    }
}
