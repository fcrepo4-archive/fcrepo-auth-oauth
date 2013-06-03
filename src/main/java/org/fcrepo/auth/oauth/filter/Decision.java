
package org.fcrepo.auth.oauth.filter;

import java.security.Principal;

import org.apache.oltu.oauth2.rsfilter.OAuthClient;
import org.apache.oltu.oauth2.rsfilter.OAuthDecision;

public class Decision implements OAuthDecision {

    private OAuthClient oAuthClient;

    private Principal principal;

    private boolean isAuthorized;

    public Decision(final String client, final String principal) {
        this.oAuthClient = new OAuthClient() {

            @Override
            public String getClientId() {
                return client;
            }

        };
        this.principal = new Principal() {

            @Override
            public String getName() {
                return principal;
            }

        };
    }

    @Override
    public OAuthClient getOAuthClient() {
        return oAuthClient;
    }

    @Override
    public Principal getPrincipal() {
        return principal;
    }

    @Override
    public boolean isAuthorized() {
        return isAuthorized;
    }

    public void setAuthorized(final boolean isAuthorized) {
        this.isAuthorized = isAuthorized;
    }

}
