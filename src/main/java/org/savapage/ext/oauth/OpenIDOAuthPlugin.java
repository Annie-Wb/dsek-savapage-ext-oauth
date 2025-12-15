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
 * @author Daniel Adu-Gyan
 *
 */
public final class OpenIDOAuthPlugin implements OAuthClientPlugin {

        private static final Logger LOGGER = LoggerFactory.getLogger(OpenIDOAuthPlugin.class);

        private static final String PROP_KEY_PFX = "openid.";
        private static final String PROP_KEY_LOGIN_BTN = PROP_KEY_PFX + "login.btn.";
        private static final String PROP_KEY_LOGIN_BTN_ICON = PROP_KEY_LOGIN_BTN + "icon";
        private static final String PROP_KEY_LOGIN_BTN_TXT = PROP_KEY_LOGIN_BTN + "text";
        private static final String PROP_KEY_PLUGIN_ICON = PROP_KEY_PFX + "plugin.icon";
        private static final String PROP_KEY_PLUGIN_USER_SOURCE = PROP_KEY_PFX + "savapage.usersource";
        private static final String PROP_KEY_OAUTH_PFX = PROP_KEY_PFX + "oauth.";
        private static final String PROP_KEY_OAUTH_CLIENT_ID = PROP_KEY_OAUTH_PFX + "client-id";
        private static final String PROP_KEY_OAUTH_CLIENT_SECRET = PROP_KEY_OAUTH_PFX + "client-secret";
        private static final String PROP_KEY_OAUTH_AUTHORIZATION_URL = PROP_KEY_OAUTH_PFX + "authorization-url";
        private static final String PROP_KEY_OAUTH_TOKEN_URL = PROP_KEY_OAUTH_PFX + "token-url";
        private static final String PROP_KEY_OAUTH_USERINFO_URL = PROP_KEY_OAUTH_PFX + "userinfo-url";
        private static final String PROP_KEY_OAUTH_CALLBACK_URL = PROP_KEY_OAUTH_PFX + "callback-url";
        private static final String OAUTH_SCOPE = "openid profile";
        private static final String CALLBACK_URL_PARM_CODE = "code";

        private String id;
        private String name;
        private String customIconPath;
        private boolean showButtonIconAtLogin;
        private String textButtonIconAtLogin;
        private boolean userSource;
        private OAuth20Service oauthService;
        private URL authorizationUrl;
        private URL userinfoUrl;
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
        public void onInit(final String pluginId, final String pluginName,
                        final boolean live, final boolean online, final Properties props,
                        final ServerPluginContext context) throws ServerPluginException {

                this.id = pluginId;
                this.name = pluginName;
                this.customIconPath = props.getProperty(PROP_KEY_PLUGIN_ICON);
                this.textButtonIconAtLogin = props.getProperty(PROP_KEY_LOGIN_BTN_TXT);
                this.showButtonIconAtLogin = Boolean
                                .parseBoolean(props.getProperty(PROP_KEY_LOGIN_BTN_ICON, Boolean.toString(true)));
                this.userSource = Boolean
                                .parseBoolean(props.getProperty(PROP_KEY_PLUGIN_USER_SOURCE, Boolean.toString(false)));

                try {
                        final ServiceBuilder svcBuilder = new ServiceBuilder(
                                        props.getProperty(PROP_KEY_OAUTH_CLIENT_ID));
                        svcBuilder.apiSecret(props.getProperty(PROP_KEY_OAUTH_CLIENT_SECRET));
                        svcBuilder.defaultScope(OAUTH_SCOPE);
                        svcBuilder.callback(props.getProperty(PROP_KEY_OAUTH_CALLBACK_URL));

                        final OpenIDApi api = OpenIDApi.instance(
                                        props.getProperty(PROP_KEY_OAUTH_AUTHORIZATION_URL),
                                        props.getProperty(PROP_KEY_OAUTH_TOKEN_URL));
                        this.oauthService = svcBuilder.build(api);

                        this.authorizationUrl = new URL(this.oauthService.getAuthorizationUrl());
                        this.callbackUrl = new URL(props.getProperty(PROP_KEY_OAUTH_CALLBACK_URL));
                        this.userinfoUrl = new URL(props.getProperty(PROP_KEY_OAUTH_USERINFO_URL));
                } catch (MalformedURLException e) {
                        throw new ServerPluginException(e.getMessage());
                }
        }

        @Override
        public void onStart() throws ServerPluginException {
        }

        @Override
        public void onStop() throws ServerPluginException {
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
        public String getCustomIconPath() {
                return this.customIconPath;
        }

        @Override
        public String getInstanceId() {
                return ID_ONE_OAUTH_PROVIDER;
        }

        @Override
        public boolean showLoginButtonIcon() {
                return this.showButtonIconAtLogin;
        }

        @Override
        public String getLoginButtonText() {
                return this.textButtonIconAtLogin;
        }

        @Override
        public OAuthProviderEnum getProvider() {
                return OAuthProviderEnum.KEYCLOAK;
        }

        @Override
        public boolean isUserSource() {
                return this.userSource;
        }

        @Override
        public OAuthUserInfo onCallBack(final Map<String, String> parameterMap)
                        throws IOException, OAuthPluginException {
                if (!parameterMap.containsKey(CALLBACK_URL_PARM_CODE)) {
                        return null;
                }
                final String code = parameterMap.get(CALLBACK_URL_PARM_CODE);
                try {
                        final OAuth2AccessToken token = this.oauthService.getAccessToken(code);
                        final String accessToken = token.getAccessToken();

                        final OAuthRequest request = new OAuthRequest(Verb.GET,
                                        this.userinfoUrl.toString());

                        this.oauthService.signRequest(accessToken, request);

                        try (Response response = this.oauthService.execute(request)) {
                                if (!response.isSuccessful()) {
                                        LOGGER.error(String.format("Error %d", response.getCode()));
                                        return null;
                                }
                                final String json = response.getBody();
                                final OpenIDOAuthPayload payload = OpenIDOAuthPayload.create(json);
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
                } catch (java.util.concurrent.ExecutionException e) {
                        throw new OAuthPluginException(e.getMessage());
                }
                return null;
        }
}

