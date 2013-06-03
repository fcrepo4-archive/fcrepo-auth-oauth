
package org.fcrepo.auth.oauth.filter;

import static javax.servlet.http.HttpServletResponse.SC_FORBIDDEN;
import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;

public class RestrictToAuthNFilter implements Filter {

    private static final Logger LOGGER = getLogger(RestrictToAuthNFilter.class);

    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
        LOGGER.debug("Initialized {}", this.getClass().getName());
    }

    /*
     * (non-Javadoc)
     * Assumes that the filter chain contains {@link
     * org.apache.oltu.oauth2.rsfilter.OAuthFilter}
     * in a preceding position.
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest,
     * javax.servlet.ServletResponse, javax.servlet.FilterChain)
     */
    @Override
    public void doFilter(final ServletRequest request,
            final ServletResponse response, final FilterChain chain)
            throws IOException, ServletException {
        final HttpServletRequest req = (HttpServletRequest) request;
        final HttpServletResponse res = (HttpServletResponse) response;
        if (req.getUserPrincipal() != null) {
            res.sendError(SC_UNAUTHORIZED);
        }
        if (req.isUserInRole("kosher")) {
            chain.doFilter(request, response);
            return;
        } else {
            res.sendError(SC_FORBIDDEN);
        }

    }

    @Override
    public void destroy() {
        LOGGER.debug("Destroyed {}", this.getClass().getName());

    }

}
