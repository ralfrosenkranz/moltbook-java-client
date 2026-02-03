package de.ralfrosenkranz.moltbook.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Wrapper for endpoints that return { agent: ... }. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AgentMeResponse(AgentMe agent) {}
