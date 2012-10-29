package jip.server

import grails.converters.JSON

import javax.ws.rs.PUT
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.core.Response

@Path('/api/job')
class JobResource {
    static transactional = false
    def springSecurityService
    def jmsService
    JobService jobService


    Response checkRequest(Job job, String data){
        User user  = springSecurityService.getCurrentUser()
        if(!data || data.isEmpty() ){
            return Response.status(Response.Status.BAD_REQUEST).entity("No data specified").build()
        }
        if(!job){
            return Response.status(Response.Status.NOT_FOUND).entity("Job ${jobid} not found!".toString()).build()
        }
        if(job.ownerId != user.id){
            return Response.status(Response.Status.UNAUTHORIZED).entity("Not allowed to update job!".toString()).build()
        }
        return null
    }

    @PUT
    @Path("{jobid}/message")
    Response sendMessage(@PathParam("jobid") Long jobid, String messageJSon) {
        Job job = Job.get(jobid)
        Response r = checkRequest(job, messageJSon)
        if(r != null){
            return r
        }

        // check message

        Map mdef = JSON.parse(messageJSon)
        if(!mdef.containsKey("type") || !mdef.containsKey("message") ){
            return Response.status(Response.Status.BAD_REQUEST).entity("Message is not valid. Specify Type and Message!").build()
        }

        if(mdef.type.toString().toUpperCase() == "PROGRESS"){
            try {
                int progress = Integer.parseInt(mdef.message)
                job.updateProgress(progress)
                return Response.ok().build()
            } catch (Exception e) {
                log.error("Unable to parse progress information from ${mdef}")
                return Response.status(Response.Status.BAD_REQUEST).entity("Unable to parse progress information!").build()
            }
        }

        Message message = new Message()
        try {
            message.type = Message.Type.valueOf(mdef.type.toString().toUpperCase())
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Unknown message type ${mdef.type}".toString()).build()
        }
        message.message = mdef.message

        log.info("Storing job message ${jobid}: ${message}")
        job.addMessage(message.type, message.message, true)
        message.createDate = new Date()
        if(job.messages == null){
            job.messages = []
        }
        job.messages.add(message)
        return Response.ok().build()
    }

    @PUT
    @Path("{jobid}/state")
    Response setState(@PathParam("jobid") Long jobid, String statusJson) {
        Job job = Job.get(jobid)
        Response r = checkRequest(job, statusJson)
        if(r != null){
            return r
        }

        Map status = JSON.parse(statusJson)
        if(!status.containsKey("state") ){
            return Response.status(Response.Status.BAD_REQUEST).entity("No status specified").build()
        }

        State state = null
        try {
            state = State.valueOf(status.state)
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Unknown message type ${status.state}".toString()).build()
        }
        if(!job.state.nextStates.contains(state)){
            return Response.status(Response.Status.BAD_REQUEST).entity("Illage state ${state} for ${job.id}".toString()).build()
        }
        FinishState finishState = null
        if(status.containsKey("finished")){
            try {
                finishState = FinishState.valueOf(status.finished)
            } catch (IllegalArgumentException e) {
                return Response.status(Response.Status.BAD_REQUEST).entity("Unknown finish statedmessage type ${status.finished}".toString()).build()
            }
        }

        jobService.updateState(job.id, state, finishState)
        return Response.ok().build()
    }

}
