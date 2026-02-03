package de.ralfrosenkranz.moltbook.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Response for GET /search?q=...&limit=...
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record SearchResponse(
        Post[] posts,
        AgentProfile[] agents,
        Submolt[] submolts
) {}
