package jip.server

import org.springframework.web.context.request.RequestContextHolder
import org.codehaus.groovy.grails.web.pages.GroovyPagesTemplateEngine
import org.codehaus.groovy.grails.web.context.ServletContextHolder
import org.springframework.web.context.support.WebApplicationContextUtils

class RenderTemplateService {
    GroovyPagesTemplateEngine groovyPagesTemplateEngine

    def renderString(String template, def model) {
        def webRequest = RequestContextHolder.getRequestAttributes()
        def servletContext  = ServletContextHolder.getServletContext()
        if(!webRequest) {

            def applicationContext = WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext)
            webRequest = grails.util.GrailsWebUtil.bindMockWebRequest(applicationContext)
        }
        def originalOut = webRequest.out
        try {
            def sw = new StringWriter()
            def pw = new PrintWriter(sw)
            webRequest.out = pw
            if(groovyPagesTemplateEngine == null){
                groovyPagesTemplateEngine = new GroovyPagesTemplateEngine(servletContext: servletContext)
            }
            groovyPagesTemplateEngine.createTemplate(template, "template-page").make(model).writeTo(pw)
            return sw.toString()
        } finally {
            webRequest.out = originalOut
        }
    }
}
