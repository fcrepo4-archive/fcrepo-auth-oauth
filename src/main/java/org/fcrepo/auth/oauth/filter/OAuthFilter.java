
package org.fcrepo.auth.oauth.filter;

import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;
import static org.apache.oltu.oauth2.common.OAuth.OAUTH_CLIENT_ID;
import static org.apache.oltu.oauth2.common.OAuth.HeaderType.WWW_AUTHENTICATE;
import static org.apache.oltu.oauth2.common.error.OAuthError.CodeResponse.INVALID_REQUEST;
import static org.apache.oltu.oauth2.common.error.OAuthError.ResourceResponse.INSUFFICIENT_SCOPE;
import static org.apache.oltu.oauth2.common.message.OAuthResponse.errorResponse;
import static org.apache.oltu.oauth2.rsfilter.OAuthUtils.isEmpty;

import java.io.IOException;
import java.security.Principal;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.OAuthResponse;
import org.apache.oltu.oauth2.common.message.types.ParameterStyle;
import org.apache.oltu.oauth2.rs.request.OAuthAccessResourceRequest;
import org.apache.oltu.oauth2.rsfilter.OAuthDecision;
import org.apache.oltu.oauth2.rsfilter.OAuthRSProvider;

public class OAuthFilter implements Filter {

    private String realm;

    private OAuthRSProvider provider;

    private Set<ParameterStyle> parameterStyles;

    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request,
            final ServletResponse response, final FilterChain chain)
            throws IOException, ServletException {
        final HttpServletRequest req = (HttpServletRequest) request;
        final HttpServletResponse res = (HttpServletResponse) response;

        try {

            // Make an OAuth Request out of this servlet request
            final OAuthAccessResourceRequest oauthRequest =
                    new OAuthAccessResourceRequest(req, parameterStyles
                            .toArray(new ParameterStyle[0]));

            // Get the access token
            final String accessToken = oauthRequest.getAccessToken();

            final OAuthDecision decision =
                    provider.validateRequest(realm, accessToken, req);

            final Principal principal = decision.getPrincipal();

            request =
                    new HttpServletRequestWrapper((HttpServletRequest) request) {

                        @Override
                        public String getRemoteUser() {
                            return principal != null ? principal.getName()
                                    : null;
                        }

                        @Override
                        public Principal getUserPrincipal() {
                            return principal;
                        }

                    };

            request.setAttribute(OAUTH_CLIENT_ID, decision.getOAuthClient()
                    .getClientId());

            chain.doFilter(request, response);
            return;

        } catch (final OAuthSystemException e1) {
            throw new ServletException(e1);
        } catch (final OAuthProblemException e) {
            respondWithError(res, e);
            return;
        }

    }

    @Override
    public void destroy() {

    }

    private void respondWithError(final HttpServletResponse resp,
            final OAuthProblemException error) throws IOException,
            ServletException {

        OAuthResponse oauthResponse = null;

        try {
            if (isEmpty(error.getError())) {
                oauthResponse =
                        errorResponse(SC_UNAUTHORIZED).setRealm(realm)
                                .buildHeaderMessage();

            } else {

                int responseCode = 401;
                if (error.getError().equals(INVALID_REQUEST)) {
                    responseCode = 400;
                } else if (error.getError().equals(INSUFFICIENT_SCOPE)) {
                    responseCode = 403;
                }

                oauthResponse =
                        errorResponse(responseCode).setRealm(realm).setError(
                                error.getError()).setErrorDescription(
                                error.getDescription()).setErrorUri(
                                error.getUri()).buildHeaderMessage();
            }
            resp.addHeader(WWW_AUTHENTICATE, oauthResponse
                    .getHeader(WWW_AUTHENTICATE));
            resp.sendError(oauthResponse.getResponseStatus());
        } catch (final OAuthSystemException e) {
            throw new ServletException(e);
        }
    }

    public void setRealm(final String realm) {
        this.realm = realm;
    }

    public void setProvider(final OAuthRSProvider provider) {
        this.provider = provider;
    }

    public void
            setParameterStyles(final Set<ParameterStyle> parameterStylesSet) {
        this.parameterStyles = parameterStylesSet;
    }

}