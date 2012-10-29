package jip.server

import org.springframework.dao.DataIntegrityViolationException
import grails.plugins.springsecurity.Secured
import jip.grid.commands.SSHRunner

@Secured(['ROLE_USER', 'ROLE_ADMIN'])
class ClusterController {

    static allowedMethods = [save: "POST", update: "POST"]
    def clusterService

    def index() {
        redirect(action: "list", params: params)
    }

    def list(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        [clusterInstanceList: Cluster.list(params), clusterInstanceTotal: Cluster.count()]
    }

    @Secured(['ROLE_ADMIN'])
    def create() {
        def cluster = new Cluster(params)
        if(!cluster.clusterUser){
            cluster.clusterUser = new ClusterUser()
        }
        [clusterInstance: cluster, clusterUser: cluster.clusterUser]
    }

    @Secured(['ROLE_ADMIN'])
    def save() {
        def clusterInstance = new Cluster(params)
        clusterInstance.clusterUser = new ClusterUser()
        clusterInstance.clusterUser.name = params.clusterUser.user
        clusterInstance.clusterUser.password = params.clusterUser.password

        if (!clusterInstance.name || clusterInstance.name.trim().isEmpty()){
            clusterInstance.errors.rejectValue("name", "You have to specify a unique name for the new cluster")
            render(view: "create", model: [clusterInstance: clusterInstance])
            return
        }
        if (Cluster.countByName(clusterInstance.name) > 0){
            clusterInstance.errors.rejectValue("name", "A cluster with that nam already exists")
            render(view: "create", model: [clusterInstance: clusterInstance])
            return
        }

        if (!clusterInstance.home){
            clusterInstance.errors.rejectValue("home", "You have to specify a home folder for the new cluster")
            render(view: "create", model: [clusterInstance: clusterInstance])
            return
        }

        if (clusterInstance.connectionType == Cluster.ConnectionType.SSH){
            if (!clusterInstance.host || clusterInstance.host.isEmpty()){
                clusterInstance.errors.rejectValue("host", "No cluster host specified")
                render(view: "create", model: [clusterInstance: clusterInstance])
                return
            }
            if (!clusterInstance.clusterUser.password || !clusterInstance.clusterUser.name){
                clusterInstance.errors.rejectValue("host", "No user/password specified")
                render(view: "create", model: [clusterInstance: clusterInstance, clusterUser: clusterInstance.clusterUser])
                return
            }
        }

        // fix the port
        if(clusterInstance.host){
            def split = clusterInstance.host.split(":")
            clusterInstance.host = split[0]
            if (split.length > 1){
                try {
                    clusterInstance.port = Integer.parseInt(split[1])
                } catch (Exception e) {
                    clusterInstance.errors.rejectValue("host", "Unable to parse port!")
                    render(view: "create", model: [clusterInstance: clusterInstance])
                    return
                }
            }
        }

        // try connection
        try {
            def runner = clusterInstance.createRunner()
            if(runner instanceof SSHRunner){
                ((SSHRunner)runner).clearCash = true
            }
            runner.run("ls", null, null)
        } catch (Exception e) {
            flash.message = "Unable to connect to cluster : ${e.message}"
            render(view: "create", model: [clusterInstance: clusterInstance, clusterUser: clusterInstance.clusterUser])
            return
        }

        if (!clusterInstance.clusterUser.save(flush: true) || !clusterInstance.save(flush: true)) {
            render(view: "create", model: [clusterInstance: clusterInstance, clusterUser: clusterInstance.clusterUser])
            return
        }
        // try to provision the cluster
        try{
            clusterService.initializeCuster(clusterInstance)
        }catch(Exception e){
            clusterInstance.delete(flush: true)
            clusterInstance.errors.reject("Unable to provision cluster: ${e.message}")
            render(view: "create", model: [clusterInstance: clusterInstance])
            return
        }

        flash.message = message(code: 'default.created.message', args: [message(code: 'cluster.label', default: 'Cluster'), clusterInstance.id])
        redirect(action: "show", id: clusterInstance.id)
    }

    def show(String id) {
        def clusterInstance = Cluster.findByName(id)
        if (!clusterInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'cluster.label', default: 'Cluster'), id])
            redirect(action: "list")
            return
        }

        [clusterInstance: clusterInstance]
    }
    @Secured(['ROLE_ADMIN'])
    def edit(String id) {
        def clusterInstance = Cluster.findByName(id)
        if (!clusterInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'cluster.label', default: 'Cluster'), id])
            redirect(action: "list")
            return
        }

        [clusterInstance: clusterInstance, clusterUser: clusterInstance.clusterUser]
    }

    @Secured(['ROLE_ADMIN'])
    def provision() {
        def clusterInstance = Cluster.findByName(params.id)
        if (!clusterInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'cluster.label', default: 'Cluster'), params.id])
            redirect(action: "list")
            return
        }
        clusterInstance.provisioned = false
        clusterService.initializeCuster(clusterInstance)
        if (!clusterInstance.provisioned){
            flash.message = "Unable to provision cluster : ${clusterInstance.name}"
        }else{
            flash.message = "Cluster provisioned succesfully"
        }
        redirect(action: "show", id: params.id)
    }

    @Secured(['ROLE_ADMIN'])
    def update(Long id, Long version) {
        def clusterInstance = Cluster.get(id)
        if (!clusterInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'cluster.label', default: 'Cluster'), id])
            redirect(action: "list")
            return
        }

        if (version != null) {
            if (clusterInstance.version > version) {
                clusterInstance.errors.rejectValue("version", "default.optimistic.locking.failure",
                        [message(code: 'cluster.label', default: 'Cluster')] as Object[],
                        "Another user has updated this Cluster while you were editing")
                render(view: "edit", model: [clusterInstance: clusterInstance])
                return
            }
        }

        clusterInstance.properties = params
        if (clusterInstance.clusterUser == null)
            clusterInstance.clusterUser = new ClusterUser()
        clusterInstance.clusterUser.password = params.password
        clusterInstance.clusterUser.name = params.user


        // try connection
        try {
            def runner = clusterInstance.createRunner()
            if(runner instanceof SSHRunner){
                ((SSHRunner)runner).clearCash = true
            }
            runner.run("ls", null, null)
        } catch (Exception e) {
            flash.message = "Unable to connect to cluster : ${e.message}"
            render(view: "edit", model: [clusterInstance: clusterInstance, clusterUser: clusterInstance.clusterUser])
            return
        }


        if (!clusterInstance.clusterUser.save(flush: true) || !clusterInstance.save(flush: true)) {
            render(view: "edit", model: [clusterInstance: clusterInstance, clusterUser: clusterInstance.clusterUser])
            return
        }

        flash.message = message(code: 'default.updated.message', args: [message(code: 'cluster.label', default: 'Cluster'), clusterInstance.id])
        redirect(action: "show", id: clusterInstance.id)
    }
    @Secured(['ROLE_ADMIN'])
    def delete(Long id) {
        def clusterInstance = Cluster.get(id)
        println("Deleting ${clusterInstance}")
        if (!clusterInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'cluster.label', default: 'Cluster'), id])
            redirect(action: "list")
            return
        }

        try {
            clusterInstance.delete(flush: true)
            flash.message = message(code: 'default.deleted.message', args: [message(code: 'cluster.label', default: 'Cluster'), id])
            redirect(action: "list")
        }
        catch (DataIntegrityViolationException e) {
            flash.message = message(code: 'default.not.deleted.message', args: [message(code: 'cluster.label', default: 'Cluster'), id])
            redirect(action: "show", id: id)
        }
    }
}
