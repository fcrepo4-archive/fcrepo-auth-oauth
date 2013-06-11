package org.fcrepo.auth.oauth.integration.api;

import static java.util.Collections.emptyList;
import static org.slf4j.LoggerFactory.getLogger;

import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.Filter;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

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
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.web.context.support.XmlWebApplicationContext;
import org.springframework.web.filter.DelegatingFilterProxy;

import com.sun.jersey.api.container.grizzly2.GrizzlyServerFactory;
import com.sun.jersey.api.core.ClassNamesResourceConfig;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.api.core.servlet.WebAppResourceConfig;
import com.sun.jersey.core.spi.component.ioc.IoCComponentProviderFactory;
import com.sun.jersey.spi.container.servlet.ServletContainer;
import com.sun.jersey.spi.spring.container.SpringComponentProviderFactory;
import com.sun.jersey.spi.spring.container.servlet.SpringServlet;
import com.sun.jersey.test.framework.AppDescriptor;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.WebAppDescriptor;
import com.sun.jersey.test.framework.WebAppDescriptor.Builder;
import com.sun.jersey.test.framework.WebAppDescriptor.FilterDescriptor;
import com.sun.jersey.test.framework.spi.container.TestContainer;
import com.sun.jersey.test.framework.spi.container.grizzly2.web.GrizzlyWebTestContainerFactory;

public class ContainerWrapper implements ApplicationContextAware {

    private static final Logger logger = getLogger(ContainerWrapper.class);

    private String contextConfigLocation = null;

    private int port;

    private HttpServer server;

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
    	
    	JerseyTest jt;
    	
        final URI uri = URI.create("http://localhost:" + port + "/");
        
    	Map<String,String> initParams = new HashMap<String, String>();
        
        server = GrizzlyWebContainerFactory.create(uri, initParams);
        
        WebappContext wac = new WebappContext("test", "");
        
        wac.addContextInitParameter("contextConfigLocation",
        		"classpath:spring-test/master.xml");

        wac.addListener("org.springframework.web.context.ContextLoaderListener");
        wac.addListener("org.springframework.web.context.request.RequestContextListener");
        
        ServletRegistration servlet = wac.addServlet("jersey-servlet", SpringServlet.class);
        
        servlet.addMapping("/*");
                
        servlet.setInitParameter("com.sun.jersey.config.property.packages", "org.fcrepo");

        servlet.setInitParameter("com.sun.jersey.api.json.POJOMappingFeature", "true");

        FilterRegistration opFilter = wac.addFilter("OpFilter", DelegatingFilterProxy.class);
        
        opFilter.setInitParameter("targetBeanName", "oauthFilter");
        
        opFilter.addMappingForUrlPatterns(null, "/rest/objects/authenticated/*");
        opFilter.addMappingForUrlPatterns(null, "/rest/objects/authenticated");
        
        FilterRegistration tokenFilter = wac.addFilter("TokenFilter", DelegatingFilterProxy.class);
        
        tokenFilter.setInitParameter("targetBeanName", "authNFilter");
        
        tokenFilter.addMappingForUrlPatterns(null, "/token");
            	
        wac.deploy(server);

        URL webXml = this.getClass().getResource("/web.xml");
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
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.applicationContext = applicationContext;
		
	}

}
