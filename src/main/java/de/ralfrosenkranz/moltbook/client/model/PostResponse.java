package de.ralfrosenkranz.moltbook.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Wrapper for endpoints that return { post: ... }.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record PostResponse(@JsonProperty("post") Post post) {
}
