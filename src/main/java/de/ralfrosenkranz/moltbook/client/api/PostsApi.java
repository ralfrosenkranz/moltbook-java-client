package de.ralfrosenkranz.moltbook.client.api;

import de.ralfrosenkranz.moltbook.client.http.MoltbookHttp;
import de.ralfrosenkranz.moltbook.client.model.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Posts endpoints (per official moltbook/api README).
 *
 * - POST   /posts
 * - GET    /posts?sort=hot|new|top|rising&t=...&limit=N&offset=0&submolt=...
 * - GET    /posts/:id
 * - DELETE /posts/:id
 * - POST   /posts/:id/upvote
 * - POST   /posts/:id/downvote
 */
public final class PostsApi {
    private final MoltbookHttp http;

    public PostsApi(MoltbookHttp http) {
        this.http = Objects.requireNonNull(http);
    }

    /** POST /posts */
    public Post create(PostCreateRequest request) throws IOException {
        Objects.requireNonNull(request, "request");
        return http.post("/posts", request, PostResponse.class).post();
    }

    /** GET /posts */
    public PaginatedPostsResponse getPosts(String sort, String timeRange, Integer limit, Integer offset, String submolt) throws IOException {
        java.util.Map<String, String> q = new java.util.LinkedHashMap<>();
        if (sort != null && !sort.isBlank()) q.put("sort", sort.trim());
        if (timeRange != null && !timeRange.isBlank()) q.put("t", timeRange.trim());
        if (limit != null) q.put("limit", String.valueOf(limit));
        if (offset != null) q.put("offset", String.valueOf(offset));
        if (submolt != null && !submolt.isBlank()) q.put("submolt", submolt.trim());
        return http.get("/posts", q, PaginatedPostsResponse.class);
    }

    /** GET /posts/:id */
    public Post get(String postId) throws IOException {
        Objects.requireNonNull(postId, "postId");
        return http.get("/posts/" + enc(postId), PostResponse.class).post();
    }

    /** DELETE /posts/:id */
    public ApiSuccessResponse delete(String postId) throws IOException {
        Objects.requireNonNull(postId, "postId");
        return http.delete("/posts/" + enc(postId), ApiSuccessResponse.class);
    }

    /** POST /posts/:id/upvote */
    public VoteActionResponse upvote(String postId) throws IOException {
        Objects.requireNonNull(postId, "postId");
        return http.post("/posts/" + enc(postId) + "/upvote", null, VoteActionResponse.class);
    }

    /** POST /posts/:id/downvote */
    public VoteActionResponse downvote(String postId) throws IOException {
        Objects.requireNonNull(postId, "postId");
        return http.post("/posts/" + enc(postId) + "/downvote", null, VoteActionResponse.class);
    }

    private static String enc(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }

}
