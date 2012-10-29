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

import org.junit.Before
import org.junit.Test

/**
 * 
 * @author Thasso Griebel <thasso.griebel@gmail.com>
 */
class SSHRunnerTest {

    private static String hostname = "10.0.0.50"
    private static String username = "jipuser"
    private static String password = "secret"

    @Before
    public void setUp(){
        //org.junit.Assume.assumeTrue(getPwd() != null);
    }
    @Test
    public void emptyTest(){

    }

//    @Test
//    public void copyViaSSH(){
//        if(!getPwd()) return
//        def runner = new SSHRunner(password: getPwd(), host: hostname, username: username)
//
//        def target = File.createTempFile("localrunner", "target")
//        target.delete();
//
//        def testfile = File.createTempFile("localrunner", "test")
//        testfile.delete()
//        testfile.mkdirs()
//        new File(testfile, "test.txt").createNewFile()
//
//        runner.copy(testfile, target.getAbsolutePath()+"/uber-duber")
////        assertTrue(target.exists())
////        assertTrue(new File(target, "uber-duber").exists())
////        assertTrue(new File(target, "uber-duber/"+testfile.getName()).exists())
////        assertTrue(new File(target, "uber-duber/"+testfile.getName()+"/test.txt").exists())
////        FileUtils.deleteDirectory(testfile)
////        FileUtils.deleteDirectory(target)
//    }
//    @Test
//    public void testExecutionViaSSH(){
//        if(!getPwd()) return
//        def runner = new SSHRunner(password: getPwd(), host: hostname, username: username)
//        def run = runner.run("hostname", null, null)
//        def hostname = run.inputStream.text
//        assert hostname != null
//        def testfile = File.createTempFile("localrunner", "test")
//        testfile.deleteOnExit()
//        def process = runner.run("hostname > ${testfile.absolutePath}", null, null)
//        assert process.waitFor() == 0
//        assert hostname == testfile.text
//    }
//    @Test
//    public void testSSHRunnerCommandExitStatus(){
//        if(!getPwd()) return
//        def runner = new SSHRunner(password: getPwd(), host: hostname, username: username)
//        assert runner.run("ls", null, null).waitFor() == 0
//        assert runner.run("unknown command to this shell", null, null).waitFor() == 127
//    }
////    @Test
////    public void testSSHConnectionWithNoPasswordKey(){
////        def runner = new SSHRunner("localhost", 22, null, null, getClass().getResource("/np_key").getFile())
////        assert runner.run("ls", null, null).waitFor() == 0
////    }
////    @Test
////    public void testSSHConnectionWithSecretPasswordKey(){
////        def runner = new SSHRunner(hostname, 22, null, 'secret', getClass().getResource("/secret_key").getFile())
////        assert runner.run("ls", null, null).waitFor() == 0
////    }
////
////    public void testKillViaSSH(){
////        if(!getPwd()) return
////        def runner = new SSHRunner(password: getPwd(), host: "localhost")
////        def p = runner.run("sleep 2498; echo hello", null, null);
////        p.destroy()
////        Thread.sleep(2000);
////        def check = runner.run("ps -eo pid,args | grep 'sleep 2498'", null, null);
////        String output = check.inputStream.text
////        println "${output}"
////        assertTrue(output.isEmpty())
////    }



    static getPwd(){
        def pwd = System.getenv("ssh_pw")
        if (!pwd){
            pwd = System.getProperty("ssh_pw", null)
        }
        if(pwd == null){
            return password
        }
        return pwd
    }

}
