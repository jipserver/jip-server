<%@ page import="jip.server.State" %>
<g:each in="${jobInstanceList}" status="i" var="jobInstance">
    <tr class="${(i % 2) == 0 ? 'even' : 'odd'} job-state-${jobInstance.state} "
        onclick='window.location.href="${createLink(action: 'show', controller:"job", id:"${jobInstance.id}")}";'>
        <td><g:link action="show" id="${jobInstance.id}">${jobInstance.id}</g:link></td>
        <td>${jobInstance.clusterId}</td>
        <td class="state-${jobInstance.state}">${jobInstance.stateText}</td>
        <td>
            <g:if test="${jobInstance.pipelineJobs}">
                 ${jobInstance.pipelineJobs.findAll {it.jobState == State.Done}.size()} / ${jobInstance.pipelineJobs.size()}
            </g:if>
            <g:else>
                -
            </g:else>
        </td>
        <td>${jobInstance.cluster}</td>
        <td>${jobInstance.owner}</td>
        <td class="job-description">
            <div class="job-name">
                <g:link controller="job" action="show" id="${jobInstance.id}">${jobInstance.name}</g:link>
            </div>
            <g:if test="${jobInstance.lastMessage}">
                <div id="job-message-${jobInstance.id}" class="job-message job-message-last">
                    <div class="job-message-message job-message-type-${jobInstance.lastMessage.type.toString().toLowerCase()}">
                        ${jobInstance.lastMessage.message}
                    </div>
                </div>
            </g:if>
        </td>
        <td>
            <div class="progress-container">
                <g:if test="${jobInstance.progress > 0}">
                    <div class="progress-bar ${jobInstance.pipelineJobs ? 'task-bar':''}" style="width:${jobInstance.progress * 2}px">&nbsp;</div>
                </g:if>
                <g:else>
                    <div class="no-progress">No progress information</div>
                </g:else>
            </div>
        </td>
        <td>${jobInstance.age}</td>
        <td>${jobInstance.createDate}</td>
    </tr>
</g:each>
