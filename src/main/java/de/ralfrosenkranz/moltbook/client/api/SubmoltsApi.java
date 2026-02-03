package de.ralfrosenkranz.moltbook.client.api;

import de.ralfrosenkranz.moltbook.client.http.MoltbookHttp;
import de.ralfrosenkranz.moltbook.client.model.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Submolts (communities) endpoints (per official moltbook/api README).
 *
 * - POST   /submolts
 * - GET    /submolts
 * - GET    /submolts/:name
 * - POST   /submolts/:name/subscribe
 * - DELETE /submolts/:name/subscribe
 */
public final class SubmoltsApi {
    private final MoltbookHttp http;

    public SubmoltsApi(MoltbookHttp http) {
        this.http = Objects.requireNonNull(http);
    }

    /** POST /submolts */
    public Submolt createSubmolt(SubmoltCreateRequest request) throws IOException {
        Objects.requireNonNull(request, "request");
        return http.post("/submolts", request, SubmoltSingleResponse.class).submolt();
    }

    /** GET /submolts */
    public PaginatedSubmoltsResponse getSubmolts(String sort, Integer limit, Integer offset) throws IOException {
        java.util.Map<String, String> q = new java.util.LinkedHashMap<>();
        if (sort != null && !sort.isBlank()) q.put("sort", sort.trim());
        if (limit != null) q.put("limit", String.valueOf(limit));
        if (offset != null) q.put("offset", String.valueOf(offset));
        return http.get("/submolts", q, PaginatedSubmoltsResponse.class);
    }

    /** GET /submolts/{name} */
    public Submolt getSubmolt(String name) throws IOException {
        Objects.requireNonNull(name, "name");
        return http.get("/submolts/" + enc(name), SubmoltSingleResponse.class).submolt();
    }

    /** POST /submolts/{name}/subscribe */
    public ApiSuccessResponse subscribeSubmolt(String name) throws IOException {
        Objects.requireNonNull(name, "name");
        return http.post("/submolts/" + enc(name) + "/subscribe", null, ApiSuccessResponse.class);
    }

    /** DELETE /submolts/{name}/subscribe */
    public ApiSuccessResponse unsubscribeSubmolt(String name) throws IOException {
        Objects.requireNonNull(name, "name");
        return http.delete("/submolts/" + enc(name) + "/subscribe", ApiSuccessResponse.class);
    }

    /** GET /submolts/{name}/feed */
    public PaginatedPostsResponse getSubmoltFeed(String name, String sort, Integer limit, Integer offset) throws IOException {
        Objects.requireNonNull(name, "name");
        java.util.Map<String, String> q = new java.util.LinkedHashMap<>();
        if (sort != null && !sort.isBlank()) q.put("sort", sort.trim());
        if (limit != null) q.put("limit", String.valueOf(limit));
        if (offset != null) q.put("offset", String.valueOf(offset));
        return http.get("/submolts/" + enc(name) + "/feed", q, PaginatedPostsResponse.class);
    }

    private static String enc(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }
}
