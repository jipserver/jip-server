package jip.server



import org.junit.*
import grails.test.mixin.*

@TestFor(ClusterController)
@Mock(Cluster)
class ClusterControllerTests {

    def populateValidParams(params) {
        assert params != null
        // TODO: Populate valid properties like...
        //params["name"] = 'someValidName'
    }

    void testIndex() {
        controller.index()
        assert "/cluster/list" == response.redirectedUrl
    }

    void testList() {

        def model = controller.list()

        assert model.clusterInstanceList.size() == 0
        assert model.clusterInstanceTotal == 0
    }

    void testCreate() {
        def model = controller.create()

        assert model.clusterInstance != null
    }

    void testSave() {
        controller.save()

        assert model.clusterInstance != null
        assert view == '/cluster/create'

        response.reset()

        populateValidParams(params)
        controller.save()

        assert response.redirectedUrl == '/cluster/show/1'
        assert controller.flash.message != null
        assert Cluster.count() == 1
    }

    void testShow() {
        controller.show()

        assert flash.message != null
        assert response.redirectedUrl == '/cluster/list'

        populateValidParams(params)
        def cluster = new Cluster(params)

        assert cluster.save() != null

        params.id = cluster.id

        def model = controller.show()

        assert model.clusterInstance == cluster
    }

    void testEdit() {
        controller.edit()

        assert flash.message != null
        assert response.redirectedUrl == '/cluster/list'

        populateValidParams(params)
        def cluster = new Cluster(params)

        assert cluster.save() != null

        params.id = cluster.id

        def model = controller.edit()

        assert model.clusterInstance == cluster
    }

    void testUpdate() {
        controller.update()

        assert flash.message != null
        assert response.redirectedUrl == '/cluster/list'

        response.reset()

        populateValidParams(params)
        def cluster = new Cluster(params)

        assert cluster.save() != null

        // test invalid parameters in update
        params.id = cluster.id
        //TODO: add invalid values to params object

        controller.update()

        assert view == "/cluster/edit"
        assert model.clusterInstance != null

        cluster.clearErrors()

        populateValidParams(params)
        controller.update()

        assert response.redirectedUrl == "/cluster/show/$cluster.id"
        assert flash.message != null

        //test outdated version number
        response.reset()
        cluster.clearErrors()

        populateValidParams(params)
        params.id = cluster.id
        params.version = -1
        controller.update()

        assert view == "/cluster/edit"
        assert model.clusterInstance != null
        assert model.clusterInstance.errors.getFieldError('version')
        assert flash.message != null
    }

    void testDelete() {
        controller.delete()
        assert flash.message != null
        assert response.redirectedUrl == '/cluster/list'

        response.reset()

        populateValidParams(params)
        def cluster = new Cluster(params)

        assert cluster.save() != null
        assert Cluster.count() == 1

        params.id = cluster.id

        controller.delete()

        assert Cluster.count() == 0
        assert Cluster.get(cluster.id) == null
        assert response.redirectedUrl == '/cluster/list'
    }
}
