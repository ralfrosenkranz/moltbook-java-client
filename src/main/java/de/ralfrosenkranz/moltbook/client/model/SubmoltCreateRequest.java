package de.ralfrosenkranz.moltbook.client.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request body for POST /submolts
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record SubmoltCreateRequest(
        String name,
        @JsonProperty("display_name") String displayName,
        String description
) {}
