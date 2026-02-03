package de.ralfrosenkranz.moltbook.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AgentStatus(
        @JsonProperty("claimed") Boolean claimed,
        @JsonProperty("verified") Boolean verified,
        @JsonProperty("verification_code") String verificationCode,
        @JsonProperty("claim_url") String claimUrl
) {}
