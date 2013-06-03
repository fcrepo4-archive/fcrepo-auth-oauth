
package org.fcrepo.auth.oauth.api;

import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_OK;
import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;
import static javax.ws.rs.core.MediaType.APPLICATION_FORM_URLENCODED;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.status;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import static org.apache.oltu.oauth2.as.response.OAuthASResponse.tokenResponse;
import static org.apache.oltu.oauth2.common.message.OAuthResponse.errorResponse;
import static org.apache.oltu.oauth2.common.OAuth.OAUTH_GRANT_TYPE;
import static org.apache.oltu.oauth2.common.error.OAuthError.TokenResponse.INVALID_GRANT;
import static org.apache.oltu.oauth2.common.error.OAuthError.TokenResponse.UNAUTHORIZED_CLIENT;
import static org.apache.oltu.oauth2.common.error.OAuthError.TokenResponse.INVALID_CLIENT;
import org.apache.oltu.oauth2.as.issuer.MD5Generator;
import org.apache.oltu.oauth2.as.issuer.OAuthIssuer;
import org.apache.oltu.oauth2.as.issuer.OAuthIssuerImpl;
import org.apache.oltu.oauth2.as.request.OAuthTokenRequest;
import org.apache.oltu.oauth2.as.response.OAuthASResponse;
import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.OAuthResponse;
import org.apache.oltu.oauth2.common.message.types.GrantType;
import org.fcrepo.AbstractResource;
import static org.fcrepo.auth.oauth.filter.Constants.CLIENT_PROPERTY;
import static org.fcrepo.auth.oauth.filter.Constants.PRINCIPAL_PROPERTY;
import org.springframework.stereotype.Component;

@Component
@Path("/token")
public class TokenEndpoint extends AbstractResource {

    public static final String INVALID_CLIENT_DESCRIPTION =
            "Client authentication failed (e.g., unknown client, no client authentication included, or unsupported authentication method).";

    @POST
    @Consumes(APPLICATION_FORM_URLENCODED)
    @Produces(APPLICATION_JSON)
    public Response authorize(@Context
    final HttpServletRequest request) throws OAuthSystemException,
            RepositoryException {

        OAuthTokenRequest oauthRequest = null;

        final OAuthIssuer oauthIssuerImpl =
                new OAuthIssuerImpl(new MD5Generator());

        try {
            oauthRequest = new OAuthTokenRequest(request);

            // TODO check if clientid is valid
            if (isValid()) {
                final OAuthResponse response =
                        OAuthASResponse.errorResponse(SC_BAD_REQUEST).setError(
                                INVALID_CLIENT).setErrorDescription(
                                INVALID_CLIENT_DESCRIPTION).buildJSONMessage();
                return status(response.getResponseStatus()).entity(
                        response.getBody()).build();
            }

            // TODO check if client_secret is valid
            if (isValid()) {
                final OAuthResponse response =
                        OAuthASResponse
                                .errorResponse(SC_UNAUTHORIZED)
                                .setError(UNAUTHORIZED_CLIENT)
                                .setErrorDescription(INVALID_CLIENT_DESCRIPTION)
                                .buildJSONMessage();
                return status(response.getResponseStatus()).entity(
                        response.getBody()).build();
            }

            // do checking for different grant types
            if (oauthRequest.getParam(OAUTH_GRANT_TYPE).equals(
                    GrantType.AUTHORIZATION_CODE.toString())) {
                // TODO check if authzcode is valid
                if (isValid()) {
                    final OAuthResponse response =
                            errorResponse(SC_BAD_REQUEST).setError(
                                    INVALID_GRANT).setErrorDescription(
                                    "invalid authorization code")
                                    .buildJSONMessage();
                    return status(response.getResponseStatus()).entity(
                            response.getBody()).build();
                }
            } else if (oauthRequest.getParam(OAuth.OAUTH_GRANT_TYPE).equals(
                    GrantType.PASSWORD.toString())) {
                // TODO check if username/password is valid
                if (isValid()) {
                    final OAuthResponse response =
                            errorResponse(SC_BAD_REQUEST).setError(
                                    INVALID_GRANT).setErrorDescription(
                                    "invalid username or password")
                                    .buildJSONMessage();
                    return status(response.getResponseStatus()).entity(
                            response.getBody()).build();
                }
            } else if (oauthRequest.getParam(OAuth.OAUTH_GRANT_TYPE).equals(
                    GrantType.REFRESH_TOKEN.toString())) {
                // refresh token is not supported in this implementation
                final OAuthResponse response =
                        errorResponse(SC_BAD_REQUEST).setError(INVALID_GRANT)
                                .setErrorDescription(
                                        "invalid username or password")
                                .buildJSONMessage();
                return status(response.getResponseStatus()).entity(
                        response.getBody()).build();
            }

            final String token = oauthIssuerImpl.accessToken();
            saveToken(token, oauthRequest.getClientId(), oauthRequest
                    .getUsername());
            final OAuthResponse response =
                    tokenResponse(SC_OK).setAccessToken(token).setExpiresIn(
                            "3600").buildJSONMessage();
            return status(response.getResponseStatus()).entity(
                    response.getBody()).build();

        } catch (final OAuthProblemException e) {
            final OAuthResponse res =
                    errorResponse(SC_BAD_REQUEST).error(e).buildJSONMessage();
            return status(res.getResponseStatus()).entity(res.getBody())
                    .build();
        }
    }

    private void saveToken(final String token, final String client,
            final String username) throws RepositoryException {
        final Session session = sessions.getSession();
        try {
            final Node tokenNode =
                    jcrTools.findOrCreateNode(session, "/tokens/" + token);
            tokenNode.setProperty(CLIENT_PROPERTY, client);
            tokenNode.setProperty(PRINCIPAL_PROPERTY, username);
            session.save();
        } finally {
            session.logout();
        }

    }

    private boolean isValid() {
        // TODO actually do some checking of client ID and secret and so forth
        return false;
    }

}
