import grails.util.GrailsUtil
import jip.grid.local.LocalGrid
import jip.server.cluster.ClusterStatusCheckJob
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import jip.server.*

class BootStrap {
    /**
     * The main logger
     */
    Logger log = LoggerFactory.getLogger(BootStrap.class)
    /**
     * The application
     */
    GrailsApplication grailsApplication

    def jipService

    def init = { servletContext ->
        switch (GrailsUtil.environment) {
            case 'development':
                // clear database
                //Job.collection.getDB().dropDatabase()
                break
            case 'test':
                // clear database
                Job.collection.getDB().dropDatabase()
                break
            case 'production':
                break
        }


        // bootstrap admin user and default groups
        Role adminRole = Role.findByAuthority("ROLE_ADMIN")
        Role userRole = Role.findByAuthority("ROLE_USER")

        if(!adminRole){
            adminRole = new Role(authority: "ROLE_ADMIN").save(flush: true)
        }
        if(!userRole){
            userRole = new Role(authority: "ROLE_USER").save(flush: true)
        }

        User admin = User.findByUsername("admin")
        if(admin == null){
            log.info("Creating admin user")
            admin = new User(username: "admin", password: "admin", enabled: true)
            admin.encodePassword()
            admin = admin.save(flush: true)
            new UserRole(user:admin, role:adminRole).save(flush: true)
        }else{
            log.info("Admin users found, skipping admin creation")
        }

        switch (GrailsUtil.environment) {
            case 'development':
            case 'test':
                User user = User.findByUsername("user")
                if(user == null){
                    log.info("Creating test user user")
                    user = new User(username: "user", password: "user", enabled: true)
                    user.encodePassword()
                    user.save(flush: true)
                    new UserRole(user:user, role:userRole).save(flush: true)
                }
                break
        }


        // bootstrap the application and
        // load the default configuration from the
        // the JIP_HOME folder from either system properties
        // or environment
        def home = new File("jip_home")
        if(System.getenv("JIP_HOME")){
            home = new File(System.getenv("JIP_HOME"))
        }else if (System.getProperty("jip.home", null) != null){
            home = new File(System.getProperty("jip.home", null))
        }

        log.info("Jip home directory : ${home.absolutePath}")
        def grailsCfg = grailsApplication.getConfig()
        if(!grailsCfg.containsKey("jip")){
            grailsCfg.put("jip", [:])
        }
        grailsCfg.get("jip")['home'] = home
        def config = jipService.loadConfigurationFromHome(home)
        grailsCfg.get("jip")['config'] = null
        if(config){
            grailsCfg.get("jip")['config'] = config
        }
    }

    def destroy = {
    }
}
