package jip.grid

import com.google.common.io.Files
import jip.grid.commands.LocalRunner
import org.apache.commons.io.FileUtils

/**
 * Created with IntelliJ IDEA.
 * User: thasso
 * Date: 8/29/12
 * Time: 9:19 PM
 * To change this template use File | Settings | File Templates.
 */
class JipGridsTest extends GroovyTestCase {

    static File home
    File tmpDir

    public void setUp(){
        tmpDir = Files.createTempDir()
        home = new File(tmpDir, "jip-home-test")
    }

    public void tearDown(){
        // delete
        FileUtils.deleteDirectory(tmpDir)
    }

    void testInitialize() {
        def runner = new LocalRunner()
        JipGrids.initialize(runner, home.absolutePath, "", "")
        assertTrue(new File("${home.absolutePath}").exists())
        assertTrue(new File("${home.absolutePath}/bin/").exists())
        assertTrue(new File("${home.absolutePath}/bin/activate").exists())
    }
}
