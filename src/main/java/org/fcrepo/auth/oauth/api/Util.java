
package org.fcrepo.auth.oauth.api;

import static com.google.common.collect.ImmutableSet.copyOf;
import static org.fcrepo.auth.oauth.Constants.OAUTH_WORKSPACE;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.fcrepo.session.SessionFactory;

public class Util {

    public static void
            createOauthWorkspace(final SessionFactory sessionFactory)
                throws RepositoryException {
        final Session session = sessionFactory.getSession();
        try {
            if (!copyOf(session.getWorkspace().getAccessibleWorkspaceNames())
                    .contains(OAUTH_WORKSPACE)) {
                session.getWorkspace().createWorkspace(OAUTH_WORKSPACE);
            }
        } finally {
            session.logout();
        }
    }

}
