package de.ralfrosenkranz.moltbook.client.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request body for POST /posts/:id/comments
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record CommentCreateRequest(
        String content,
        @JsonProperty("parent_id") String parentId
) {}
