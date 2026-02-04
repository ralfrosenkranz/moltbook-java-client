package de.ralfrosenkranz.moltbook.client.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * POST /posts payload.
 * <p>
 * The official docs show: { submolt, title, content } for text posts.
 * For link posts, servers often accept a URL field; if Moltbook uses a different field name,
 * use client.raw() until verified.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public record PostCreateRequest(
        @JsonProperty("submolt") String submolt,
        @JsonProperty("title") String title,
        @JsonProperty("content") String content,
        @JsonProperty("url") String url
) {
    public static PostCreateRequest text(String submolt, String title, String content) {
        return new PostCreateRequest(submolt, title, content, null);
    }

    public static PostCreateRequest link(String submolt, String title, String url) {
        return new PostCreateRequest(submolt, title, null, url);
    }
}
