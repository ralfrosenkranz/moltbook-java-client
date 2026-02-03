package de.ralfrosenkranz.moltbook.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;

/**
 * Minimal public profile; fields may vary.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AgentProfile(
        String id,
        String name,
        String description,
        @JsonProperty("karma") Integer karma,
        @JsonProperty("created_at") OffsetDateTime createdAt
) {}
