/*
 * This file is part of the SavaPage project <https://www.savapage.org>.
 * Copyright (c) 2020 Datraverse B.V.
 * Author: Rijk Ravestein.
 *
 * SPDX-FileCopyrightText: © 2020 Datraverse B.V. <info@datraverse.com>
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 * For more information, please contact Datraverse B.V. at this
 * address: info@datraverse.com
 */
package org.savapage.ext.oauth;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import org.savapage.ext.ServerPluginContext;
import org.savapage.ext.ServerPluginException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;

/**
 * <a href="https://www.keycloak.org/about">www.keycloak.org</a>.
 *
 * @author Rijk Ravestein
 *
 */
public final class KeycloakOAuthPlugin implements OAuthClientPlugin {

    /**
     * The {@link Logger}.
     */
    private static final Logger LOGGER =
            LoggerFactory.getLogger(KeycloakOAuthPlugin.class);

    /**
     * Property key prefix.
     */
    private static final String PROP_KEY_PFX = "keycloak.";

    /** */
    private static final String PROP_KEY_PLUGIN_ICON =
            PROP_KEY_PFX + "plugin.icon";

    /** */
    private static final String PROP_KEY_PLUGIN_USER_SOURCE =
            PROP_KEY_PFX + "savapage.usersource";

    /**
     * OAuth property key prefix.
     */
    private static final String PROP_KEY_OAUTH_PFX = PROP_KEY_PFX + "oauth.";

    /**
     * .
     */
    private static final String PROP_KEY_OAUTH_CLIENT_ID =
            PROP_KEY_OAUTH_PFX + "client-id";

    /**
     * .
     */
    private static final String PROP_KEY_OAUTH_CLIENT_SECRET =
            PROP_KEY_OAUTH_PFX + "client-secret";

    /**
     * .
     */
    private static final String PROP_KEY_OAUTH_BASE_URL =
            PROP_KEY_OAUTH_PFX + "base-url";

    /**
     * .
     */
    private static final String PROP_KEY_OAUTH_REALM =
            PROP_KEY_OAUTH_PFX + "realm";

    /**
     * .
     */
    private static final String PROP_KEY_OAUTH_REALM_PATH =
            PROP_KEY_OAUTH_REALM + ".path";

    /**
     * .
     */
    private static final String PROP_KEY_OAUTH_CALLBACK_URL =
            PROP_KEY_OAUTH_PFX + "callback-url";

    /**
     * OAuth scope.
     */
    private static final String OAUTH_SCOPE = "openid";

    /** */
    private static final String CALLBACK_URL_PARM_CODE = "code";

    /**
     * {@link String#format(String, Object...)} for Protected Resource URL.
     * Placeholders from left to right: {@link #PROP_KEY_OAUTH_BASE_URL},
     * {@link PROP_KEY_OAUTH_REALM_PATH}, {@link #PROP_KEY_OAUTH_REALM}.
     */
    private static final String PROTECTED_RESOURCE_URL_FORMAT =
            "%s/%s/%s/protocol/openid-connect/userinfo";

    /**
     * The unique ID.
     */
    private String id;

    /**
     * The user friendly name.
     */
    private String name;

    /**
     * Custom path for icon on login page.
     */
    private String customIconPath;

    /**
     * If {@code true}, the OAuth provided User ID is part of SavaPage external
     * user source.
     */
    private boolean userSource;

    /**
     * The singleton {@link OAuth20Service}.
     */
    private OAuth20Service oauthService;

    /**
     * URL of OAuth provider where users authorize SavaPage to do OAuth calls.
     */
    private URL authorizationUrl;

    /**
     * .
     */
    private URL protectedResourceUrl;

    /**
     * URL the OAuth provider should redirect after authorization.
     */
    private URL callbackUrl;

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void onStart() throws ServerPluginException {
    }

    @Override
    public void onStop() throws ServerPluginException {
    }

