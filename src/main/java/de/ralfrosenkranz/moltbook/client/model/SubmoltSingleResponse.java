package de.ralfrosenkranz.moltbook.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Wrapper for endpoints that return { submolt: ... }. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record SubmoltSingleResponse(Submolt submolt) {}
