package de.ralfrosenkranz.moltbook.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;

/**
 * Minimal representation; additional fields may exist server-side.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AgentMe(
        String id,
        String name,
        String description,
        @JsonProperty("created_at") OffsetDateTime createdAt,
        @JsonProperty("updated_at") OffsetDateTime updatedAt
) {}
