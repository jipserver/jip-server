package jip.grid

import com.google.common.io.Files
import jip.grid.commands.LocalRunner
import jip.grid.local.LocalGrid
import jip.server.Job
import org.apache.commons.io.FileUtils

class JipGridTest extends GroovyTestCase {
    File tmpDir

    public void setUp(){
        tmpDir = Files.createTempDir()
    }

    public void tearDown(){
        FileUtils.deleteDirectory(tmpDir)
    }

    public void testSubmittingJob(){
        LocalRunner localRunner = new LocalRunner()
        def localGrid = new LocalGrid("local", [:])
        def paths = JipGrids.initialize(localRunner, tmpDir.absolutePath, "", "")

        def jipGrid = new JipGrid(tmpDir.absolutePath, null, localGrid)
        def job = new Job()
        job.id = 1
        job.command = ["ls", "-la"]
        jipGrid.submit([job], localRunner)
        assert job.clusterId == "1"
        def joblist = localGrid.list(localRunner)
        while(joblist.size() > 0){
            println "Waiting for job to finish ${joblist}"
            Thread.sleep(100)
            joblist = localGrid.list(localRunner)
        }
        assert localGrid.states.size() == 0
        assert localGrid.futures.size() == 0

    }
}
