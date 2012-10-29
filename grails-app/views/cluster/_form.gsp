<%@ page import="jip.server.Cluster" %>



<div class="fieldcontain ${hasErrors(bean: clusterInstance, field: 'clusterType', 'error')} ">
	<label for="clusterType">
		<g:message code="cluster.clusterType.label" default="Cluster Type" />
		
	</label>
	<g:textField name="clusterType" value="${clusterInstance?.clusterType}" />
</div>

<div class="fieldcontain ${hasErrors(bean: clusterInstance, field: 'connectionType', 'error')} ">
	<label for="connectionType">
		<g:message code="cluster.connectionType.label" default="Connection Type" />
		
	</label>
	<g:select name="connectionType" from="${jip.server.Cluster$ConnectionType?.values()}" keys="${jip.server.Cluster$ConnectionType.values()*.name()}" required="" value="${clusterInstance?.connectionType?.name()}"/>
</div>

<div class="fieldcontain ${hasErrors(bean: clusterInstance, field: 'home', 'error')} ">
	<label for="home">
		<g:message code="cluster.home.label" default="Home" />
		
	</label>
	<g:textField name="home" value="${clusterInstance?.home}" />
</div>

<div class="fieldcontain ${hasErrors(bean: clusterInstance, field: 'host', 'error')} ">
	<label for="host">
		<g:message code="cluster.host.label" default="Host" />
		
	</label>
	<g:textField name="host" value="${clusterInstance?.host}" />
</div>

<div class="fieldcontain ${hasErrors(bean: clusterInstance, field: 'jip', 'error')} ">
	<label for="jip">
		<g:message code="cluster.jip.label" default="Jip" />
		
	</label>
	<g:textField name="jip" value="${clusterInstance?.jip}" />
</div>

<div class="fieldcontain ${hasErrors(bean: clusterInstance, field: 'keyfile', 'error')} ">
	<label for="keyfile">
		<g:message code="cluster.keyfile.label" default="Keyfile" />
		
	</label>
	<g:textField name="keyfile" value="${clusterInstance?.keyfile}" />
</div>

<div class="fieldcontain ${hasErrors(bean: clusterInstance, field: 'message', 'error')} ">
	<label for="message">
		<g:message code="cluster.message.label" default="Message" />
		
	</label>
	<g:textField name="message" value="${clusterInstance?.message}" />
</div>

<div class="fieldcontain ${hasErrors(bean: clusterInstance, field: 'name', 'error')} ">
	<label for="name">
		<g:message code="cluster.name.label" default="Name" />
		
	</label>
	<g:textField name="name" value="${clusterInstance?.name}" />
</div>

<div class="fieldcontain ${hasErrors(bean: clusterInstance, field: 'password', 'error')} ">
	<label for="password">
		<g:message code="cluster.password.label" default="Password" />
		
	</label>
	<g:textField name="password" value="${clusterInstance?.password}" />
</div>

<div class="fieldcontain ${hasErrors(bean: clusterInstance, field: 'port', 'error')} ">
	<label for="port">
		<g:message code="cluster.port.label" default="Port" />
		
	</label>
	<g:field type="number" name="port" value="${clusterInstance.port}" />
</div>

<div class="fieldcontain ${hasErrors(bean: clusterInstance, field: 'provisioned', 'error')} ">
	<label for="provisioned">
		<g:message code="cluster.provisioned.label" default="Provisioned" />
		
	</label>
	<g:checkBox name="provisioned" value="${clusterInstance?.provisioned}" />
</div>

<div class="fieldcontain ${hasErrors(bean: clusterInstance, field: 'state', 'error')} ">
	<label for="state">
		<g:message code="cluster.state.label" default="State" />
		
	</label>
	<g:textField name="state" value="${clusterInstance?.state}" />
</div>

<div class="fieldcontain ${hasErrors(bean: clusterInstance, field: 'user', 'error')} ">
	<label for="user">
		<g:message code="cluster.user.label" default="User" />
		
	</label>
	<g:textField name="user" value="${clusterInstance?.user}" />
</div>

