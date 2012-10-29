<%@ page import="jip.server.Cluster" %>
<!doctype html>
<html>
	<head>
		<meta name="layout" content="main">
		<g:set var="entityName" value="${message(code: 'cluster.label', default: 'Cluster')}" />
		<title><g:message code="default.edit.label" args="[entityName]" /></title>
	</head>
	<body>
		<a href="#edit-cluster" class="skip" tabindex="-1"><g:message code="default.link.skip.label" default="Skip to content&hellip;"/></a>
		<div id="edit-cluster" class="content scaffold-edit" role="main">
			<h1><g:message code="default.edit.label" args="[entityName]" /></h1>
			<g:if test="${flash.message}">
			<div class="message" role="status">${flash.message}</div>
			</g:if>
			<g:hasErrors bean="${clusterInstance}">
			<ul class="errors" role="alert">
				<g:eachError bean="${clusterInstance}" var="error">
				<li <g:if test="${error in org.springframework.validation.FieldError}">data-field-id="${error.field}"</g:if>><g:message error="${error}"/></li>
				</g:eachError>
			</ul>
			</g:hasErrors>
			<g:form method="post" >
				<g:hiddenField name="id" value="${clusterInstance?.id}" />
				<g:hiddenField name="version" value="${clusterInstance?.version}" />
				<fieldset class="form">

                    <div class="fieldcontain ${hasErrors(bean: clusterInstance, field: 'name', 'error')} ">
                        <label for="name">
                            <g:message code="cluster.name.label" default="Name" />
                            <span class="small">Name of the cluster</span>
                        </label>
                        <g:textField  disabled="true" name="name" value="${clusterInstance?.name}" />
                    </div>

                    <div class="fieldcontain ${hasErrors(bean: clusterInstance, field: 'clusterType', 'error')} ">
                        <label for="clusterType">
                            <g:message code="cluster.clusterType.label" default="Cluster Type" />
                            <span class="small">Choose the type of grid engine used on the cluster</span>
                        </label>
                        <g:select name="clusterType" from="${["Local", "Slurm", "Sun Grid Engine", "LSF"]}" value="${clusterInstance?.clusterType}" keys="${["local", "slurm", "sge", "lsf"]}"/>
                    </div>

                    <div class="fieldcontain ${hasErrors(bean: clusterInstance, field: 'connectionType', 'error')} ">
                        <label for="connectionType">
                            <g:message code="cluster.connectionType.label" default="Connection Type" />
                            <span class="small">How do you connect to the cluster</span>
                        </label>
                        <g:select name="connectionType" from="${jip.server.Cluster$ConnectionType?.values()}" keys="${jip.server.Cluster$ConnectionType.values()*.name()}" required="" value="${clusterInstance?.connectionType?.name()}"/>
                    </div>

                    <div class="fieldcontain ${hasErrors(bean: clusterInstance, field: 'home', 'error')} ">
                        <label for="home">
                            <g:message code="cluster.home.label" default="Home" />
                            <span class="small">Home folder used by JIP to store data</span>
                        </label>
                        <g:textField name="home" value="${clusterInstance?.home}" />
                    </div>

                    <div class="fieldcontain ${hasErrors(bean: clusterInstance, field: 'host', 'error')} ">
                        <label for="host">
                            <g:message code="cluster.host.label" default="Host" />
                            <span class="small">Remote host name or IP. If necessary specify a port, i.e localhost:1024 </span>
                        </label>
                        <g:textField name="host" value="${clusterInstance?.host}" />
                    </div>

                    <div class="fieldcontain ${hasErrors(bean: clusterInstance, field: 'serverUrl', 'error')} ">
                        <label for="serverUrl">
                            <g:message code="cluster.serverUrl.label" default="Server Url" />
                            <span class="small">Specify an alternatife URL to reach the JIP server. The FQDN is used by default.</span>
                        </label>
                        <g:textField name="serverUrl" value="${clusterInstance?.serverUrl}" />
                    </div>

                    <div class="fieldcontain ${hasErrors(bean: clusterInstance?.clusterUser, field: 'user', 'error')} ">
                        <label for="user">
                            <g:message code="cluster.user.label" default="User" />
                            <span class="small">The remote user name</span>
                        </label>
                        <g:textField name="user" value="${clusterInstance?.clusterUser?.name}" />
                    </div>

                    <div class="fieldcontain ${hasErrors(bean: clusterInstance?.clusterUser, field: 'password', 'error')} ">
                        <label for="password">
                            <g:message code="cluster.password.label" default="Password" />
                            <span class="small">The remote user password</span>
                        </label>
                        <g:passwordField name="password" value="${clusterInstance?.clusterUser?.password}" />
                    </div>

                </fieldset>
				<fieldset class="buttons">
					<g:actionSubmit class="save" action="update" value="${message(code: 'default.button.update.label', default: 'Update')}" />
				</fieldset>
			</g:form>
		</div>
	</body>
</html>
