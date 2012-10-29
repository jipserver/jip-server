<table class="job-table">
    <thead>
    <tr>
        <g:sortableColumn property="id" title="${message(code: 'job.id.label', default: 'ID')}" />
        <g:sortableColumn property="clusterId" title="${message(code: 'job.clusterId.label', default: 'C-ID')}" />
        <g:sortableColumn property="state" title="${message(code: 'job.state.label', default: 'State')}" />
        <g:sortableColumn property="pipelineJobs" title="${message(code: 'job.tasks.label', default: 'Tasks')}" />
        <g:sortableColumn property="cluster" title="${message(code: 'job.cluster.label', default: 'Cluster')}" />
        <g:sortableColumn property="ownerId" title="${message(code: 'job.owner.label', default: 'Owner')}" />
        <g:sortableColumn property="name" title="${message(code: 'job.name.label', default: 'Description')}" />
        <g:sortableColumn property="progress" title="${message(code: 'job.progress.label', default: 'Progress')}" />
        <th>${message(code: 'job.age.label', default: 'Age/Time')}</th>
        <g:sortableColumn property="createDate" title="${message(code: 'job.createDate.label', default: 'Create Date')}" />
    </tr>
    </thead>
    <tbody id="joblist-table-body">
    <g:render template="job_list_rows" model="['jobInstanceList':it]"/>
    </tbody>
</table>
