package de.ralfrosenkranz.moltbook.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Wrapper for endpoints that return { agent: ... }.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AgentMeResponse(@JsonProperty("agent") AgentMe agent) {
}
