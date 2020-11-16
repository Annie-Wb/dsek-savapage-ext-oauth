/*
 * This file is part of the SavaPage project <https://www.savapage.org>.
 * Copyright (c) 2011-2020 Datraverse B.V.
 * Author: Rijk Ravestein.
 *
 * SPDX-FileCopyrightText: 2011-2020 Datraverse B.V. <info@datraverse.com>
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
import java.util.Scanner;
import java.util.concurrent.ExecutionException;

import org.apache.commons.lang3.BooleanUtils;
import org.savapage.ext.ServerPluginContext;
import org.savapage.ext.ServerPluginException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.scribejava.apis.MicrosoftAzureActiveDirectory20Api;
import com.github.scribejava.apis.MicrosoftAzureActiveDirectoryApi;
import com.github.scribejava.apis.microsoftazureactivedirectory.BaseMicrosoftAzureActiveDirectoryApi;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;

/**
 *
 * @author Rijk Ravestein
 *
 */
public final class AzureOAuthPlugin implements OAuthClientPlugin {

    /**
     * The {@link Logger}.
     */
    private static final Logger LOGGER =
            LoggerFactory.getLogger(AzureOAuthPlugin.class);

    /** */
    enum ApiVersionEnum {
        /** Version 1.6. */
        V16,
        /** Version 2.0. */
        V20
    }

    /**
     * Property key prefix.
     */
    private static final String PROP_KEY_PFX = "azure.";

    /**
     * OAuth property key prefix.
     */
    private static final String PROP_KEY_OAUTH_PFX = PROP_KEY_PFX + "oauth.";

    /** */
    private static final String PROP_KEY_OAUTH_CLIENT_ID =
            PROP_KEY_OAUTH_PFX + "client-id";

    /** */
    private static final String PROP_KEY_OAUTH_CLIENT_SECRET =
            PROP_KEY_OAUTH_PFX + "client-secret";

    /** */
    private static final String PROP_KEY_OAUTH_CALLBACK_URL =
            PROP_KEY_OAUTH_PFX + "callback-url";

    /** */
    private static final String PROP_KEY_OAUTH_VERSION =
            PROP_KEY_OAUTH_PFX + "version";

    /** */
    private static final String PROP_KEY_OAUTH_V16 = "1";
    /** */
    private static final String PROP_KEY_OAUTH_V20 = "2";

    /**
     * OAuth scope.
     */
    private static final String OAUTH_SCOPE = "email";

    /** */
    private static final String CALLBACK_URL_PARM_CODE = "code";

    /** */
    private static final String PROTECTED_RESOURCE_URL_V16 =
            "https://graph.windows.net/me?api-version=1.6";

    /** */
    private static final String PROTECTED_RESOURCE_URL_V20 =
            "https://graph.microsoft.com/v1.0/me";

    /** */
    private static final String NETWORK_NAME =
            "Microsoft Azure Active Directory";

    /**
     *
     */
    private String id;

    /** */
    private String name;

    /** */
    private ApiVersionEnum apiVersion;

    /**
     * The singleton {@link OAuth20Service}.
     */
    private OAuth20Service oauthService;

    /**
     * URL of OAuth provider where users authorize SavaPage to do OAuth calls.
     */
    private URL authorizationUrl;

    /**
     * URL the OAuth provider should redirect after authorization.
     */
    private URL callbackUrl;

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public OAuthProviderEnum getProvider() {
        return OAuthProviderEnum.AZURE;
    }

    @Override
    public String getInstanceId() {
        return null;
    }

