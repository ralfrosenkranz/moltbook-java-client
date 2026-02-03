package de.ralfrosenkranz.moltbook.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;

/**
 * Minimal post model; expand as needed.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record Post(
        String id,
        String submolt,
        String title,
        String content,
        @JsonProperty("url") String url,
        @JsonProperty("author") String author,
        @JsonProperty("score") Integer score,
        @JsonProperty("created_at") OffsetDateTime createdAt
) {}
