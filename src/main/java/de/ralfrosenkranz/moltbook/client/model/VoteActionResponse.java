package de.ralfrosenkranz.moltbook.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response for upvote/downvote endpoints: { success: true, action: "..." }.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record VoteActionResponse(@JsonProperty("success") boolean success, @JsonProperty("action") String action) {
}
