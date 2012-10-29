import org.apache.activemq.ActiveMQConnectionFactory
import org.springframework.jms.connection.SingleConnectionFactory

beans = {
    springConfig.addAlias "persistenceInterceptor", "mongoPersistenceInterceptor"
    jmsConnectionFactory(SingleConnectionFactory) { bean ->
        targetConnectionFactory = { ActiveMQConnectionFactory cf ->
            brokerURL = 'vm://localhost'
        }
    }
}
