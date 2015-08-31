package grails.plugin.markup.view

import grails.util.Environment
import grails.views.GenericViewConfiguration
import grails.views.ViewConfiguration
import groovy.text.markup.TemplateConfiguration
import groovy.transform.CompileStatic
import org.springframework.beans.BeanUtils

import java.beans.PropertyDescriptor

/**
 * @author Graeme Rocher
 * @since 1.0
 */
@CompileStatic
class MarkupViewConfiguration extends TemplateConfiguration implements GenericViewConfiguration {

    public static final String MODULE_NAME = "markup"

    MarkupViewConfiguration() {
        setExtension(MarkupViewTemplate.EXTENSION)
        setBaseTemplateClass(MarkupViewTemplate)
        setCacheTemplates( !Environment.isDevelopmentMode() )
        setAutoEscape(true)
    }

    @Override
    boolean isCache() {
        return isCacheTemplates()
    }

    @Override
    void setCache(boolean cache) {
        setCacheTemplates(cache)
    }

    @Override
    String getViewModuleName() {
        MODULE_NAME
    }

    @Override
    PropertyDescriptor[] findViewConfigPropertyDescriptor() {
        def allDescriptors = []
        allDescriptors.addAll(
            GenericViewConfiguration.super.findViewConfigPropertyDescriptor()
        )
        allDescriptors.addAll(
            BeanUtils.getPropertyDescriptors(TemplateConfiguration)
        )
        return allDescriptors as PropertyDescriptor[]
    }
}
