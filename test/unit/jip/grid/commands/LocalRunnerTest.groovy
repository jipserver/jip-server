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

package jip.grid.commands

class LocalRunnerTest extends GroovyTestCase {

    public void testExecutionDirect(){
        def hostname = new LocalRunner().run("hostname", null, null).inputStream.text
        assert hostname != null
        def testfile = File.createTempFile("localrunner", "test")
        testfile.deleteOnExit()
        def process = new LocalRunner().run("hostname > ${testfile.absolutePath}", null, null)
        assert process.waitFor() == 0
        assert hostname == testfile.text
    }



}
