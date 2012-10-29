
includeTargets << grailsScript("_GrailsArgParsing")
includeTargets << grailsScript('_GrailsBootstrap')


target(main: "Load demo cluster implementations") {
    def ips = [
            'local': '10.0.0.50',
            'sge': '10.0.0.40',
            'lsf': '10.0.0.30',
            'slurm': '10.0.0.20',
    ]

    if(!argsMap.params || argsMap.params.size != 1 || !ips.keySet().contains(argsMap.params[0])){
        println("""Please specify the demo cluster you want to load.
The corresponding vagrant VM should be up and running. The JIP server uses jipserver/secret
to log in and you can authorize a jipuser/secret to send jobs. Available configuraitons:

   - local
   - slurm
   - sge
   - lsf

Usage: demo-clusters <cluster>
""")
        return
    }

    depends checkVersion, configureProxy, bootstrap

    def persistenceInterceptor = appCtx.containsBean('persistenceInterceptor') ? appCtx.persistenceInterceptor : null
    persistenceInterceptor?.init()
    try {
        def shell = new GroovyShell(classLoader, new Binding(ctx: appCtx, grailsApplication: grailsApp, argsMap:argsMap, ips:ips))
        shell.evaluate '''
        import jip.server.Cluster
        import jip.server.ClusterUser
        Cluster cluster = Cluster.findByName(argsMap.params[0])
        if(cluster == null){
            cluster = new Cluster()
        }
        cluster.name = argsMap.params[0]
        cluster.host = ips[cluster.name]
        cluster.home = "/home/jipserver"
        if(cluster.clusterUser == null){
            cluster.clusterUser = new ClusterUser()
        }
        cluster.clusterUser.name = "jipserver"
        cluster.clusterUser.password = "secret"
        cluster.connectionType = Cluster.ConnectionType.SSH
        cluster.clusterType = cluster.name
        cluster.serverUrl = "http://${InetAddress.getLocalHost().getHostAddress()}:8080/jip-server"
        cluster.clusterUser.save(flush: true)
        cluster.save(flush: true)
        '''
    } finally {
        persistenceInterceptor?.flush()
        persistenceInterceptor?.destroy()
    }

    println("Cluster ${argsMap.params[0]} loaded")

}

setDefaultTarget(main)
