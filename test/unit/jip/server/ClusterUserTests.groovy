package jip.server



import grails.test.mixin.*

/**
 * See the API for {@link grails.test.mixin.domain.DomainClassUnitTestMixin} for usage instructions
 */
@TestFor(ClusterUser)
class ClusterUserTests {

    void testConstraints() {
        assert new ClusterUser().validate() == false
        assert new ClusterUser(name: "Test").validate() == true
    }

    void testCreateRunner(){
        try {
            new ClusterUser(name:"user").createRunner(null)
            fail()
        } catch (NullPointerException e) {
        }

        Cluster localhost = new Cluster(name:"local", connectionType: Cluster.ConnectionType.Local)
        assert new ClusterUser(name: "unknown").createRunner(localhost) != null

        // test that connection to ssh host without hostname fails
        try {
            Cluster host = new Cluster(name:"local", connectionType: Cluster.ConnectionType.SSH)
            new ClusterUser(name: "unknown").createRunner(host)
            fail()
        } catch (RuntimeException e) {
        }


    }
}
