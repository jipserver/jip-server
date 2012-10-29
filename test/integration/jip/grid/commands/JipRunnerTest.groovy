package jip.grid.commands

import com.google.common.io.Files
import jip.grid.JipGrids
import org.apache.commons.io.FileUtils

class JipRunnerTest extends GroovyTestCase {
    File tmpDir

    public void setUp(){
        tmpDir = Files.createTempDir()
    }

    public void tearDown(){
        FileUtils.deleteDirectory(tmpDir)
    }

    public void testRunningLocalCommandsWithJip(){
        def localRunner = new LocalRunner()
        JipGrids.initialize(localRunner, tmpDir.absolutePath, "", "")
        def runner = new JipRunner(localRunner, tmpDir.absolutePath)
        try{
            runner.run([:])
            fail()
        }catch (IllegalArgumentException expected){
            // expected
        }
        assert runner.run([command: "ls -la"]).waitFor() == 0
    }
}
