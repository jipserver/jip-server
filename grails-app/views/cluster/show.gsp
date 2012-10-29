
<%@ page import="jip.server.Cluster" %>
<!doctype html>
<html>
	<head>
		<meta name="layout" content="main">
		<g:set var="entityName" value="${message(code: 'cluster.label', default: 'Cluster')}" />
		<title><g:message code="default.show.label" args="[entityName]" /></title>
	</head>
	<body>
		<a href="#show-cluster" class="skip" tabindex="-1"><g:message code="default.link.skip.label" default="Skip to content&hellip;"/></a>
		<div id="show-cluster" class="content scaffold-show" role="main">
			<h1><g:message code="default.show.label" args="[entityName]" /></h1>
			<g:if test="${flash.message}">
			<div class="message" role="status">${flash.message}</div>
			</g:if>
			<ol class="property-list cluster">
                <g:if test="${clusterInstance?.name}">
                    <li class="fieldcontain">
                        <span id="name-label" class="property-label"><g:message code="cluster.name.label" default="Name" /></span>
                        <span class="property-value" aria-labelledby="name-label"><g:fieldValue bean="${clusterInstance}" field="name"/></span>
                    </li>
                </g:if>

                <g:if test="${clusterInstance?.clusterType}">
				<li class="fieldcontain">
					<span id="clusterType-label" class="property-label"><g:message code="cluster.clusterType.label" default="Cluster Type" /></span>
    				<span class="property-value" aria-labelledby="clusterType-label"><g:fieldValue bean="${clusterInstance}" field="clusterType"/></span>
				</li>
				</g:if>

				<g:if test="${clusterInstance?.connectionType}">
				<li class="fieldcontain">
					<span id="connectionType-label" class="property-label"><g:message code="cluster.connectionType.label" default="Connection Type" /></span>
                    <span class="property-value" aria-labelledby="connectionType-label"><g:fieldValue bean="${clusterInstance}" field="connectionType"/></span>
				</li>
				</g:if>

				<g:if test="${clusterInstance?.home}">
				<li class="fieldcontain">
					<span id="home-label" class="property-label"><g:message code="cluster.home.label" default="Home" /></span>
					<span class="property-value" aria-labelledby="home-label"><g:fieldValue bean="${clusterInstance}" field="home"/></span>
				</li>
				</g:if>

				<g:if test="${clusterInstance?.serverUrl}">
				<li class="fieldcontain">
					<span id="serverUrl-label" class="property-label"><g:message code="cluster.serverUrl.label" default="Server URL" /></span>
					<span class="property-value" aria-labelledby="serverUrl-label"><g:fieldValue bean="${clusterInstance}" field="serverUrl"/></span>
				</li>
				</g:if>

				<g:if test="${clusterInstance?.host}">
				<li class="fieldcontain">
					<span id="host-label" class="property-label"><g:message code="cluster.host.label" default="Host" /></span>
					<span class="property-value" aria-labelledby="host-label"><g:fieldValue bean="${clusterInstance}" field="host"/></span>
				</li>
				</g:if>

                <g:if test="${clusterInstance?.port}">
                    <li class="fieldcontain">
                        <span id="port-label" class="property-label"><g:message code="cluster.port.label" default="Port" /></span>
                        <span class="property-value" aria-labelledby="port-label"><g:fieldValue bean="${clusterInstance}" field="port"/></span>
                    </li>
                </g:if>

                <g:if test="${clusterInstance?.clusterUser}">
                    <li class="fieldcontain">
                        <span id="user-label" class="property-label"><g:message code="cluster.user.label" default="User" /></span>
                        <span class="property-value" aria-labelledby="user-label"><g:fieldValue bean="${clusterInstance.clusterUser}" field="name"/></span>
                    </li>
                </g:if>


				<li class="fieldcontain">
					<span id="provisioned-label" class="property-label"><g:message code="cluster.provisioned.label" default="Provisioned" /></span>
					<span class="property-value" aria-labelledby="provisioned-label"><g:formatBoolean boolean="${clusterInstance?.provisioned}" /></span>
				</li>

				<li class="fieldcontain">
					<span id="state-label" class="property-label"><g:message code="cluster.state.label" default="State" /></span>
					<span class="property-value" aria-labelledby="state-label"><g:fieldValue bean="${clusterInstance}" field="state"/></span>
				</li>

			</ol>
            <div class="clear"></div>
            <sec:ifAnyGranted roles="ROLE_ADMIN">
                <g:form>
                    <fieldset class="buttons">
                        <g:hiddenField name="id" value="${clusterInstance?.id}" />
                        <g:hiddenField name="name" value="${clusterInstance?.name}" />
                        <g:link class="edit" action="provision" id="${clusterInstance?.name}"><g:message code="default.button.provision.label" default="Provision" /></g:link>
                        <g:link class="edit" action="edit" id="${clusterInstance?.name}"><g:message code="default.button.edit.label" default="Edit" /></g:link>
                        <g:actionSubmit class="delete" action="delete" value="${message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" />
                    </fieldset>
                </g:form>
            </sec:ifAnyGranted>
		</div>
	</body>
</html>
