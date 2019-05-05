/*
 * This file is part of the SavaPage project <https://www.savapage.org>.
 * Copyright (c) 2011-2019 Datraverse B.V.
 * Author: Rijk Ravestein.
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
public final class AzureOAuthPayload extends JsonAbstractBase {

    /** */
    @JsonProperty("accountEnabled")
    private Boolean accountEnabled;

    /** */
    @JsonProperty("displayName")
    private String displayName;

    /** */
    @JsonProperty("employeeId")
    private String employeeId;

    /** */
    @JsonProperty("givenName")
    private String givenName;

    /** */
    @JsonProperty("mail")
    private String mail;

    /** */
    @JsonProperty("mailNickname")
    private String mailNickname;

    public Boolean getAccountEnabled() {
        return accountEnabled;
    }

    public void setAccountEnabled(final Boolean accountEnabled) {
        this.accountEnabled = accountEnabled;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(final String displayName) {
        this.displayName = displayName;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getGivenName() {
        return givenName;
    }

    public void setGivenName(final String givenName) {
        this.givenName = givenName;
    }

    /**
     * @return Full email address. For example: john.doe@example.com
     */
    public String getMail() {
        return mail;
    }

    /**
     * @param mail
     *            Full email address.
     */
    public void setMail(final String mail) {
        this.mail = mail;
    }

    /**
     *
     * @return Mailbox name (alias). For example: john.doe
     */
    public String getMailNickname() {
        return mailNickname;
    }

    public void setMailNickname(String mailNickname) {
        this.mailNickname = mailNickname;
    }

    /**
     * Creates an instance from a JSON string.
     *
     * @param json
     *            The JSON string.
     * @return The {@link AzureOAuthPayload} instance.
     */
    public static AzureOAuthPayload create(final String json) {
        return create(AzureOAuthPayload.class, json);
    }

}
