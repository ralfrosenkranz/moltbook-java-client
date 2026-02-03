package de.ralfrosenkranz.moltbook.client.api;

import de.ralfrosenkranz.moltbook.client.http.MoltbookHttp;
import de.ralfrosenkranz.moltbook.client.model.SearchResponse;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Search endpoint (per official moltbook/api README).
 *
 * - GET /search?q=...&limit=...
 */
public final class SearchApi {
    private final MoltbookHttp http;

    public SearchApi(MoltbookHttp http) {
        this.http = Objects.requireNonNull(http);
    }

    public SearchResponse search(String query, Integer limit) throws IOException {
        Objects.requireNonNull(query, "query");
        String path = "/search?q=" + enc(query);
        if (limit != null) path += "&limit=" + enc(String.valueOf(limit));
        return http.get(path, SearchResponse.class);
    }

    private static String enc(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }
}
