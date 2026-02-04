package de.ralfrosenkranz.moltbook.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Generic {"success": true} responses.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ApiSuccessResponse(@JsonProperty("success") boolean success) {
}
