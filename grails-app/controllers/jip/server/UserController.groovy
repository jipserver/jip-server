package jip.server

import org.springframework.dao.DataIntegrityViolationException
import grails.plugins.springsecurity.Secured
import jip.grid.commands.JipRunner
import jip.grid.commands.SSHRunner

@Secured(['ROLE_ADMIN'])
class UserController {

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]
    def springSecurityService
    def clusterService

    def grailsApplication

    def index() {
        redirect(action: "list", params: params)
    }

    def list(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        [userInstanceList: User.list(params), userInstanceTotal: User.count()]
    }

    def create() {
        [userInstance: new User(params)]
    }

    def save() {
        // Role userRole = Role.findByAuthority("ROLE_USER")
        def userInstance = new User(
                username: params.username,
                password: params.pwd,
                email: params.email,
                enabled: true)

        if(!params.pwd){
            flash.message = "No password specified"
            render(view: "create", model: [userInstance: userInstance, pwd: params.pwd, pwd_repeat:params.pwd_repeat])
            return
        }
        if(params.pwd != params.pwd_repeat){
            flash.message = "Password and repeat do not match"
            render(view: "create", model: [userInstance: userInstance, pwd: "", pwd_repeat:""])
            return
        }

        userInstance.encodePassword()
        if (!userInstance.save(flush: true)) {
            render(view: "create", model: [userInstance: userInstance])
            return
        }

        flash.message = message(code: 'default.created.message', args: [message(code: 'user.label', default: 'User'), userInstance.id])
        redirect(action: "show", id: userInstance.id)
    }

    @Secured(['ROLE_USER', 'ROLE_ADMIN'])
    def show(String id) {
        def userInstance = User.get(id)

        if (!userInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'user.label', default: 'User'), id])
            redirect(action: "list")
            return
        }
        User currentUser = (User)springSecurityService.currentUser
        if(currentUser == null || (!currentUser.hasRole("ROLE_ADMIN") && id != currentUser.id)){
            redirect(url: "/")
            return
        }
        
        def cluster = []
        for (Cluster c : Cluster.findAll().sort{it.name}) {
            def cl = [:]
            cl['name'] = c.name
            cl['status'] = c.connectionType == Cluster.ConnectionType.Local ? "Local" : "Unknown"
            cl['authorize'] = false
            if(c.connectionType == Cluster.ConnectionType.SSH ){
                if (userInstance.clusterUser == null || userInstance.clusterUser.get(c.name) == null){
                    cl['authorize'] = true
                }else{
                    cl['status'] = "Authorized"
                }
            }
            cluster << cl
        }


        [userInstance: userInstance, cluster: cluster]
    }

    @Secured(['ROLE_USER', 'ROLE_ADMIN'])
    def updateProfileDialog(){
        log.error("asdadadasdad")
        def userInstance = User.get(params.user)
        if (!userInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'user.label', default: 'User'), id])
            redirect(action: "list")
            return
        }
        User currentUser = (User)springSecurityService.currentUser
        if(currentUser == null || (!currentUser.hasRole("ROLE_ADMIN") && userInstance.id != currentUser.id)){
            redirect(url: "/")
            return
        }

        Cluster cluster = Cluster.findByName(params.cluster)
        if(!cluster){
            flash.message = "Cluster not found!"
        }
        render template: "updateProfileDialog"
        return
