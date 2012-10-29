eventCompileEnd = { kind->
    println "Packaging python jip bundle"
    def sourceDir = "${new File('').absolutePath}/src/python"
    ant.zip(destfile:"${classesDir}/jip-client.zip"){
        fileset(dir:"${sourceDir}")
    }
}