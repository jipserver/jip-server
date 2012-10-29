
<%@ page import="jip.server.Job" %>
<!doctype html>
<html>
	<head>
		<meta name="layout" content="main">
		<g:set var="entityName" value="${message(code: 'job.label', default: 'Job')}" />
		<title><g:message code="default.list.label" args="[entityName]" /></title>
        <g:javascript>
            var offset = 1
            var loading_job_date = false;
            $(window).scroll(function () {
                if ($(window).scrollTop() >= $(document).height() - $(window).height() - 200) {
                    if(!loading_job_date){
                        loading_job_date = true;
                        showSpinner();
                        $.ajax({
                            url: "${createLink(controller: 'job', action: 'listJobs')}",
                            type: 'GET',
                            data: {
                                'offset':offset,
                                'sort':"${sort}",
                                'order':"${order}"
                            },
                            dataType: 'html',
                            success: function(data) {
                                hideSpinner();
                                loading_job_date = false;
                                offset += 1;
                                $("#joblist-table-body").append(data);
                            }
                        });
                    }

                }
            });
        </g:javascript>
	</head>
	<body>
		<div id="list-job" class="content scaffold-list" role="main">
			<g:if test="${flash.message}">
			<div class="message" role="status">${flash.message}</div>
			</g:if>
            <g:render template="job_counts" model="[counts:counts]"/>
            <g:render template="job_table" bean="${jobInstanceList}"/>
		</div>
	</body>
</html>
