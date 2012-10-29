package jip.server

import org.junit.Test

import static junit.framework.Assert.assertTrue

class JobIntegrationTests {

    @Test
    public void testJobCreation(){
        def job = new Job()
        job.command = ["ls", "-la"]
        job.cluster = "sshnode"
        assertTrue(job.validate())
        Job stored = job.save(flush: true)
        assertTrue(stored.id > 0)
    }
}
