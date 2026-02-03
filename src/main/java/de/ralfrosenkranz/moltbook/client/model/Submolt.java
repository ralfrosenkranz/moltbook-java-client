package de.ralfrosenkranz.moltbook.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Complete Submolt (community) model with all fields from the API response.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record Submolt(
        String id,
        String name,
        @JsonProperty("display_name") String displayName,
        String description,
        @JsonProperty("subscriber_count") Integer subscriberCount,
        @JsonProperty("created_at") String createdAt,
        @JsonProperty("last_activity_at") String lastActivityAt,
        @JsonProperty("featured_at") String featuredAt,
        @JsonProperty("created_by") String createdBy
) {}
