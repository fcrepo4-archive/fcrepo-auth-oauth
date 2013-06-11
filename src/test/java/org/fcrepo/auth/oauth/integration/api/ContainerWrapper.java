
package org.fcrepo.auth.oauth.integration.api;

import static java.util.Collections.emptyList;
import static org.slf4j.LoggerFactory.getLogger;

import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.Filter;
//import com.sun.grizzly.servlet.ServletRegistration;
//import org.glassfish.grizzly.servlet.WebappContext;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.servlet.FilterRegistration;
import org.glassfish.grizzly.servlet.ServletRegistration;
import org.glassfish.grizzly.servlet.WebappContext;
import org.glassfish.jersey.grizzly2.servlet.GrizzlyWebContainerFactory;
import org.slf4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.filter.DelegatingFilterProxy;

import com.sun.jersey.spi.spring.container.servlet.SpringServlet;
import com.sun.jersey.test.framework.JerseyTest;

public class ContainerWrapper implements ApplicationContextAware {

    private static final Logger logger = getLogger(ContainerWrapper.class);

    private int port;

    private HttpServer server;

    private List<Filter> filters = emptyList();

    public void setPackagesToScan(final String packagesToScan) {
    }

    public void setContextConfigLocation(final String contextConfigLocation) {
    }

    public void setPort(final int port) {
        this.port = port;
    }

    public void start() throws Exception {

        final JerseyTest jt;

        final URI uri = URI.create("http://localhost:" + port + "/");

        final Map<String, String> initParams = new HashMap<String, String>();

        server = GrizzlyWebContainerFactory.create(uri, initParams);

        final WebappContext wac = new WebappContext("test", "");

        wac.addContextInitParameter("contextConfigLocation",
                "classpath:spring-test/master.xml");

        wac.addListener("org.springframework.web.context.ContextLoaderListener");
        wac.addListener("org.springframework.web.context.request.RequestContextListener");

        final ServletRegistration servlet =
                wac.addServlet("jersey-servlet", SpringServlet.class);

        servlet.addMapping("/*");

        servlet.setInitParameter("com.sun.jersey.config.property.packages",
                "org.fcrepo");

        servlet.setInitParameter("com.sun.jersey.api.json.POJOMappingFeature",
                "true");

        final FilterRegistration wrapFilter =
                wac.addFilter("WrapFilter", DelegatingFilterProxy.class);

        wrapFilter.setInitParameter("targetBeanName", "wrapFilter");

        wrapFilter.addMappingForUrlPatterns(null,
                "/rest/objects/authenticated/*");
        wrapFilter
                .addMappingForUrlPatterns(null, "/rest/objects/authenticated");

        final FilterRegistration opFilter =
                wac.addFilter("OpFilter", DelegatingFilterProxy.class);

        opFilter.setInitParameter("targetBeanName", "oauthFilter");

        opFilter.addMappingForUrlPatterns(null, "/rest/objects/authenticated/*");
        opFilter.addMappingForUrlPatterns(null, "/rest/objects/authenticated");

        final FilterRegistration tokenFilter =
                wac.addFilter("TokenFilter", DelegatingFilterProxy.class);

        tokenFilter.setInitParameter("targetBeanName", "authNFilter");

        tokenFilter.addMappingForUrlPatterns(null, "/token");

        wac.deploy(server);

        final URL webXml = this.getClass().getResource("/web.xml");
        logger.error(webXml.toString());

        logger.debug("started grizzly webserver endpoint at " +
                server.getHttpHandler().getName());
    }

    public void stop() throws Exception {
        server.stop();
    }

    public void setFilters(final List<Filter> filters) {
        this.filters = filters;
    }

    @Override
    public void setApplicationContext(
            final ApplicationContext applicationContext) throws BeansException {

    }

}
