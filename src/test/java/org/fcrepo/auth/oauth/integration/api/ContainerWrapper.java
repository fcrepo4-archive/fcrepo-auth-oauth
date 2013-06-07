package org.fcrepo.auth.oauth.integration.api;

import static java.util.Collections.emptyList;
import static org.slf4j.LoggerFactory.getLogger;

import java.net.URI;
import java.util.List;
import java.util.Map;

import javax.servlet.Filter;

//import com.sun.grizzly.servlet.ServletRegistration;
//import org.glassfish.grizzly.servlet.WebappContext;
import org.slf4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.web.context.support.XmlWebApplicationContext;
import org.springframework.web.filter.DelegatingFilterProxy;

import com.sun.grizzly.http.SelectorThread;
import com.sun.grizzly.http.servlet.ServletAdapter;
import com.sun.grizzly.http.servlet.ServletContextImpl;
import com.sun.grizzly.http.servlet.deployer.WebAppAdapter;
import com.sun.grizzly.http.webxml.WebappLoader;
import com.sun.grizzly.http.webxml.schema.ContextParam;
import com.sun.grizzly.http.webxml.schema.FilterMapping;
import com.sun.grizzly.http.webxml.schema.ServletMapping;
import com.sun.grizzly.http.webxml.schema.WebApp;
import com.sun.jersey.api.container.grizzly.GrizzlyServerFactory;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.api.core.servlet.WebAppResourceConfig;
import com.sun.jersey.core.spi.component.ioc.IoCComponentProviderFactory;
import com.sun.jersey.spi.spring.container.SpringComponentProviderFactory;
import com.sun.jersey.spi.spring.container.servlet.SpringServlet;

public class ContainerWrapper implements ApplicationContextAware {

    private static final Logger logger = getLogger(ContainerWrapper.class);

    private String contextConfigLocation = null;

    private int port;

    private SelectorThread server;

    private String packagesToScan = null;

    private List<Filter> filters = emptyList();
    
    private ApplicationContext applicationContext;

    public void setPackagesToScan(final String packagesToScan) {
        this.packagesToScan = packagesToScan;
    }

    public void setContextConfigLocation(final String contextConfigLocation) {
        this.contextConfigLocation = contextConfigLocation;
    }

    public void setPort(final int port) {
        this.port = port;
    }

    public void start() throws Exception {
    	
    	WebApp webApp = new WebApp();
    	
        WebAppAdapter waa = new WebAppAdapter("target", "test", webApp, getClass().getClassLoader(), null);

        final URI uri = URI.create("http://localhost:" + port + "/");
        final ServletAdapter adapter = waa.newServletAdapter(new SpringServlet());
        if (packagesToScan != null) {
            adapter.addInitParameter("com.sun.jersey.config.property.packages",
                    packagesToScan);
        }
        adapter.addInitParameter("com.sun.jersey.api.json.POJOMappingFeature",
                "true");
    	
        if (contextConfigLocation != null) {
            adapter.addContextParameter("contextConfigLocation",
                    contextConfigLocation);
        }
        
        
        DelegatingFilterProxy filter = new DelegatingFilterProxy();
        filter.setTargetBeanName("oauthFilter");
        adapter.addFilter(filter, filter.getClass().getName() + "-" +
                filter.hashCode(), null);
        
        filter = new DelegatingFilterProxy();
        filter.setTargetBeanName("authNFilter");
        adapter.addFilter(filter, filter.getClass().getName() + "-" +
                filter.hashCode(), null);

        adapter.addServletListener("org.springframework.web.context.ContextLoaderListener");

        adapter.setContextPath(uri.getPath());
        adapter.setProperty("load-on-startup", 1);
        
        server = GrizzlyServerFactory.create(uri, waa);

        logger.debug("started grizzly webserver endpoint at " +
                server.getPort());
    }

    public void stop() throws Exception {
        server.stopEndpoint();
    }

    public void setFilters(final List<Filter> filters) {
        this.filters = filters;
    }

	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.applicationContext = applicationContext;
		
	}

}
