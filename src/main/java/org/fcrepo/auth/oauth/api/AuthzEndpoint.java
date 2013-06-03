
package org.fcrepo.auth.oauth.api;

import static javax.servlet.http.HttpServletResponse.SC_FOUND;
import static javax.ws.rs.core.Response.status;
import static org.apache.oltu.oauth2.as.response.OAuthASResponse.authorizationResponse;
import static org.apache.oltu.oauth2.common.OAuth.OAUTH_REDIRECT_URI;
import static org.apache.oltu.oauth2.common.OAuth.OAUTH_RESPONSE_TYPE;
import static org.apache.oltu.oauth2.common.message.types.ResponseType.CODE;
import static org.apache.oltu.oauth2.common.message.types.ResponseType.TOKEN;
import static org.apache.oltu.oauth2.common.utils.OAuthUtils.isEmpty;

import java.net.URI;
import java.net.URISyntaxException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.servlet.http.HttpServletRequest;
import org.apache.oltu.oauth2.as.issuer.MD5Generator;
import org.apache.oltu.oauth2.as.issuer.OAuthIssuerImpl;
import org.apache.oltu.oauth2.as.request.OAuthAuthzRequest;
import org.apache.oltu.oauth2.as.response.OAuthASResponse;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.OAuthResponse;
import org.springframework.stereotype.Component;

import static org.apache.oltu.oauth2.common.message.OAuthResponse.errorResponse;

@Component
@Path("/authorization")
public class AuthzEndpoint {

    @GET
    public Response authorize(@Context
    final HttpServletRequest request) throws URISyntaxException,
            OAuthSystemException {

        OAuthAuthzRequest oauthRequest = null;

        final OAuthIssuerImpl oauthIssuerImpl =
                new OAuthIssuerImpl(new MD5Generator());

        try {
            oauthRequest = new OAuthAuthzRequest(request);

            //build response according to response_type
            final String responseType =
                    oauthRequest.getParam(OAUTH_RESPONSE_TYPE);

            final OAuthASResponse.OAuthAuthorizationResponseBuilder builder =
                    authorizationResponse(request, SC_FOUND);

            if (responseType.equals(CODE.toString())) {
                builder.setCode(oauthIssuerImpl.authorizationCode());
            }
            if (responseType.equals(TOKEN.toString())) {
                builder.setAccessToken(oauthIssuerImpl.accessToken());
                builder.setExpiresIn(3600l);
            }

            final String redirectURI =
                    oauthRequest.getParam(OAUTH_REDIRECT_URI);

            final OAuthResponse response =
                    builder.location(redirectURI).buildQueryMessage();
            final URI url = new URI(response.getLocationUri());

            return status(response.getResponseStatus()).location(url).build();

        } catch (final OAuthProblemException e) {

            final Response.ResponseBuilder responseBuilder = status(SC_FOUND);

            final String redirectUri = e.getRedirectUri();

            if (isEmpty(redirectUri)) {
                throw new WebApplicationException(responseBuilder.entity(
                        "OAuth callback url needs to be provided by client!!!")
                        .build());
            }
            final OAuthResponse response =
                    errorResponse(SC_FOUND).error(e).location(redirectUri)
                            .buildQueryMessage();
            final URI location = new URI(response.getLocationUri());
            return responseBuilder.location(location).build();
        }
    }

}
