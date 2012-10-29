grails.servlet.version = "2.5" // Change depending on target container compliance (2.5 or 3.0)
grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"
grails.project.target.level = 1.6
grails.project.source.level = 1.6
//grails.project.war.file = "target/${appName}-${appVersion}.war"

grails.project.dependency.resolution = {
    // inherit Grails' default dependencies
    inherits("global") {
        // specify dependency exclusions here; for example, uncomment this to disable ehcache:
        // excludes 'ehcache'
    }
    log "warn" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'
    checksums true // Whether to verify checksums on resolve

    repositories {
        inherits true // Whether to inherit repository definitions from plugins

        grailsPlugins()
        grailsHome()
        grailsCentral()

        mavenLocal()
        mavenCentral()

        // uncomment these (or add new ones) to enable remote dependency resolution from public Maven repositories
        mavenRepo "http://barnaserver.com/artifactory/repo"
        //mavenRepo "http://snapshots.repository.codehaus.org"
        //mavenRepo "http://repository.codehaus.org"
        //mavenRepo "http://download.java.net/maven/2/"
        //mavenRepo "http://repository.jboss.com/maven2/"
    }
    dependencies {
        // specify dependencies here under either 'build', 'compile', 'runtime', 'test' or 'provided' scopes eg.
        compile 'jgrapht:jgrapht:0.8.3',
                'com.google.guava:guava:13.0',
                'net.schmizz:sshj:0.7.0',
                'org.bouncycastle:bcprov-jdk16:1.46',
                'commons-io:commons-io:2.3',
                'org.apache.activemq:activemq-core:5.3.0'
        runtime 'org.springframework:org.springframework.test:3.0.3.RELEASE'
    //test 'junit:junit-dep:4.10'
//        runtime 'cat.cnag:jip-grid:1.0-SNAPSHOT',
//                'cat.cnag:jip-graphs:1.0-SNAPSHOT'
//
        // runtime 'mysql:mysql-connector-java:5.1.20'
    }

    plugins {
        //runtime ":hibernate:$grailsVersion"
        runtime ":jquery:1.7.2"
        runtime ":resources:1.1.6"

        // Uncomment these (or add new ones) to enable additional resources capabilities
        //runtime ":zipped-resources:1.0"
        //runtime ":cached-resources:1.0"
        //runtime ":yui-minify-resources:0.1.4"

        build ":tomcat:$grailsVersion"

        //runtime ":database-migration:1.1"

        compile ':cache:1.0.0'
    }
}
