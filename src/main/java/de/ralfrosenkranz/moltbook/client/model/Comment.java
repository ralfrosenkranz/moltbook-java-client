package de.ralfrosenkranz.moltbook.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;

/**
 * Minimal comment model; server may include additional fields.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record Comment(
        @JsonProperty("id") String id,
        @JsonProperty("post_id") String postId,
        @JsonProperty("parent_id") String parentId,
        @JsonProperty("content") String content,
        @JsonProperty("author") String author,
        @JsonProperty("score") Integer score,
        @JsonProperty("created_at") OffsetDateTime createdAt
) {
}