/*
 * Copyright (C) 2012 Thasso Griebel
 *
 * This file is part of JIP.
 *
 * JIP is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JIP is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with JIP.  If not, see <http://www.gnu.org/licenses/>.
 */

package jip.grid.local

import jip.grid.commands.LocalRunner
import jip.server.Job

/**
 * 
 * @author Thasso Griebel <thasso.griebel@gmail.com>
 */
class LocalGridTest extends GroovyTestCase{

    public void testJobSubmission(){
        def localRunner = new LocalRunner()
        def localGrid = new LocalGrid("local", [:])
        assert localGrid.list(localRunner) != null
        assert localGrid.list(localRunner).size() == 0

        def result = File.createTempFile("result", ".txt")
        result.deleteOnExit()

        def job = new Job()
        job.id = 1
        job.name = "Local test job"
        job.command = ["bash", "-c", "hostname > ${result.absolutePath}"]
        localGrid.submit([job], localRunner)
        assert job.clusterId == "1"
        assert job.cluster  == "local"
        def joblist = localGrid.list(localRunner)
        while(joblist.size() > 0){
            println "Waiting for job to finish ${joblist}"
            Thread.sleep(100)
            joblist = localGrid.list(localRunner)
        }
        assert result.exists()
        assert localGrid.states.size() == 0
        assert localGrid.futures.size() == 0
    }


}
