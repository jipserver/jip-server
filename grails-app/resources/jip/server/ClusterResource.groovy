package jip.server

import grails.converters.JSON
import grails.plugins.springsecurity.SpringSecurityService

import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response
import javax.ws.rs.*

@Path('/api/cluster')
class ClusterResource {
    SpringSecurityService springSecurityService
    ClusterService clusterService
    JobService jobService

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{clusterName}/submit")
    Response submitJob(@PathParam("clusterName") String clusterName, String jobDefinitionJSon){
        log.info("Submit request for ${clusterName}")
        // check user has user role
        User user  = springSecurityService.getCurrentUser()
        if(!jobDefinitionJSon || jobDefinitionJSon.isEmpty()){
            return Response.status(Response.Status.BAD_REQUEST).entity("No job definition specified").build()
        }

        // check that the cluster exists
        def cluster = Cluster.findByName(clusterName)
        if(! cluster){
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Cluster with name ${clusterName} not found".toString())
                    .build()
        }

        // check that user has cluster access
        if(!user.clusterUser || user.clusterUser.get(cluster.name) == null){
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("${user.username} has no authorized access to ${cluster.name}".toString())
                    .build()
        }

        try{
            Map data = JSON.parse(jobDefinitionJSon)
            JobDefinition jobDefinition = new JobDefinition(data)
            jobDefinition.owner = user.id
            Job submittedJob = jobService.createJob(jobDefinition, clusterName)
            return Response.ok(new JSON(submittedJob).toString()).build()
        }catch(Exception e){
            log.error("Unable to submit job !", e)
            return Response.status(Response.Status.BAD_REQUEST).entity("Unable to submit job : ${e?.message}".toString()).build()
        }
    }
}