//        // find cluster user
//        if(userInstance.clusterUser == null){
//            userInstance.clusterUser = new HashMap<String, Long>()
//        }
//        Long clusterUserId = userInstance.clusterUser.get(cluster.name)
//        ClusterUser clusterUser = null
//        if(clusterUserId != null){
//            clusterUser = ClusterUser.get(clusterUserId)
//        }
//        if(clusterUser == null){
//            clusterUserId = null
//            clusterUser = new ClusterUser(name: params.username, password: params.password)
//            clusterUser.save(flush: true)
//        }
//
//        // save user
//        if(clusterUserId == null){
//            clusterUserId = clusterUser.id
//            userInstance.clusterUser.put(cluster.name, clusterUserId)
//            userInstance.save(flush:true)
//        }
    }

    @Secured(['ROLE_USER', 'ROLE_ADMIN'])
    def authorize() {
        def userInstance = User.get(params.user)
        if (!userInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'user.label', default: 'User'), id])
            redirect(action: "list")
            return
        }
        User currentUser = (User)springSecurityService.currentUser
        if(currentUser == null || (!currentUser.hasRole("ROLE_ADMIN") && userInstance.id != currentUser.id)){
            redirect(url: "/")
            return
        }

        Cluster cluster = Cluster.findByName(params.cluster)
        if(!cluster){
            flash.message = "Cluster not found!"
        }

        if(!params.username || !params.password){
            flash.message = "No username or password specified"
        }else{
            // try to connect
            try {
                // find cluster user
                if(userInstance.clusterUser == null){
                    userInstance.clusterUser = new HashMap<String, Long>()
                }
                Long clusterUserId = userInstance.clusterUser.get(cluster.name)
                ClusterUser clusterUser = null
                if(clusterUserId != null){
                    clusterUser = ClusterUser.get(clusterUserId)
                }
                if(clusterUser == null){
                    clusterUserId = null
                    clusterUser = new ClusterUser(name: params.username, password: params.password)
                    clusterUser.save(flush: true)
                }
                clusterService.authorizeUser(cluster, clusterUser)
                // save user
                if(clusterUserId == null){
                    clusterUserId = clusterUser.id
                    userInstance.clusterUser.put(cluster.name, clusterUserId)
                    userInstance.save(flush:true)
                }
                flash.message = "Access granted"
            } catch (Exception e) {
                flash.message = "Error while create keys! ${e.message}"
            }
        }
        redirect(action: 'show', params: [id:userInstance.id])
    }

    @Secured(['ROLE_USER', 'ROLE_ADMIN'])
    def validateAuthorization() {
        def userInstance = User.get(params.user)
        if (!userInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'user.label', default: 'User'), id])
            redirect(action: "list")
            return
        }
        User currentUser = (User)springSecurityService.currentUser
        if(currentUser == null || (!currentUser.hasRole("ROLE_ADMIN") && userInstance.id != currentUser.id)){
            redirect(url: "/")
            return
        }

        Cluster cluster = Cluster.findByName(params.cluster)
        if(!cluster){
            flash.message = "Cluster not found!"
        }

        if(userInstance.clusterUser == null){
            flash.message = "Authorization invalid"
            redirect(action: 'show', params: [id:userInstance.id])
            return
        }
        Long clusterUserId = userInstance.clusterUser.get(cluster.name)
        if(clusterUserId == null){
            flash.message = "Authorization invalid"
            redirect(action: 'show', params: [id:userInstance.id])
            return
        }

        ClusterUser clusterUser = ClusterUser.get(clusterUserId)
        def runner = cluster.createRunner(clusterUser)
        if(runner instanceof SSHRunner){
            ((SSHRunner)runner).clearCash = true
        }
        if(clusterUser == null ){
            userInstance.clusterUser.remove(cluster.name)
            userInstance.clusterUser = userInstance.clusterUser
            userInstance.save(flush: true)
            flash.message = "Authorization invalid"
            redirect(action: 'show', params: [id:userInstance.id])
            return
        }

        try {
            if(runner.run("ls", null, null).waitFor() != 0){
                throw new RuntimeException("Unable to connect to host")
            }
        } catch (RuntimeException e) {
            userInstance.clusterUser.remove(cluster.name)
            userInstance.clusterUser = userInstance.clusterUser
            userInstance.save(flush: true)
            flash.message = "Authorization invalid"
            redirect(action: 'show', params: [id:userInstance.id])
            return
        }

        flash.message = "Authozation is valid"
        redirect(action: 'show', params: [id:userInstance.id])
    }

    @Secured(['ROLE_USER', 'ROLE_ADMIN'])
    def edit(String id) {
        def userInstance = User.get(id)
        User currentUser = (User)springSecurityService.currentUser
        if(currentUser == null || (!currentUser.hasRole("ROLE_ADMIN") && id != currentUser.id)){
            redirect(url: "/")
            return
        }

        if (!userInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'user.label', default: 'User'), id])
            redirect(action: "list")
            return
        }

        [userInstance: userInstance, pwd: "", pwd_repeat: ""]
    }

    @Secured(['ROLE_USER', 'ROLE_ADMIN'])
    def update(String id, Long version) {
        def userInstance = User.get(id)

        if (!userInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'user.label', default: 'User'), id])
            redirect(action: "list")
            return
        }

        User currentUser = (User)springSecurityService.currentUser
        if(currentUser == null || (!currentUser.hasRole("ROLE_ADMIN") && id != currentUser.id)){
            redirect(url: "/")
            return
        }


        if (version != null) {
            if (userInstance.version > version) {
                userInstance.errors.rejectValue("version", "default.optimistic.locking.failure",
                        [message(code: 'user.label', default: 'User')] as Object[],
                        "Another user has updated this User while you were editing")
                render(view: "edit", model: [userInstance: userInstance])
                return
            }
        }

        userInstance.properties = params

        if (params.password){
            userInstance.encodePassword()
        }

        if (!userInstance.save(flush: true)) {
            render(view: "edit", model: [userInstance: userInstance])
            return
        }

        flash.message = message(code: 'default.updated.message', args: [message(code: 'user.label', default: 'User'), userInstance.id])
        redirect(action: "show", id: userInstance.id)
    }

    def delete(Long id) {
        def userInstance = User.get(id)
        if (!userInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'user.label', default: 'User'), id])
            redirect(action: "list")
            return
        }

        try {
            userInstance.delete(flush: true)
            flash.message = message(code: 'default.deleted.message', args: [message(code: 'user.label', default: 'User'), id])
            redirect(action: "list")
        }
        catch (DataIntegrityViolationException e) {
            flash.message = message(code: 'default.not.deleted.message', args: [message(code: 'user.label', default: 'User'), id])
            redirect(action: "show", id: id)
        }
    }
}
