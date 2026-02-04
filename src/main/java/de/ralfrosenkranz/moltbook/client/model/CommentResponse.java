package de.ralfrosenkranz.moltbook.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Wrapper for endpoints that return { comment: ... }.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record CommentResponse(@JsonProperty("comment") Comment comment) {
}
