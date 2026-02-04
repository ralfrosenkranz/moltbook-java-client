package de.ralfrosenkranz.moltbook.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * PATCH /agents/me payload (documented field: description).
 * Additional updatable fields may exist.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AgentUpdateMeRequest(
        @JsonProperty("description") String description
) {
}
