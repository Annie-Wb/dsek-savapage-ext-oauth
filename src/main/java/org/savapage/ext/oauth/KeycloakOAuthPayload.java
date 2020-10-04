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

import org.savapage.core.json.JsonAbstractBase;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 * @author Rijk Ravestein
 *
 */
@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public final class KeycloakOAuthPayload extends JsonAbstractBase {

    /**
     * ?
     */
    @JsonProperty("sub")
    private String sub;

    /**
     * Name. For example: "James Brown".
     */
    @JsonProperty("name")
    private String name;

    /**
     * Preferred username. For example: "jbrown".
     */
    @JsonProperty("preferred_username")
    private String preferredUsername;

    /**
     * Given name. For example: "James".
     */
    @JsonProperty("given_name")
    private String givenName;

    /**
     * Family name. For example: "Brown".
     */
    @JsonProperty("family_name")
    private String familyName;

    /**
     * Email address.
     */
    @JsonProperty("email")
    private String email;

    /**
     * {@link Boolean#TRUE} if verified.
     */
    @JsonProperty("email_verified")
    private Boolean emailVerified;

    public String getSub() {
        return sub;
    }

    public void setSub(String sub) {
        this.sub = sub;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return Preferred username. For example: "jbrown".
     */
    public String getPreferredUsername() {
        return preferredUsername;
    }

    /**
     * @param preferredUsername
     *            Preferred username. For example: "jbrown".
     */
    public void setPreferredUsername(String preferredUsername) {
        this.preferredUsername = preferredUsername;
    }

    public String getGivenName() {
        return givenName;
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    public String getFamilyName() {
        return familyName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Boolean getEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(Boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    /**
     * Creates an instance from a JSON string.
     *
     * @param json
     *            The JSON string.
     * @return The {@link KeycloakOAuthPayload} instance.
     */
    public static KeycloakOAuthPayload create(final String json) {
        return create(KeycloakOAuthPayload.class, json);
    }

}
