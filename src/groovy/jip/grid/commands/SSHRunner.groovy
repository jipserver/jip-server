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

import groovy.transform.TupleConstructor
import net.schmizz.sshj.connection.channel.direct.Session
import net.schmizz.sshj.connection.channel.direct.Signal
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import net.schmizz.sshj.userauth.UserAuthException

/**
 * Runs the command in a remote bash shell.
 * NOTE that we open a ssh connection and you will get the full output
 * of the ssh process including any motd messages printed when you login to the
 * remote host manually.
 *
 *
 * @author Thasso Griebel <thasso.griebel@gmail.com>
 */
class SSHRunner implements CommandRunner{
    /**
     * The logger
     */
    private Logger log = LoggerFactory.getLogger(SSHRunner.class)
    /**
     * The remote host to connect to
     */
    String host
    /**
     * The remote port
     */
    int port = 22
    /**
     * The user name
     */
    String username = System.getProperty("user.name")
    /**
     * The password
     */
    String password
    /**
     * Path to a keyFile
     */
    String keyFile

    /**
     * Default timeout
     */
    int timeout = 5000
    /**
     * If true a non cached session will be used!
     */
    boolean clearCash = false

    SSHRunner(String host){
        this([host: host])
    }

    SSHRunner(String host, String password){
        this([host: host, password: password])
    }

    SSHRunner(Map attr) {
        if(attr == null) attr = [:]
        this.host = attr.host ? attr.host : this.host
        this.port = attr.port ? attr.port : this.port
        this.username = attr.username ? attr.username : this.username
        this.password = attr.password ? attr.password : this.password
        this.keyFile = attr.keyFile ? attr.keyFile : this.keyFile

    }

    SSHRunner(String host, int port, String username, String password, String keyFile) {
        this.host = host
        this.port = port
        this.username = username ? username : this.username
        this.password = password
        this.keyFile = keyFile
    }

    @Override
    CommandProcess run(String command, String workingDir, Map env) {

        try {
            if(!workingDir) workingDir = "."
            if(!env) env = [:]
            final Session session = SSHSessionFactory.getInstance()
                    .createSession(host: host, port: port, username: username, password: password, keyFile: keyFile, timeout: timeout, clean:clearCash)
            def shell = session.startShell()
            def prefix = ""
            if (workingDir != "."){
                prefix += "cd ${workingDir};"
            }
            if(env.size() > 0){
                prefix += "${env.collect{k,v-> "${k}=\"${v}\""}.join(" ")};"
            }

            log.info("Running command '${command}' @ ${host}")
            shell.outputStream.write("${prefix}${command};exit \$?\n".toString().bytes)
            shell.outputStream.flush()
            return new SSHCommandProcess(command:shell, session:session);
        } catch (UserAuthException e) {
            throw new RuntimeException("Authorization failed")
        }
    }

    @Override
    void copy(File source, String target) {
        log.info("Transfering file ${source.absolutePath} to ${target} @ ${host}")
        def sftp = SSHSessionFactory.getInstance().createSFPTClient(host: host, port: port, username: username, password: password, keyFile: keyFile)
        if(source.isDirectory()) sftp.mkdirs(target)
        sftp.put(source.getAbsolutePath(), target)
        sftp.close()
    }

    @Override
    void get(String source, File target) {
        log.info("Transfering remote file ${source} to ${target.absolute} @ ${host}")
        def sftp = SSHSessionFactory.getInstance().createSFPTClient(host: host, port: port, username: username, password: password, keyFile: keyFile)
        sftp.get(source, target.absolutePath)
        sftp.close()
    }

    /**
     * Wraps around the ssh command
     */
    @TupleConstructor class SSHCommandProcess implements CommandProcess{
        Session.Shell command
        Session session
        @Override
        InputStream getInputStream() {
            return command.inputStream
        }

        @Override
        InputStream getErrorStream() {
            return command.errorStream
        }

        @Override
        OutputStream getOutputStream() {
            return command.outputStream
        }

        @Override
        int waitFor() {
            command.join()
            command.close()
            session.close()
            return command.exitStatus
        }

        @Override
        void destroy() {
            command.signal(Signal.QUIT)
            command.signal(Signal.KILL)
//            command.handle(Message.CHANNEL_CLOSE, null)
            command.close()
            session.close()

        }
    }
}
