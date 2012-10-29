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

import com.google.common.util.concurrent.FutureCallback
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListeningExecutorService
import com.google.common.util.concurrent.MoreExecutors
import jip.grid.Grid
import jip.grid.commands.CommandProcess
import jip.grid.commands.CommandRunner
import jip.grid.model.GridJobState
import jip.server.Job
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.util.concurrent.Callable
import java.util.concurrent.Executors

/**
 * Simple local execution grid manager
 *
 * @author Thasso Griebel <thasso.griebel@gmail.com>
 */
class LocalGrid implements Grid{
    /**
     * Local grid type
     */
    public static final String TYPE = "local"

    /**
     * The logger
     */
    private Logger log = LoggerFactory.getLogger(LocalGrid.class);

    /**
     * Listening executor service
     */
    private final ListeningExecutorService executorService

    /**
     * Number of executor threads
     */
    final int threads

    /**
     * The configuration
     */
    Map configuration

    /**
     * Grid identifier
     */
    final String name

    /**
     * Finished job states
     */
    final Map states = [:]

    /**
     * Finished futures
     */
    final Map futures = [:]

    /**
     * Create a new grid
     *
     * @param name the name
     * @param config the configuration
     */
    LocalGrid(String name, Map config) {
        this.name = name
        this.threads = config.threads ? config.threads : 2
        this.executorService = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(threads))
        this.configuration = config
    }

    @Override
    String getId() {
        return name
    }

    @Override
    void cancel(List<Job> jobs, final CommandRunner runner) throws Exception{
    }

    @Override
    void submit(List<Job> submitJobs, final CommandRunner runner) throws Exception{
        for (Job job : submitJobs) {
            job.clusterId = job.id.toString()
            job.cluster = getId()
            synchronized (states){
                states[job.clusterId] = GridJobState.Queued
            }

            def submittedFuture = executorService.submit(new Callable<CommandProcess>() {
                @Override
                CommandProcess call() {
                    synchronized (states){
                        states[job.clusterId] = GridJobState.Running
                    }
                    def process = runner.run(job.command.join(" "), job.workingDirectory, [:]) // todo : add environment variables
                    process.waitFor()
                    return process
                }
            })

            // add to futures map
            synchronized (futures){
                futures[job.clusterId] = submittedFuture
            }

            Futures.addCallback(submittedFuture, new FutureCallback<CommandProcess>(){
                @Override
                void onSuccess(CommandProcess process) {
                    synchronized (states){
                        states[job.clusterId] = process.waitFor() == 0 ? GridJobState.Done : GridJobState.Error
                    }
                    removeFromFuturesmap(job.clusterId)
                }

                @Override
                void onFailure(Throwable throwable) {
                    log.error("Local Grid job ${job.id} on ${getId()} failed : ${throwable.message}")
                    synchronized (states){
                        states[job.cluster] = GridJobState.Error
                    }
                    removeFromFuturesmap(job.clusterId)
                }

                void removeFromFuturesmap(String id) {
                    // remove to futures map
                    synchronized (futures){
                        futures.remove(id)
                    }
                }

            })

        }
    }

    @Override
    Map<String, GridJobState> list(CommandRunner runner) {
        Map<String, GridJobState> result = [:]
        def toRemove = []
        synchronized (states){
            states.each {k,v->
                result[k] = v
                if( ((GridJobState)v).isFinishedState()){
                    toRemove << k
                }
            }
            // clear states for completed jobs
            toRemove.each {
                states.remove(it)
            }
        }
        return result
    }

    @Override
    void copy(File source, String target, CommandRunner runner) {
        runner.copy(source, target)
    }

}
