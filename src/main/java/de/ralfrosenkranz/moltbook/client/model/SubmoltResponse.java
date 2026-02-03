package de.ralfrosenkranz.moltbook.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Wrapper class for API response containing submolts array
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record SubmoltResponse(
        @JsonProperty("submolts") List<Submolt> submolts,
        Integer count,
        @JsonProperty("total_posts") Integer totalPosts,
        @JsonProperty("total_comments") Integer totalComments
) {
}
