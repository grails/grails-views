/*
 * Copyright 2015 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package grails.views.mvc

import grails.views.ResolvableGroovyTemplateEngine
import grails.web.http.HttpHeaders
import grails.web.mapping.LinkGenerator
import grails.web.mime.MimeType
import grails.web.mime.MimeUtility
import groovy.text.Template
import groovy.transform.CompileStatic
import org.springframework.beans.BeanUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.servlet.LocaleResolver
import org.springframework.web.servlet.View

import javax.annotation.PreDestroy
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import java.util.concurrent.ConcurrentHashMap
/**
 * Spring's default view resolving mechanism only accepts the view name and locale, this forces you to code around its limitations when you want to add intelligent features such as
 * version and mime type awareness.
 *
 * This aims to fix that whilst reducing complexity
 *
 * @author Graeme Rocher
 * @since 1.0
 */
@CompileStatic
class SmartViewResolver implements Closeable {

    @Delegate ResolvableGroovyTemplateEngine templateEngine

    Class<? extends GenericGroovyTemplateView> viewClass = GenericGroovyTemplateView
    String contentType
    String suffix = ""

    @Autowired
    LinkGenerator linkGenerator

    @Autowired
    LocaleResolver localeResolver

    @Autowired
    MimeUtility mimeUtility

    private Map<String, GenericGroovyTemplateView> viewCache = new ConcurrentHashMap<String, GenericGroovyTemplateView>().withDefault { String path ->
        GenericGroovyTemplateView view = BeanUtils.instantiateClass(viewClass)
        String contentType = getContentType()
        if (contentType != null) {
            view.contentType = contentType
        }

        view.url = path
        view.templateEngine = getTemplateEngine()
        view.linkGenerator = getLinkGenerator()
        view.localeResolver = getLocaleResolver()
        view.mimeUtility = getMimeUtility()
        return view
    }

    SmartViewResolver(ResolvableGroovyTemplateEngine templateEngine) {
        this.templateEngine = templateEngine
    }

    View resolveView(String viewName, Locale locale) {
        def url = "${viewName}${suffix}"
        View v = viewCache.containsKey(url) ? viewCache.get(url) : null
        if(v == null) {
            def template = resolveTemplate(url, locale)
            if(template != null) {
                return viewCache.get(url)
            }
        }
        return v
    }

    View resolveView(String viewName, HttpServletRequest request, HttpServletResponse response) {
        String url = "${viewName}${suffix}"
        View v = viewCache.containsKey(url) ? viewCache.get(url) : null
        if(v == null) {

            def locale = localeResolver?.resolveLocale(request) ?: request.locale
            def qualifiers = []
            def version = request.getHeader(HttpHeaders.ACCEPT_VERSION)
            MimeType mimeType = response.getMimeType()
            if(mimeType != null) {
                qualifiers.add(mimeType.extension)
            }
            if(version != null) {
                qualifiers.add(version)
            }
            def template = resolveTemplate(url, locale, qualifiers as String[])
            if(template != null) {
                return viewCache.get(url)
            }
        }
        return v
    }


    @PreDestroy
    void close() {
        templateEngine.close()
    }
}
