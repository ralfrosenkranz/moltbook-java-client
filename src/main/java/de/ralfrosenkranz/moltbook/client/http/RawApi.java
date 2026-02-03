package de.ralfrosenkranz.moltbook.client.http;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.StringJoiner;

/**
 * Generic access layer for endpoints not yet mapped by typed APIs.
 *
 * IMPORTANT: Only use paths you have verified in Moltbook documentation.
 */
public final class RawApi {
    private final MoltbookHttp http;

    public RawApi(MoltbookHttp http) {
        this.http = http;
    }

    public <T> T get(String path, Class<T> clazz) throws IOException {
        return http.get(path, clazz);
    }

    public <T> T get(String path, Map<String, String> query, Class<T> clazz) throws IOException {
        String p = path + toQuery(query);
        return http.get(p, clazz);
    }

    public <T> T post(String path, Object body, Class<T> clazz) throws IOException {
        return http.post(path, body, clazz);
    }

    public <T> T patch(String path, Object body, Class<T> clazz) throws IOException {
        return http.patch(path, body, clazz);
    }

    public <T> T delete(String path, Class<T> clazz) throws IOException {
        return http.delete(path, clazz);
    }

    private static String toQuery(Map<String, String> query) {
        if (query == null || query.isEmpty()) return "";
        StringJoiner sj = new StringJoiner("&", "?", "");
        for (var e : query.entrySet()) {
            sj.add(URLEncoder.encode(e.getKey(), StandardCharsets.UTF_8) + "=" +
                    URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8));
        }
        return sj.toString();
    }
}
