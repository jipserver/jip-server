package jip.server

import grails.test.mixin.TestFor
import jip.utils.KeyUtils

/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
@TestFor(ClusterService)
class ClusterServiceTests {

    void testKeyGeneration() {
        def keys = KeyUtils.generate()
        assert keys != null
        assert keys['private'] != null
        assert keys['public'] != null
        assert keys.size() == 2
    }
}
