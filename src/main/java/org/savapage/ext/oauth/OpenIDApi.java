package org.savapage.ext.oauth;

import com.github.scribejava.core.builder.api.DefaultApi20;

/**
 * @author Daniel Adu-Gyan
 *
 */
public final class OpenIDApi extends DefaultApi20 {

    private final String authorizationBaseUrl;
    private final String accessTokenEndpoint;

    protected OpenIDApi(final String authorizationBaseUrl, final String accessTokenEndpoint) {
        this.authorizationBaseUrl = authorizationBaseUrl;
        this.accessTokenEndpoint = accessTokenEndpoint;
    }

    public static OpenIDApi instance(final String authorizationBaseUrl, final String accessTokenEndpoint) {
        return new OpenIDApi(authorizationBaseUrl, accessTokenEndpoint);
    }

    @Override
    public String getAccessTokenEndpoint() {
        return accessTokenEndpoint;
    }

    @Override
    protected String getAuthorizationBaseUrl() {
        return authorizationBaseUrl;
    }
}
