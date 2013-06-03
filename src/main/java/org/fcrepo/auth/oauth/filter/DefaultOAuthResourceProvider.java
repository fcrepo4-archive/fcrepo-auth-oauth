
package org.fcrepo.auth.oauth.filter;

import static com.google.common.base.Throwables.propagate;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.servlet.http.HttpServletRequest;

import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthRuntimeException;
import org.apache.oltu.oauth2.rsfilter.OAuthDecision;
import org.apache.oltu.oauth2.rsfilter.OAuthRSProvider;
import org.fcrepo.session.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DefaultOAuthResourceProvider implements OAuthRSProvider {

    @Autowired
    SessionFactory sessionFactory;

    @Override
    public OAuthDecision validateRequest(final String rsId, final String token,
            final HttpServletRequest req) throws OAuthProblemException {
        // first check validity of token
        try {
            final Session session = sessionFactory.getSession();
            try {
                if (!session.itemExists("/tokens/" + token)) {
                    throw new OAuthRuntimeException("Invalid token!");
                } else {
                    final Node tokenNode = session.getNode("/tokens/" + token);
                    final String client =
                            tokenNode.getProperty("oauth:client").getString();
                    final String principal =
                            tokenNode.getProperty("oauth:principal")
                                    .getString();

                    return new Decision(client, principal, authorized);
                }
            } finally {
                session.logout();
            }
        } catch (final RepositoryException e) {
            propagate(e);
        }

        return null;
    }
}
