
package org.fcrepo.auth.oauth.filter;

import java.security.Principal;

import org.apache.oltu.oauth2.rsfilter.OAuthClient;
import org.apache.oltu.oauth2.rsfilter.OAuthDecision;

public class Decision implements OAuthDecision {

    private OAuthClient oAuthClient;

    private Principal principal;

    private boolean authorized;

    public Decision(final String client, final String principal,
            final Boolean isAuthorized) {
        this.oAuthClient = null;
        this.principal = new Principal() {

            @Override
            public String getName() {
                return principal;
            }

        };
        this.authorized = isAuthorized;
    }

    @Override
    public OAuthClient getOAuthClient() {
        return oAuthClient;
    }

    @Override
    public Principal getPrincipal() {
        // TODO Auto-generated method stub
        return principal;
    }

    @Override
    public boolean isAuthorized() {
        // TODO Auto-generated method stub
        return authorized;
    }

}
