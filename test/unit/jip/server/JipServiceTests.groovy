package jip.server



import grails.test.mixin.*
import org.junit.*
import com.google.common.io.Files
import org.apache.commons.io.FileUtils

/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
@TestFor(JipService)
class JipServiceTests {

    void testGetHome() {
        def service = new JipService()
        try {
            service.home()  // not initialized
        } catch (RuntimeException e) {
            assert e.message == "JIP home directory is not configured!"
        }
    }

    void testInitializeHome(){
        def dir = Files.createTempDir()
        try{
            def service = new JipService()
            service.grailsApplication = [
                    "config":[
                            "jip":[
                                    "home":dir.absolutePath
                            ]
                    ]
            ]
            def jipHome = service.home()
            assert jipHome == dir
            assert new File(jipHome, "keys").exists()
            assert new File(jipHome, "logs").exists()
        }finally {
            FileUtils.deleteDirectory(dir)
        }
    }
}
