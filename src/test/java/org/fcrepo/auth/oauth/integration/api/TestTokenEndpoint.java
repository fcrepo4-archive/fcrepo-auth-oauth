
package org.fcrepo.auth.oauth.integration.api;

import static javax.ws.rs.core.MediaType.APPLICATION_FORM_URLENCODED;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.junit.Assert.assertEquals;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.util.EntityUtils;
import org.junit.Test;

public class TestTokenEndpoint extends AbstractResourceIT {

    final String tokenEndpoint = "http://" + HOSTNAME + ":" + SERVER_PORT +
            "/token";

    @Test
    public void testGetToken() throws Exception {
        logger.trace("Entering testGetToken()...");

        final HttpPost post =
                new HttpPost(
                        tokenEndpoint +
                                "?grant_type=password&username=foo&password=bar&client_secret=foo&client_id=bar");
        post.addHeader("Accept", APPLICATION_JSON);
        post.addHeader("Content-type", APPLICATION_FORM_URLENCODED);
        final HttpResponse tokenResponse = client.execute(post);
        logger.debug("Got a token response: \n{}", EntityUtils
                .toString(tokenResponse.getEntity()));
        assertEquals("Couldn't retrieve a token from token endpoint!", 200,
                tokenResponse.getStatusLine().getStatusCode());

    }

}
