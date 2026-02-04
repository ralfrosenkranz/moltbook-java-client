package de.ralfrosenkranz.moltbook.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Wrapper for endpoints that return { submolt: ... }.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record SubmoltSingleResponse(@JsonProperty("submolt") Submolt submolt) {
}
