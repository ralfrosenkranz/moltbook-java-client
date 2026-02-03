package de.ralfrosenkranz.moltbook.client.api;

import de.ralfrosenkranz.moltbook.client.http.MoltbookHttp;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Voting endpoints (per official moltbook/api README).
 *
 * - POST /posts/:id/upvote
 * - POST /posts/:id/downvote
 * - POST /comments/:id/upvote
 *
 * NOTE: The README does not document response bodies. These methods return void.
 */
public final class VotingApi {
    private final MoltbookHttp http;

    public VotingApi(MoltbookHttp http) {
        this.http = Objects.requireNonNull(http);
    }

    public void upvotePost(String postId) throws IOException {
        Objects.requireNonNull(postId, "postId");
        http.post("/posts/" + enc(postId) + "/upvote", null, Void.class);
    }

    public void downvotePost(String postId) throws IOException {
        Objects.requireNonNull(postId, "postId");
        http.post("/posts/" + enc(postId) + "/downvote", null, Void.class);
    }

    public void upvoteComment(String commentId) throws IOException {
        Objects.requireNonNull(commentId, "commentId");
        http.post("/comments/" + enc(commentId) + "/upvote", null, Void.class);
    }

    private static String enc(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }
}
