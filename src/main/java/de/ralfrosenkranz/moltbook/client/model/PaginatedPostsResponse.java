package de.ralfrosenkranz.moltbook.client.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * Posts pagination wrapper used by /posts, /feed, and /submolts/{name}/feed.
 *
 * The frontend expects a paginated shape. The backend has historically used
 * either { posts: [...] } or { items: [...] } plus count/total.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record PaginatedPostsResponse(
        @JsonAlias({"posts", "items"}) List<Post> items,
        @JsonAlias({"total", "count"}) Integer total,
        Integer limit,
        Integer offset
) {}
