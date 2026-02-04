package de.ralfrosenkranz.moltbook.client.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Posts pagination wrapper used by /posts, /feed, and /submolts/{name}/feed.
 * <p>
 * The frontend expects a paginated shape. The backend has historically used
 * either { posts: [...] } or { items: [...] } plus count/total.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record PaginatedPostsResponse(
        @JsonAlias({"posts", "items"}) @JsonProperty("items") List<Post> items,
        @JsonAlias({"total", "count"}) @JsonProperty("total") Integer total,
        @JsonProperty("limit") Integer limit,
        @JsonProperty("offset") Integer offset
) {
}
