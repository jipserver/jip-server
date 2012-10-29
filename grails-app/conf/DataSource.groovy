    // environment specific settings
environments {
    development {
        grails{
            mongo {
                host = "localhost"
                port = 27017
                databaseName = "jip_dev"
                options{
                    autoConnectRetry = true
                }
            }
        }
    }
    test {
        grails{
            mongo {
                host = "localhost"
                port = 27017
                databaseName = "jip_tests"
                options{
                    autoConnectRetry = true
                }
            }
        }
    }
    production {
        grails{
            mongo {
                host = "localhost"
                port = 27017
                databaseName = "jip"
                options{
                    autoConnectRetry = true
                }
            }
        }
    }
}
