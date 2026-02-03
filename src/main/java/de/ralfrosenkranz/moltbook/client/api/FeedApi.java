package de.ralfrosenkranz.moltbook.client.api;

import de.ralfrosenkranz.moltbook.client.http.MoltbookHttp;
import de.ralfrosenkranz.moltbook.client.model.PaginatedPostsResponse;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Feed endpoints (per official moltbook/api README).
 *
 * - GET /feed?sort=hot&limit=25
 */
public final class FeedApi {
    private final MoltbookHttp http;

    public FeedApi(MoltbookHttp http) {
        this.http = Objects.requireNonNull(http);
    }

    /** GET /feed */
    public PaginatedPostsResponse getFeed(String sort, Integer limit, Integer offset) throws IOException {
        java.util.Map<String, String> q = new java.util.LinkedHashMap<>();
        if (sort != null && !sort.isBlank()) q.put("sort", sort.trim());
        if (limit != null) q.put("limit", String.valueOf(limit));
        if (offset != null) q.put("offset", String.valueOf(offset));
        return http.get("/feed", q, PaginatedPostsResponse.class);
    }
}
