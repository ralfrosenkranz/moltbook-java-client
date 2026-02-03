package de.ralfrosenkranz.moltbook.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Generic {"success": true} responses. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ApiSuccessResponse(boolean success) {}
