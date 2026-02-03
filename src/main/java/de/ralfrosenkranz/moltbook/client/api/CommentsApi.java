package de.ralfrosenkranz.moltbook.client.api;

import de.ralfrosenkranz.moltbook.client.http.MoltbookHttp;
import de.ralfrosenkranz.moltbook.client.model.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Comments endpoints (per official moltbook/api README).
 *
 * - POST /posts/:id/comments
 * - GET  /posts/:id/comments?sort=top|new|controversial
 */
public final class CommentsApi {
    private final MoltbookHttp http;

    public CommentsApi(MoltbookHttp http) {
        this.http = Objects.requireNonNull(http);
    }

    /** POST /posts/{id}/comments */
    public Comment createComment(String postId, CommentCreateRequest request) throws IOException {
        Objects.requireNonNull(postId, "postId");
        Objects.requireNonNull(request, "request");
        return http.post("/posts/" + enc(postId) + "/comments", request, CommentResponse.class).comment();
    }

    /** GET /posts/{id}/comments */
    public Comment[] getComments(String postId, String sort, Integer limit) throws IOException {
        Objects.requireNonNull(postId, "postId");
        java.util.Map<String, String> q = new java.util.LinkedHashMap<>();
        if (sort != null && !sort.isBlank()) q.put("sort", sort.trim());
        if (limit != null) q.put("limit", String.valueOf(limit));
        return http.get("/posts/" + enc(postId) + "/comments", q, CommentsResponse.class).comments();
    }

    /** DELETE /comments/{id} */
    public ApiSuccessResponse deleteComment(String commentId) throws IOException {
        Objects.requireNonNull(commentId, "commentId");
        return http.delete("/comments/" + enc(commentId), ApiSuccessResponse.class);
    }

    /** POST /comments/{id}/upvote */
    public VoteActionResponse upvoteComment(String commentId) throws IOException {
        Objects.requireNonNull(commentId, "commentId");
        return http.post("/comments/" + enc(commentId) + "/upvote", null, VoteActionResponse.class);
    }

    /** POST /comments/{id}/downvote */
    public VoteActionResponse downvoteComment(String commentId) throws IOException {
        Objects.requireNonNull(commentId, "commentId");
        return http.post("/comments/" + enc(commentId) + "/downvote", null, VoteActionResponse.class);
    }

    private static String enc(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }
}
