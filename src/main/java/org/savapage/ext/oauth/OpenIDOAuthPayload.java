package org.savapage.ext.oauth;

import org.savapage.core.json.JsonAbstractBase;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 * @author Daniel Adu-Gyan
 *
 */
@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public final class OpenIDOAuthPayload extends JsonAbstractBase {

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
     * @return The {@link OpenIDOAuthPayload} instance.
     */
    public static OpenIDOAuthPayload create(final String json) {
        return create(OpenIDOAuthPayload.class, json);
    }

}

