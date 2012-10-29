package jip.server

/**
 * JIP service that provides access to the jip server configuration
 * and the local storage folder
 */
class JipService {

    /**
     * This service is not transactional
     */
    static transactional = false

    /**
     * The grails application
     */
    def grailsApplication

    /**
     * Access to the jip home folder or null if the home is
     * not configured. If the home folder is configured but
     * does not exist, the folder is created and permissions
     * are set
     *
     * @return home the jip home folder
     */
    File home() {
        def config = grailsApplication?.config
        def jipConfig = config?.get("jip")
        String homeFolder = jipConfig?.get('home')
        if(homeFolder != null) {
            def homeDir = new File(homeFolder)
            if(!validateHome(homeDir)){
                initializeHome(homeDir)
            }
            return homeDir
        }else{
            throw new RuntimeException("JIP home directory is not configured!")
        }
    }

    /**
     * Get keys directory
     *
     * @return key the keys directory
     */
    File keys() {
        return new File(home(), "keys")
    }


    /**
     * Validate home directory structure
     *
     * @param home the directory
     * @return valid true if all necessary folders exist
     */
    boolean validateHome(File home) {
        if(!home.exists()) return false
        if(!new File(home, "keys").exists()) return false
        if(!new File(home, "logs").exists()) return false
        return true
    }

    /**
     * Initializes the JIP home dir, sets permissions and
     * throws an exception if the folder can not be created
     *
     * @param home the jip home directory
     */
    void initializeHome(File home) {
        log.info("Initializing JIP home folder in ${home.absolutePath}")
        home = createDirectory(null, home.absolutePath)
        File keys = createDirectory(home, "keys")
        File logs = createDirectory(home, "logs")
        Process permissionChanger = null
        try {
            permissionChanger = "chmod -R 0700 ${home.absolutePath}".execute()
        } catch (Exception error) {
            log.error("Error while setting JIP home folder permissions!\n${permissionChanger.inputStream.text}\n${permissionChanger.errorStream.text}", error)
        }
    }

    /**
     * Helper to create a sub directory and throw an Exception if the
     * directory can not be created or is a file
     *
     * @param home the base directory
     * @param dir the new directory
     * @return dir the existing directory
     */
    private File createDirectory(File home, String dir){
        def directory = null
        if(home != null){
            directory = new File(home, dir)
        }else{
            directory = new File(dir)
        }
        if(!directory.exists()){
            if(!directory.mkdirs()){
                throw new RuntimeException("Unable to create ${directory.absolutePath}")
            }
        }else{
            if(directory.isFile()){
                throw new RuntimeException("Unable to create ${directory.absolutePath}, file exists!")
            }
        }
        return directory
    }
    /**
     * Return the servers base url
     *
     * @return baseUrl the servers base url
     */
    String baseUrl() {
        return grailsApplication.config.grails.serverURL
    }

    /**
     * Find the jip.cfg file in the specified home directory and load it
     *
     * @param homedir the jip_home directory
     * @return cfg the configuration or null
     */
    ConfigObject loadConfigurationFromHome(File homedir) {
        File config = new File(homedir, "jip.cfg")
        if(config.exists()){
            log.info("Loading configuration from ${config.absolutePath}")
            try {
                ConfigSlurper slurper = new ConfigSlurper()
                def cfg = slurper.parse(config.text)
                return cfg
            } catch (Exception e) {
                log.error("Error while loading configuration from ${config.absolutePath}", e)
            }
        }
        return null
    }

}
