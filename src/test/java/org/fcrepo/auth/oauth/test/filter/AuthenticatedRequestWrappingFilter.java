
package org.fcrepo.auth.oauth.test.filter;

import static java.lang.reflect.Proxy.newProxyInstance;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;

public class AuthenticatedRequestWrappingFilter implements Filter {

    private static final Logger LOGGER =
            getLogger(AuthenticatedRequestWrappingFilter.class);

    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
        init();
    }

    // used by Spring
    public void init() {
        LOGGER.debug("Initialized {}", this.getClass().getName());
    }

    @Override
    public void doFilter(final ServletRequest request,
            final ServletResponse response, final FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;

        try {
            req.isUserInRole("");
        } catch (final IllegalStateException e) {
            // stupid grizzly doesn't impl isUserInRole(), so we have to stub it out
            req =
                    (HttpServletRequest) newProxyInstance(req.getClass()
                            .getClassLoader(),
                            new Class[] {HttpServletRequest.class},
                            new InvocationHandler() {

                                @Override
                                public Object
                                        invoke(final Object proxy,
                                                final Method method,
                                                final Object[] args)
                                                throws Throwable {
                                    if (method.getName().equals("isUserInRole")) {
                                        return true;
                                    }
                                    return method.invoke(request, args);

                                }
                            });
        }

        chain.doFilter(req, response);

    }

    @Override
    public void destroy() {
        LOGGER.debug("Destroyed {}", this.getClass().getName());

    }

}