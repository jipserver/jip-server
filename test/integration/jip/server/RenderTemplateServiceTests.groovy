package jip.server



import grails.test.mixin.*
import org.junit.*

/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
@TestFor(RenderTemplateService)
class RenderTemplateServiceTests {
    def renderTemplateService

    void testSomething() {
        assert renderTemplateService.renderString("lala", [:]) == "lala"
        assert renderTemplateService.renderString('${one}', [one:"one"]) == "one"
        assert renderTemplateService.renderString('${one.two}', [one:[two:"two"]]) == "two"
        assert renderTemplateService.renderString('${one.two}', ["one":["two":"two"]]) == "two"
    }
}
