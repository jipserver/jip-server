<div class="job-message-message job-message-type-${fieldValue(bean: message, field:"type").toString().toLowerCase()}">
    <div class="job-message-date"><g:formatDate date="${message.createDate}" style="SHORT" type="datetime"/></div>
    ${fieldValue(bean: message, field:"message")}
</div>
