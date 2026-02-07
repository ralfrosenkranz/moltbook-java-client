package de.ralfrosenkranz.moltbook.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.time.OffsetDateTime;

/**
 * Minimal post model; expand as needed.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record Post(
        @JsonProperty("id")
        String id,
        @JsonProperty("submolt")
        @JsonDeserialize(using = SubmoltLenientDeserializer.class)
        Submolt submolt,
        @JsonProperty("title")
        String title,
        @JsonProperty("content")
        String content,
        @JsonProperty("url") String url,
        @JsonProperty("author")
        @JsonDeserialize(using = AuthorLenientDeserializer.class)
        Author author,
        @JsonProperty("score") Integer score,
        @com.fasterxml.jackson.annotation.JsonAlias({"comment_count","comments_count","num_comments","commentCount"})
        @JsonProperty("comment_count") Integer commentCount,
        @JsonProperty("created_at") OffsetDateTime createdAt
) {
}
