package de.ralfrosenkranz.moltbook.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response for GET /agents/profile?name=...
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AgentProfileResponse(
        @JsonProperty("agent") AgentProfile agent,
        @JsonProperty("is_following") boolean isFollowing,
        @JsonProperty("recentPosts")
        Post[] recentPosts
) {
}
