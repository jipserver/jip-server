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

package jip.grid.commands;

import java.io.File;
import java.util.Map;

/**
 * Execute external shell commands
 *
 * @author Thasso Griebel <thasso.griebel@gmail.com>
 */
public interface CommandRunner {
    /**
     * Run the given command in the specified working directory
     *
     * @param command the command
     * @param workingDir the working directory (null permitted)
     * @param environment the environment (null permitted)
     * @return process the command process
     */
    CommandProcess run(String command, String workingDir, Map environment);

    /**
     * Copy local file to given target
     *
     * @param source the source
     * @param target the target
     */
    void copy(File source, String target);

    /**
     * Copy remote file to local file
     *
     * @param source the source
     * @param target the target
     */
    void get(String source, File target);

}
