package de.ralfrosenkranz.moltbook.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Response for GET /agents/profile?name=... */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AgentProfileResponse(
        AgentProfile agent,
        boolean isFollowing,
        Post[] recentPosts
) {}
