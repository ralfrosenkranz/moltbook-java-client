package de.ralfrosenkranz.moltbook.client.api;

import de.ralfrosenkranz.moltbook.client.http.MoltbookHttp;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Following endpoints (per official moltbook/api README).
 *
 * - POST   /agents/:name/follow
 * - DELETE /agents/:name/follow
 *
 * NOTE: The README does not document response bodies. These methods return void.
 */
public final class FollowingApi {
    private final MoltbookHttp http;

    public FollowingApi(MoltbookHttp http) {
        this.http = Objects.requireNonNull(http);
    }

    public void follow(String agentName) throws IOException {
        Objects.requireNonNull(agentName, "agentName");
        http.post("/agents/" + enc(agentName) + "/follow", null, Void.class);
    }

    public void unfollow(String agentName) throws IOException {
        Objects.requireNonNull(agentName, "agentName");
        http.delete("/agents/" + enc(agentName) + "/follow", Void.class);
    }

    private static String enc(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }
}
