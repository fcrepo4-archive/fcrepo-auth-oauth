
package org.fcrepo.auth.oauth;

import static com.google.common.base.Throwables.propagate;
import static org.slf4j.LoggerFactory.getLogger;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.servlet.http.HttpServletRequest;

import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthRuntimeException;
import org.apache.oltu.oauth2.rsfilter.OAuthDecision;
import org.apache.oltu.oauth2.rsfilter.OAuthRSProvider;
import org.fcrepo.session.SessionFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static org.fcrepo.auth.oauth.Constants.*;

@Component
public class DefaultOAuthResourceProvider implements OAuthRSProvider {

    @Autowired
    private SessionFactory sessionFactory;

    private static final Logger LOGGER =
            getLogger(DefaultOAuthResourceProvider.class);

    @Override
    public OAuthDecision validateRequest(final String rsId, final String token,
            final HttpServletRequest req) throws OAuthProblemException {
        // first check validity of token
        try {
            final Session session = sessionFactory.getSession(OAUTH_WORKSPACE);
            try {
                if (!session.itemExists("/tokens/" + token)) {
                    throw new OAuthRuntimeException("Invalid token!");
                } else {
                    final Node tokenNode = session.getNode("/tokens/" + token);
                    LOGGER.debug("Retrieved token from: {}", tokenNode
                            .getPath());
                    final String client =
                            tokenNode.getProperty(CLIENT_PROPERTY).getString();
                    LOGGER.debug("Retrieved client: {}", client);
                    final String principal =
                            tokenNode.getProperty(PRINCIPAL_PROPERTY)
                                    .getString();
                    LOGGER.debug("Retrieved principal: {}", principal);
                    return new Decision(client, principal);
                }
            } finally {
                session.logout();
            }
        } catch (final RepositoryException e) {
            propagate(e);
        }

        return null;
    }

    public void setSessionFactory(final SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }
}