    @Override
    public String getCustomIconPath() {
        return null;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void onStart() {
    }

    @Override
    public void onStop() {
    }

    @Override
    public void onInit(final String pluginId, final String pluginName,
            final boolean live, final boolean online, final Properties props,
            final ServerPluginContext context) throws ServerPluginException {

        this.id = pluginId;
        this.name = pluginName;

        final BaseMicrosoftAzureActiveDirectoryApi baseApi;

        if (props.getProperty(PROP_KEY_OAUTH_VERSION, PROP_KEY_OAUTH_V16)
                .equals(PROP_KEY_OAUTH_V16)) {

            this.apiVersion = ApiVersionEnum.V16;
            baseApi = MicrosoftAzureActiveDirectoryApi.instance();

        } else {

            this.apiVersion = ApiVersionEnum.V20;
            baseApi = MicrosoftAzureActiveDirectory20Api.instance();
        }

        this.oauthService =
                new ServiceBuilder(props.getProperty(PROP_KEY_OAUTH_CLIENT_ID))
                        .apiSecret(
                                props.getProperty(PROP_KEY_OAUTH_CLIENT_SECRET))
                        .defaultScope(OAUTH_SCOPE)
                        .callback(
                                props.getProperty(PROP_KEY_OAUTH_CALLBACK_URL))
                        .build(baseApi);
        try {

            this.callbackUrl =
                    new URL(props.getProperty(PROP_KEY_OAUTH_CALLBACK_URL));

            this.authorizationUrl =
                    new URL(this.oauthService.getAuthorizationUrl());

        } catch (MalformedURLException e) {
            throw new IllegalStateException(e);
        }
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

        // See: https://github.com/scribejava/scribejava
        // com.github.scribejava.apis.examples
        // .MicrosoftAzureActiveDirectory20Example

        if (!parameterMap.containsKey(CALLBACK_URL_PARM_CODE)) {
            return null;
        }

        final String code = parameterMap.get(CALLBACK_URL_PARM_CODE);

        try {
            /*
             * Get the Access Token. Since this is just a quick peek at the
             * username, a refresh token is not relevant.
             */
            final OAuth2AccessToken token = oauthService.getAccessToken(code);
            final String accessToken = token.getAccessToken();

            /*
             * Ask for a protected resource.
             */
            final String resourceURL;

            if (this.apiVersion == ApiVersionEnum.V16) {
                resourceURL = PROTECTED_RESOURCE_URL_V16;
            } else {
                resourceURL = PROTECTED_RESOURCE_URL_V20;
            }
            final OAuthRequest request =
                    new OAuthRequest(Verb.GET, resourceURL);

            oauthService.signRequest(accessToken, request);

            try (Response response = oauthService.execute(request)) {

                if (!response.isSuccessful()) {
                    LOGGER.error(String.format("Error %d", response.getCode()));
                    return null;
                }

                final String json = response.getBody();
                final AzureOAuthPayload payload =
                        AzureOAuthPayload.create(json);

                final OAuthUserInfo userInfo = new OAuthUserInfo();
                userInfo.setUserId(payload.getMailNickname());

                if (userInfo.getUserId() == null) {
                    LOGGER.error("No username found:\n{}", json);
                    return null;
                }

                if (BooleanUtils.isNotTrue(payload.getAccountEnabled())) {
                    LOGGER.error("User account [{}] disabled.",
                            userInfo.getUserId());
                    return null;
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

    /**
     *
     * @param args
     *            CLI arguments.
     * @throws Exception
     *             If errors.
     */
    public static void main(final String... args) throws Exception {

        int iArg = 0;

        final String version = args[iArg++];
        final String clientId = args[iArg++];
        final String clientSecret = args[iArg++];
        final String callback = args[iArg++];

        final OAuth20Service service;
        final String resourceURL;

        if (version.equals(PROP_KEY_OAUTH_V16)) {
            resourceURL = PROTECTED_RESOURCE_URL_V16;

            service = new ServiceBuilder(clientId).apiSecret(clientSecret)
                    .defaultScope("openid").callback(callback)
                    .build(MicrosoftAzureActiveDirectoryApi.instance());
        } else {
            resourceURL = PROTECTED_RESOURCE_URL_V20;

            service = new ServiceBuilder(clientId).apiSecret(clientSecret)
                    .defaultScope("openid User.Read").callback(callback)
                    .build(MicrosoftAzureActiveDirectory20Api.instance());
        }

        Scanner in = new Scanner(System.in, "UTF-8");

        System.out.println("=== " + NETWORK_NAME + "'s OAuth Workflow ===");
        System.out.println();

        // Obtain the Authorization URL
        System.out.println("Fetching the Authorization URL...");
        final String authorizationUrl = service.getAuthorizationUrl();
        System.out.println("Got the Authorization URL!");
        System.out.println("Now go and authorize ScribeJava here:");
        System.out.println(authorizationUrl);
        System.out.println("And paste the authorization code here");
        System.out.print(">>");
        final String code = in.nextLine();
        System.out.println();

        // Trade the Request Token and Verfier for the Access Token
        System.out.println("Trading the Request Token for an Access Token...");
        final OAuth2AccessToken accessToken = service.getAccessToken(code);
        System.out.println("Got the Access Token!");
        System.out.println("(The raw response looks like this: "
                + accessToken.getRawResponse() + "')");
        System.out.println();

        // Now let's go and ask for a protected resource!
        System.out.println("Now we're going to access a protected resource...");
        final OAuthRequest request = new OAuthRequest(Verb.GET, resourceURL);
        service.signRequest(accessToken, request);
        final Response response = service.execute(request);
        System.out.println("Got it! Lets see what we found...");
        System.out.println();
        System.out.println(response.getCode());
        System.out.println(response.getBody());

        System.out.println();
        System.out.println("Thats it man! Go and build something "
                + "awesome with ScribeJava! :)");

        in.close();
    }

    @Override
    public boolean isUserSource() {
        // not supported for now.
        return false;
    }

}
