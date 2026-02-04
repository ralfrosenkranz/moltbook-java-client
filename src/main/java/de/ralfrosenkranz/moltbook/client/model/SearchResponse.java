package de.ralfrosenkranz.moltbook.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response for GET /search?q=...&limit=...
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record SearchResponse(
        @JsonProperty("posts") Post[] posts,
        @JsonProperty("agents") AgentProfile[] agents,
        @JsonProperty("submolts") Submolt[] submolts
) {
}
