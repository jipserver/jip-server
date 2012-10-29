<%@ page import="jip.server.Job" %>
<!doctype html>
<html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: 'job.label', default: 'Job')}"/>
    <title><g:message code="default.show.label" args="[entityName]"/></title>
</head>

<body>
<div id="show-job" class="content scaffold-show" role="main">
    <h1 class="state-${jobInstance.state}"><g:message code="job.show.label" args="[jobInstance.id]"/></h1>
    <g:if test="${flash.message}">
        <div class="message" role="status">${flash.message}</div>
    </g:if>

    <table id="job-show-table">
        <tr>
            <td class="job-show-table-description">
                <ol class="property-list job">
                    <h2>Description</h2>
                    <li class="fieldcontain">
                        <div id="cluster-label" class="property-label">
                            <g:message code="job.cluster.label" default="Cluster"/>
                            <div id="cluster-description" class="property-description">
                                <g:message code="job.cluster.description" default="The cluster that runs the job"/>
                            </div>
                        </div>
                        <div class="property-value" aria-labelledby="cluster-label">
                            ${jobInstance.cluster}
                        </div>
                    </li>
                    <li class="fieldcontain">
                        <div id="owner-label" class="property-label">
                            <g:message code="job.owner.label" default="Owner"/>
                            <div id="owner-description" class="property-description">
                                <g:message code="job.owner.description" default="The Job owner"/>
                            </div>
                        </div>
                        <div class="property-value" aria-labelledby="owner-label">
                            ${jobInstance.owner}
                        </div>
                    </li>
                    <li class="fieldcontain">
                        <div id="clusterid-label" class="property-label">
                            <g:message code="job.clusterid.label" default="Cluster ID"/>
                            <div id="clusterid-description" class="property-description">
                                <g:message code="job.clusterid.description" default="Remote Job ID"/>
                            </div>
                        </div>
                        <div class="property-value" aria-labelledby="clusterid-label">
                            ${jobInstance.clusterId}
                        </div>
                    </li>
                    <li class="fieldcontain">
                        <div id="state-label" class="property-label">
                            <g:message code="job.state.label" default="State"/>
                            <div id="state-description" class="property-description">
                                <g:message code="job.state.description" default="Current Job state"/>
                            </div>
                        </div>
                        <div class="property-value" aria-labelledby="state-label">
                            ${jobInstance.stateText}
                        </div>
                    </li>
                    <li class="fieldcontain">
                        <div id="progress-label" class="property-label">
                            <g:message code="job.progress.label" default="Progress"/>
                            <div id="progress-description" class="property-description">
                                <g:message code="job.progress.description" default="Job Progress"/>
                            </div>
                        </div>
                        <div class="property-value" aria-labelledby="progress-label">
                            <div class="progress-container">
                                <g:if test="${jobInstance.progress > 0}">
                                    <div class="progress-bar ${jobInstance.pipelineJobs ? 'task-bar':''}" style="width:${jobInstance.progress * 2}px">&nbsp;</div>
                                </g:if>
                                <g:else>
                                    <div class="no-progress">No progress information</div>
                                </g:else>
                            </div>
                        </div>
                    </li>
                    <g:if test="${jobInstance.pipelineJobs}">
                        <li class="fieldcontain">
                            <div id="tasks-label" class="property-label">
                                <g:message code="job.tasks.label" default="Tasks"/>
                                <div id="created-description" class="property-description">
                                    <g:message code="job.created.description" default="#Jobs associated with this pipeline"/>
                                </div>
                            </div>
                            <div class="property-value" aria-labelledby="tasks-label">
                                ${jobInstance.pipelineJobs.size()}
                            </div>
                        </li>
                    </g:if>
                    <li class="fieldcontain">
                        <div id="created-label" class="property-label">
                            <g:message code="job.created.label" default="Created"/>
                            <div id="created-description" class="property-description">
                                <g:message code="job.created.description" default="Create Date"/>
                            </div>
                        </div>
                        <div class="property-value" aria-labelledby="created-label">
                            ${jobInstance.createDate}
                        </div>
                    </li>
                    <li class="fieldcontain">
                        <div id="submitted-label" class="property-label">
                            <g:message code="job.submitted.label" default="Submitted"/>
                            <div id="submitted-description" class="property-description">
                                <g:message code="job.submitted.description" default="Date of submission to the cluster"/>
                            </div>
                        </div>
                        <div class="property-value" aria-labelledby="submitted-label">
                            ${jobInstance.submitDate}
                        </div>
                    </li>
                    <li class="fieldcontain">
                        <div id="started-label" class="property-label">
                            <g:message code="job.started.label" default="Started"/>
                            <div id="submitted-description" class="property-description">
                                <g:message code="job.started.description" default="Job started on cluster"/>
                            </div>
                        </div>
                        <div class="property-value" aria-labelledby="started-label">
                            ${jobInstance.startDate}
                        </div>
                    </li>
                    <li class="fieldcontain">
                        <div id="finish-label" class="property-label">
                            <g:message code="job.finish.label" default="Finished"/>
                            <div id="finish-description" class="property-description">
                                <g:message code="job.finish.description" default="Job finished on cluster"/>
                            </div>
                        </div>
                        <div class="property-value" aria-labelledby="finish-label">
                            ${jobInstance.finishDate}
                        </div>
                    </li>
                </ol>
            </td>
            <td class="job-show-table-additional">
                <div class="job-additional">
                    <div class="job-messages">
                        <h2>Messages</h2>
                        <g:if test="${jobInstance.pipelineJobs}">
                            <g:each in="${jobInstance.pipelineJobs}" var="job">
                                <h3>${job.name} (${job.id})</h3>
                                <g:each in="${job.messages}" var="message">
                                    <g:render template="job_message" model="['message':message]"/>
                                </g:each>
                            </g:each>
                        </g:if>
                        <g:else>
                            <g:each in="${jobInstance.messages}" var="message">
                                <g:render template="job_message" model="['message':message]"/>
                            </g:each>
                        </g:else>
                    </div>
                    <div class="job-stderr">
                        <h2>Error log preview</h2>
                        <g:if test="${jobInstance.pipelineJobs}">
                            <g:each in="${jobInstance.pipelineJobs}" var="job">
                                <pre class="logview">${job.stderr}</pre>
                            </g:each>
                        </g:if>
                        <g:else>
                            <pre class="logview">${jobInstance.stderr}</pre>
                        </g:else>
                    </div>
                    <div class="job-stdout">
                        <h2>Output log preview</h2>
                        <g:if test="${jobInstance.pipelineJobs}">
                            <g:each in="${jobInstance.pipelineJobs}" var="job">
                                <pre class="logview">${job.stdout}</pre>
                            </g:each>
                        </g:if>
                        <g:else>
                            <pre class="logview">${jobInstance.stdout}</pre>
                        </g:else>

                    </div>
                </div>
            </td>
        </tr>
    </table>
    <g:if test="${jobInstance.pipelineJobs}">
        <h2>Pipeline Jobs</h2>
        <g:render template="job_table" bean="${jobInstance.pipelineJobs}"/>
    </g:if>
    <g:form>
        <fieldset class="buttons">
            <g:hiddenField name="id" value="${jobInstance?.id}"/>
            <g:actionSubmit class="delete" action="delete"
                            value="${message(code: 'default.button.delete.label', default: 'Delete')}"
                            onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');"/>
        </fieldset>
    </g:form>
</div>
</body>
</html>
