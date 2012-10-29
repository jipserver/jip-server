<%@ page import="jip.server.User" %>

<div class="fieldcontain ${hasErrors(bean: userInstance, field: 'username', 'error')} ">
    <label for="username">
        <g:message code="user.username.label" default="Username" />

    </label>
    <g:textField name="username" value="${userInstance?.username}" />
</div>

<div class="fieldcontain ${hasErrors(bean: userInstance, field: 'email', 'error')} ">
    <label for="email">
        <g:message code="user.email.label" default="EMail" />

    </label>
    <g:textField name="email" value="${userInstance?.email}" />
</div>

<div class="fieldcontain ${hasErrors(bean: userInstance, field: 'password', 'error')} ">
	<label for="password">
		<g:message code="user.password.label" default="Password" />
	</label>
	<g:passwordField name="pwd" value="${pwd}" />
</div>

<div class="fieldcontain ${hasErrors(bean: userInstance, field: 'password', 'error')} ">
	<label for="password_repeat">
		<g:message code="user.password.label" default="Repeat Password" />
	</label>
	<g:passwordField name="pwd_repeat" value="${pwd_repeat}" />
</div>
