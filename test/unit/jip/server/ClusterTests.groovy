package jip.server

import grails.test.mixin.TestFor

/**
 * See the API for {@link grails.test.mixin.domain.DomainClassUnitTestMixin} for usage instructions
 */
@TestFor(Cluster)
class ClusterTests {

    void testSimpleClusterCreate() {
        def c1 = new Cluster(name: "test1", home: "/tmp")
        assert c1.validate()
    }

    void testSettingHomeDirectory(){
        assert new Cluster(home:"/home/jipserver").home == "/home/jipserver"
        assert new Cluster(home:"/home/jipserver/////").home == "/home/jipserver"
        assert new Cluster(home:"/home/jipserver/").home == "/home/jipserver"
    }


    void testJipPath() {
        assert new Cluster(home:"/home/jipserver").jip == "/home/jipserver/jip-environment/bin/jip-wrapper.sh"
        assert new Cluster(home:"/home/jipserver/").jip == "/home/jipserver/jip-environment/bin/jip-wrapper.sh"
    }

    void testCreateDefaultCommandRunner(){
        try {
            new Cluster().createRunner()
            fail()
        } catch (NullPointerException e) {
            assert e.message == "No user specified, unable to create runner"
        }
        assert new Cluster(clusterUser: new ClusterUser(name: "testuser")).createRunner() != null
    }
}
