package jip.server

import grails.test.mixin.TestFor

/**
 * See the API for {@link grails.test.mixin.domain.DomainClassUnitTestMixin} for usage instructions
 */
@TestFor(Job)
class JobTests {

    void testJobCreationAndInitialValues() {
        Job job = new Job()
        assert job.createDate != null
        assert job.state == State.Queued
    }

    void testInitialJobValidation(){
        Job job = new Job()
        assert !job.validate()

        def errors = job.errors
        assert errors.errorCount == 2
        assert errors.fieldErrorCount == 2
        assert errors.getFieldError("command") != null
        assert errors.getFieldError("command").rejectedValue == null
        assert errors.getFieldError("cluster") != null
        assert errors.getFieldError("cluster").rejectedValue == null
    }


}
