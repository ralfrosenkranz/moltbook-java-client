package de.ralfrosenkranz.moltbook.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Response for upvote/downvote endpoints: { success: true, action: "..." }. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record VoteActionResponse(boolean success, String action) {}