    @Override
    public void onInit(final String pluginId, final String pluginName,
            final boolean live, final boolean online, final Properties props,
            final ServerPluginContext context) throws ServerPluginException {

        final String oauthBasePropValue =
                props.getProperty(PROP_KEY_OAUTH_BASE_URL);

        if (oauthBasePropValue == null) {
            throw new ServerPluginException(String.format("[%s] not specified.",
                    PROP_KEY_OAUTH_BASE_URL));
        }

        final String oauthRealmPath =
                props.getProperty(PROP_KEY_OAUTH_REALM_PATH,
                        KeycloakApiExt.RealmPath.REALMS.getPath());

        final KeycloakApiExt.RealmPath oauthRealmPathEnum =
                KeycloakApiExt.RealmPath.lookup(oauthRealmPath);

        if (oauthRealmPathEnum == null) {
            throw new ServerPluginException(
                    String.format("[%s] [%s] is invalid. Valid values are: %s",
                            PROP_KEY_OAUTH_REALM_PATH, oauthRealmPath,
                            KeycloakApiExt.RealmPath.validSubPaths()));
        }

        this.id = pluginId;
        this.name = pluginName;

        this.customIconPath = props.getProperty(PROP_KEY_PLUGIN_ICON);

        this.userSource = Boolean.parseBoolean(props.getProperty(
                PROP_KEY_PLUGIN_USER_SOURCE, Boolean.toString(false)));

        try {
            // Validate URL by provoking MalformedURLException.
            final URL oauthBaseURL = new URL(oauthBasePropValue);

            /*
             * A valid URL path with (typo) '//' at the end (or middle?) is
             * reported NOT to work.
             */
            if (oauthBaseURL.getPath().contains("//")) {
                throw new ServerPluginException(
                        String.format("[%s] [%s] path contains \"//\".",
                                PROP_KEY_OAUTH_BASE_URL, oauthBasePropValue));
            }

            final ServiceBuilder svcBuilder = new ServiceBuilder(
                    props.getProperty(PROP_KEY_OAUTH_CLIENT_ID));

            svcBuilder
                    .apiSecret(props.getProperty(PROP_KEY_OAUTH_CLIENT_SECRET));
            svcBuilder.defaultScope(OAUTH_SCOPE);
            svcBuilder.callback(props.getProperty(PROP_KEY_OAUTH_CALLBACK_URL));

            final KeycloakApiExt api = KeycloakApiExt.instance(
                    oauthBasePropValue, oauthRealmPathEnum,
                    props.getProperty(PROP_KEY_OAUTH_REALM));

            this.oauthService = svcBuilder.build(api);

            this.callbackUrl =
                    new URL(props.getProperty(PROP_KEY_OAUTH_CALLBACK_URL));

            this.authorizationUrl =
                    new URL(this.oauthService.getAuthorizationUrl());

            this.protectedResourceUrl =
                    new URL(String.format(PROTECTED_RESOURCE_URL_FORMAT,
                            oauthBasePropValue, oauthRealmPath,
                            props.getProperty(PROP_KEY_OAUTH_REALM)));

        } catch (MalformedURLException e) {
            throw new ServerPluginException(e.getMessage());
        }
    }

    @Override
    public OAuthProviderEnum getProvider() {
        return OAuthProviderEnum.KEYCLOAK;
    }

    @Override
    public String getInstanceId() {
        return null;
    }

    @Override
    public String getCustomIconPath() {
        return this.customIconPath;
    }

    @Override
    public URL getAuthorizationUrl() {
        return this.authorizationUrl;
    }

    @Override
    public URL getCallbackUrl() {
        return this.callbackUrl;
    }

    @Override
    public OAuthUserInfo onCallBack(final Map<String, String> parameterMap)
            throws IOException, OAuthPluginException {

        if (!parameterMap.containsKey(CALLBACK_URL_PARM_CODE)) {
            return null;
        }
        final String code = parameterMap.get(CALLBACK_URL_PARM_CODE);

        try {
            /*
             * Get the Access Token. Since this is just a quick peek at the
             * username, a refresh token is not relevant.
             */
            final OAuth2AccessToken token =
                    this.oauthService.getAccessToken(code);
            final String accessToken = token.getAccessToken();

            final OAuthRequest request = new OAuthRequest(Verb.GET,
                    this.protectedResourceUrl.toString());

            this.oauthService.signRequest(accessToken, request);

            try (Response response = this.oauthService.execute(request)) {

                if (!response.isSuccessful()) {
                    LOGGER.error(String.format("Error %d", response.getCode()));
                    return null;
                }

                final String json = response.getBody();

                final KeycloakOAuthPayload payload =
                        KeycloakOAuthPayload.create(json);

                final OAuthUserInfo userInfo = new OAuthUserInfo();
                userInfo.setUserId(payload.getPreferredUsername());

                if (userInfo.getUserId() == null) {
                    LOGGER.error("No preferred username found:\n{}", json);
                    return null;
                }

                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(json);
                }

                return userInfo;
            }

        } catch (InterruptedException e) {
            LOGGER.warn(e.getMessage());
        } catch (ExecutionException e) {
            throw new OAuthPluginException(e.getMessage());
        }

        return null;
    }

    @Override
    public boolean isUserSource() {
        return this.userSource;
    }
}
