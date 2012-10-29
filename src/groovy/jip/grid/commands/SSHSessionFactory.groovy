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

import net.schmizz.sshj.SSHClient
import net.schmizz.sshj.connection.channel.direct.Session
import net.schmizz.sshj.sftp.SFTPClient
import net.schmizz.sshj.transport.verification.PromiscuousVerifier
import net.schmizz.sshj.userauth.keyprovider.KeyProvider
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class SSHSessionFactory {
    /**
     * The ssh session factory
     */
    private static SSHSessionFactory instance;
    /**
     * Logger
     */
    private Logger log = LoggerFactory.getLogger(SSHSessionFactory.class)

    /**
     * Maps from host name to username to client
     */
    private final Map<String, Map<String, SSHClient>> clients = [:]

    /**
     * True if shutdown hook is added
     */
    private boolean addedHook


    public static SSHSessionFactory getInstance(){
        if(instance == null) instance = new SSHSessionFactory()
        return instance
    }

    private SSHSessionFactory(){
    }

    Session createSession(Map cfg){
        SSHClient client = client(cfg)
        if(!addedHook && client){
            addedHook = true
            System.addShutdownHook {
                clients.each{host, users->
                    users.each {username, ssh->
                        ssh.disconnect()
                    }
                }
            }
        }
        if(!client.isAuthenticated() || !client.isConnected()){
            connectClient(client, cfg)
        }
        return client.startSession()

    }

    private void connectClient(SSHClient client, Map cfg) {
        client.connect((String) cfg.host, cfg.port ? cfg.port : 22)
        if (cfg.password && !cfg.keyFile) client.authPassword(cfg.username, cfg.password)
        else if (cfg.keyFile) {
            KeyProvider provider = null
            if (cfg.password && !cfg.password.isEmpty()) {
                provider = client.loadKeys(cfg.keyFile, cfg.password.toString().getChars())
            } else {
                provider = client.loadKeys(cfg.keyFile)
            }
            client.authPublickey(cfg.username, [provider])
        } else {
            client.authPublickey(cfg.username)
        }
    }

    SFTPClient createSFPTClient(Map cfg){
        SSHClient client = client(cfg)
        if(!addedHook && client){
            addedHook = true
            System.addShutdownHook {
                clients.each{host, users->
                    users.each {username, ssh->
                        ssh.disconnect()
                    }
                }
            }
        }
        if(!client.isAuthenticated() || !client.isConnected()){
            connectClient(client, cfg)
        }
        return client.newSFTPClient()

    }

    private SSHClient client(Map cfg ) {
        def users = clients[cfg.host]
        if(!users){
            users = [:]
            if(!cfg.clean){
                clients[cfg.host] = users
            }
        }
        SSHClient client = users[cfg.username]
        if(!client || cfg.clean){
            log.info("Creating SSH Client to connecto to ${cfg.host}" )
            client = new SSHClient()
            client.loadKnownHosts()
            client.addHostKeyVerifier(new PromiscuousVerifier())
            if(!cfg.clean){
                users[cfg.username] = client
            }
            int timeout = 5000;
            if(cfg.containsKey("timeout")){
                timeout = cfg["timeout"]
            }
            //client.setTimeout(timeout)
            client.setConnectTimeout(timeout)
        }
        return client
    }
}
