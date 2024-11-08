/*
 * This file is part of the SavaPage project <https://www.savapage.org>.
 * Copyright (c) 2024 Datraverse B.V.
 * Author: Rijk Ravestein.
 *
 * SPDX-FileCopyrightText: © 2024 Datraverse B.V. <info@datraverse.com>
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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.github.scribejava.apis.KeycloakApi;

/**
 * Extended class to override fixed "auth/realms" path in
 * {@link com.github.scribejava.apis.KeycloakApi}.
 *
 * @author Rijk Ravestein
 *
 */
public final class KeycloakApiExt extends KeycloakApi {

    public enum RealmPath {
        /** */
        REALMS("realms"), //
        /** */
        AUTH_REALMS("auth/realms");

        /** */
        private final String path;

        /**
         * @param p
         *            URL realm path.
         */
        RealmPath(final String p) {
            this.path = p;
        }

        /**
         * @return URL realm subpath.
         */
        public String getPath() {
            return this.path;
        }

        /**
         * Gets enum from path.
         *
         * @param path
         *            URL realm path.
         * @return {@code null} if not present.
         */
        public static RealmPath lookup(final String path) {
            for (RealmPath value : RealmPath.values()) {
                if (value.getPath().equals(path)) {
                    return value;
                }
            }
            return null;
        }

        /**
         * @return All subpath values concatenated in one string.
         */
        public static String validSubPaths() {
            final StringBuilder sb = new StringBuilder();
            for (RealmPath value : RealmPath.values()) {
                if (sb.length() > 0) {
                    sb.append(", ");
                }
                sb.append("\'").append(value.getPath()).append("\'");
            }
            return sb.toString();
        }
    };

    /** */
    private static final ConcurrentMap<String, KeycloakApiExt> INSTANCES =
            new ConcurrentHashMap<>();

    /**
     * @param baseUrlWithRealm
     */
    private KeycloakApiExt(final String baseUrlWithRealm) {
        super(baseUrlWithRealm);
    }

    /**
     *
     * @param baseUrl
     * @param realmSubPath
     * @param realm
     * @return instance.
     */
    public static KeycloakApiExt instance(final String baseUrl,
            final RealmPath realmSubPath, final String realm) {

        final String defaultBaseUrlWithRealm =
                composeBaseUrlWithRealmExt(baseUrl, realmSubPath, realm);

        KeycloakApiExt api = INSTANCES.get(defaultBaseUrlWithRealm);

        if (api == null) {

            api = new KeycloakApiExt(defaultBaseUrlWithRealm);

            final KeycloakApiExt alreadyCreatedApi =
                    INSTANCES.putIfAbsent(defaultBaseUrlWithRealm, api);
            if (alreadyCreatedApi != null) {
                return alreadyCreatedApi;
            }
        }
        return api;
    }

    /**
     * @param baseUrl
     * @param realmSubPath
     * @param realm
     * @return URL string.
     */
    private static String composeBaseUrlWithRealmExt(final String baseUrl,
            final RealmPath realmSubPath, final String realm) {
        return baseUrl + (baseUrl.endsWith("/") ? "" : "/")
                + realmSubPath.getPath() + "/" + realm;
    }

}
