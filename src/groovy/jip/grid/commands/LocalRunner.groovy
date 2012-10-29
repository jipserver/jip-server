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

import org.apache.commons.io.FileUtils

/**
 * Runs the command in a local bash shell
 *
 * @author Thasso Griebel <thasso.griebel@gmail.com>
 */
class LocalRunner implements CommandRunner{

    @Override
    CommandProcess run(String command, String workingDir, Map environment) {
        if(!workingDir) workingDir = "."
        if(!environment) environment = [:]
        def pb = new ProcessBuilder(["bash", "-c", command])
                .directory(new File(workingDir))
        environment.each {k,v->
            pb.environment().put(k, v)
        }
        def process = pb.start()
        return new LocalCommandProcess(process)
    }

    @Override
    void copy(File source, String target) {
        if(source.isDirectory())
            FileUtils.copyDirectory(source, new File(target))
        else
            FileUtils.copyFile(source, new File(target))
    }

    @Override
    void get(String source, File target) {
        copy(new File(source), target.getAbsolutePath())
    }

    class LocalCommandProcess implements CommandProcess{
        Process process

        LocalCommandProcess(Process process) {
            this.process = process
        }

        @Override
        InputStream getInputStream() {
            return process.inputStream
        }

        @Override
        InputStream getErrorStream() {
            return process.errorStream
        }

        @Override
        OutputStream getOutputStream() {
            return process.outputStream
        }

        @Override
        int waitFor() {
            return process.waitFor()
        }

        @Override
        void destroy() {
            process.destroy()
        }
    }

}
