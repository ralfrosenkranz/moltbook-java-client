package de.ralfrosenkranz.moltbook.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Response for GET /posts/{id}/comments ... */
@JsonIgnoreProperties(ignoreUnknown = true)
public record CommentsResponse(Comment[] comments) {}
