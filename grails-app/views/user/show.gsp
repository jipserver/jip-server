
<%@ page import="jip.server.User" %>
<!doctype html>
<html>
	<head>
		<meta name="layout" content="main">
		<g:set var="entityName" value="${message(code: 'user.label', default: 'User')}" />
		<title><g:message code="default.show.label" args="[entityName]" /></title>
	</head>
	<body>
		<a href="#show-user" class="skip" tabindex="-1"><g:message code="default.link.skip.label" default="Skip to content&hellip;"/></a>
		<div id="show-user" class="content scaffold-show" role="main">
			<h1>Welcome ${userInstance.username}</h1>
			<g:if test="${flash.message}">
			<div class="message" role="status">${flash.message}</div>
			</g:if>
            <g:each in="${cluster}" var="cl">
                <div class="user-cluster">
                    <span class="cluster-name">Cluster: ${cl.name}</span>
                    <span class="cluster-status">Current Status:${cl.status}</span>
                    <g:if test="${cl.authorize}">
                        <div class="cluster-authorize">
                            <g:form controller="user" action="authorize">
                                <fieldset class="form">
                                    <g:hiddenField name="cluster" value="${cl.name}"/>
                                    <g:hiddenField name="user" value="${userInstance.id}"/>
                                    <div class="fieldcontain">
                                        <label for="username">
                                            <g:message code="user.username.label" default="Username" />
                                            <span class="small">SSH Remote user name</span>
                                        </label>
                                        <g:textField name="username"/>
                                    </div>
                                    <div class="fieldcontain">
                                        <label for="password">
                                            <g:message code="user.password.label" default="Password" />
                                            <span class="small">SSH Remote password</span>
                                        </label>
                                        <g:passwordField name="password"/>
                                    </div>
                                </fieldset>
                                <fieldset class="buttons">
                                    <g:submitButton name="authorize" value="${message(code: 'default.button.authorize.label', default: 'Authorize')}"/>
                                </fieldset>
                            </g:form>
                        </div>
                    </g:if>
                    <g:else>
                        <div class="cluster-authorize">
                            <g:form controller="user" action="validateAuthorization">
                                <g:hiddenField name="cluster" value="${cl.name}"/>
                                <g:hiddenField name="user" value="${userInstance.id}"/>
                                <fieldset class="buttons">
                                    <g:submitButton name="validateAuthorization" value="${message(code: 'default.button.validate.label', default: 'Validate')}"/>
                                    <g:remoteLink action="updateProfileDialog" update="dialog"
                                                  params="[cluster:cl.name,user:userInstance.id]"
                                                  onLoading="showSpinner();">Update Profile</g:remoteLink>
                                </fieldset>
                            </g:form>
                        </div>
                    </g:else>
                </div>
            </g:each>
		</div>
	</body>
</html>
