
package org.fcrepo.auth.oauth.filter;

import java.io.IOException;
import java.security.Principal;
import java.util.Set;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.error.OAuthError;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.OAuthResponse;
import org.apache.oltu.oauth2.common.message.types.ParameterStyle;
import org.apache.oltu.oauth2.rs.request.OAuthAccessResourceRequest;
import org.apache.oltu.oauth2.rs.response.OAuthRSResponse;
import org.apache.oltu.oauth2.rsfilter.OAuthDecision;
import org.apache.oltu.oauth2.rsfilter.OAuthFilter;
import org.apache.oltu.oauth2.rsfilter.OAuthRSProvider;
import org.apache.oltu.oauth2.rsfilter.OAuthUtils;

public class InjectableOAuthFilter implements Filter {

    public static final String OAUTH_RS_PROVIDER_CLASS =
            "oauth.rs.provider-class";

    public static final String RS_REALM = "oauth.rs.realm";

    public static final String RS_TOKENS = "oauth.rs.tokens";

    private String realm;

    private OAuthRSProvider provider;

    private Set<String> parameterStylesSet;

    private ParameterStyle[] parameterStyles;

    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {

        final String parameterStylesString =
                filterConfig.getServletContext().getInitParameter(RS_TOKENS);

        int i = 0;
        for (final String parameterStyleName : parameterStylesSet) {
            final ParameterStyle tempParameterStyle =
                    ParameterStyle.valueOf(parameterStyleName);
            if (tempParameterStyle != null) {
                parameterStyles[i++] = tempParameterStyle;
            } else {
                throw new ServletException("Incorrect ParameterStyle: " +
                        parameterStyleName);
            }
        }
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
                    new OAuthAccessResourceRequest(req, parameterStyles);

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

            request.setAttribute(OAuth.OAUTH_CLIENT_ID, decision
                    .getOAuthClient().getClientId());

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
            if (OAuthUtils.isEmpty(error.getError())) {
                oauthResponse =
                        OAuthRSResponse.errorResponse(
                                HttpServletResponse.SC_UNAUTHORIZED).setRealm(
                                realm).buildHeaderMessage();

            } else {

                int responseCode = 401;
                if (error.getError().equals(
                        OAuthError.CodeResponse.INVALID_REQUEST)) {
                    responseCode = 400;
                } else if (error.getError().equals(
                        OAuthError.ResourceResponse.INSUFFICIENT_SCOPE)) {
                    responseCode = 403;
                }

                oauthResponse =
                        OAuthRSResponse.errorResponse(responseCode).setRealm(
                                realm).setError(error.getError())
                                .setErrorDescription(error.getDescription())
                                .setErrorUri(error.getUri())
                                .buildHeaderMessage();
            }
            resp.addHeader(OAuth.HeaderType.WWW_AUTHENTICATE, oauthResponse
                    .getHeader(OAuth.HeaderType.WWW_AUTHENTICATE));
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

    public void setParameterStyles(final Set<String> parameterStylesSet) {
        this.parameterStylesSet = parameterStylesSet;
    }

}
