
<%@ page import="jip.server.Cluster" %>
<!doctype html>
<html>
	<head>
		<meta name="layout" content="main">
		<g:set var="entityName" value="${message(code: 'cluster.label', default: 'Cluster')}" />
		<title><g:message code="default.list.label" args="[entityName]" /></title>
	</head>
	<body>
		<a href="#list-cluster" class="skip" tabindex="-1"><g:message code="default.link.skip.label" default="Skip to content&hellip;"/></a>
		<div class="nav" role="navigation">
			<ul>
                <sec:ifAnyGranted roles="ROLE_ADMIN">
				    <li><g:link class="create" action="create"><g:message code="default.new.label" args="[entityName]" /></g:link></li>
                </sec:ifAnyGranted>
			</ul>
		</div>
		<div id="list-cluster" class="content scaffold-list" role="main">
			<h1><g:message code="default.list.label" args="[entityName]" /></h1>
			<g:if test="${flash.message}">
			<div class="message" role="status">${flash.message}</div>
			</g:if>
			<table>
				<thead>
					<tr>
                        <g:sortableColumn property="name" title="${message(code: 'cluster.name.label', default: 'Name')}" />
                        <g:sortableColumn property="clusterType" title="${message(code: 'cluster.clusterType.label', default: 'Cluster Type')}" />
                        <g:sortableColumn property="connectionType" title="${message(code: 'cluster.connectionType.label', default: 'Connection Type')}" />
                        <g:sortableColumn property="host" title="${message(code: 'cluster.host.label', default: 'Host')}" />
						<g:sortableColumn property="home" title="${message(code: 'cluster.home.label', default: 'Home')}" />
						<g:sortableColumn property="state" title="${message(code: 'cluster.home.label', default: 'State')}" />

					</tr>
				</thead>
				<tbody>
				<g:each in="${clusterInstanceList}" status="i" var="clusterInstance">
					<tr class="${(i % 2) == 0 ? 'even' : 'odd'}">
						<td><g:link action="show" id="${clusterInstance.name}">${fieldValue(bean: clusterInstance, field: "name")}</g:link></td>
						<td>${fieldValue(bean: clusterInstance, field: "clusterType")}</td>
                        <td>${fieldValue(bean: clusterInstance, field: "connectionType")}</td>
                        <td>${fieldValue(bean: clusterInstance, field: "host")}</td>
						<td>${fieldValue(bean: clusterInstance, field: "home")}</td>
						<td>${fieldValue(bean: clusterInstance, field: "state")}</td>
					</tr>
				</g:each>
				</tbody>
			</table>
			<div class="pagination">
				<g:paginate total="${clusterInstanceTotal}" />
			</div>
		</div>
	</body>
</html>
