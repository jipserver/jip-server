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

package jip.grid;

import jip.grid.commands.CommandRunner;
import jip.grid.model.GridJobState;
import jip.server.Job;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * Grid base interface to add support for external grid systems.
 *
 * @author Thasso Griebel <thasso.griebel@gmail.com>
 */
public interface Grid {
    /**
     * Cancel the given jobs
     *
     * @param jobs the jobs
     * @param runner the runner
     * @throws Exception in case of an error
     */
    void cancel(List<Job> jobs, CommandRunner runner) throws Exception;

    /**
     * Submits the given list of jobs
     *
     * @param runner the runner
     * @param jobs the jobs to be submitted
     * @throws Exception
     */
    void submit(List<Job> jobs, CommandRunner runner) throws Exception;

    /**
     * Query the grid and returns a map from the clusterId to the current status
     *
     * @param runner the runner
     * @return jobs currently queued or running jobs
     * @throws Exception in case the list could not be fetched
     */
    Map<String, GridJobState> list(CommandRunner runner) throws Exception;

    /**
     * Copy the given local file (might be a file or a directory)
     * to the cluster.
     *
     * @param source the source
     * @param target the target
     * @param runner the runner
     * @throws Exception if the copy failed
     */
    void copy(File source, String target, CommandRunner runner) throws Exception;

}
