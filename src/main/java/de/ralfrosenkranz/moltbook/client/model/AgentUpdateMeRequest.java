package de.ralfrosenkranz.moltbook.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * PATCH /agents/me payload (documented field: description).
 * Additional updatable fields may exist.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AgentUpdateMeRequest(
        String description
) {}
