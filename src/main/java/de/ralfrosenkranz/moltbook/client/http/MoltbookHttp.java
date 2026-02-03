package de.ralfrosenkranz.moltbook.client.http;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.ralfrosenkranz.moltbook.client.MoltbookClientConfig;
import okhttp3.*;
import okio.Buffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Thin HTTP wrapper: JSON serialization, auth header, error mapping, retry/backoff.
 */
public final class MoltbookHttp {
    private Logger log = LoggerFactory.getLogger(MoltbookHttp.class);
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private final MoltbookClientConfig config;
    private final OkHttpClient client;
    private final ObjectMapper om;

    public MoltbookHttp(MoltbookClientConfig config, OkHttpClient client, ObjectMapper om) {
        this.config = Objects.requireNonNull(config);
        this.client = Objects.requireNonNull(client);
        this.om = Objects.requireNonNull(om);
    }

    public MoltbookClientConfig config() { return config; }
    public ObjectMapper objectMapper() { return om; }

    public <T> T get(String path, Class<T> clazz) throws IOException {
        Request req = baseRequest(path, null).get().build();
        return execute(req, clazz);
    }

    public <T> T get(String path, Map<String, String> query, Class<T> clazz) throws IOException {
        Request req = baseRequest(path, query).get().build();
        return execute(req, clazz);
    }

    public <T> T post(String path, Object body, Class<T> clazz) throws IOException {
        RequestBody rb = body == null ? RequestBody.create(new byte[0], JSON) : RequestBody.create(toJson(body), JSON);
        Request req = baseRequest(path, null).post(rb).build();
        return execute(req, clazz);
    }

    public <T> T patch(String path, Object body, Class<T> clazz) throws IOException {
        RequestBody rb = body == null ? RequestBody.create(new byte[0], JSON) : RequestBody.create(toJson(body), JSON);
        Request req = baseRequest(path, null).patch(rb).build();
        return execute(req, clazz);
    }

    public <T> T delete(String path, Class<T> clazz) throws IOException {
        Request req = baseRequest(path, null).delete().build();
        return execute(req, clazz);
    }

    private Request.Builder baseRequest(String path, Map<String, String> query) {
        String base = Objects.requireNonNull(config.baseUrl(), "baseUrl");
        String full = joinUrl(base, path);
        HttpUrl parsed = HttpUrl.parse(full);
        if (parsed == null) throw new IllegalArgumentException("Invalid URL: " + full);

        HttpUrl.Builder ub = parsed.newBuilder();
        if (query != null && !query.isEmpty()) {
            for (Map.Entry<String, String> e : query.entrySet()) {
                if (e.getKey() == null || e.getValue() == null) continue;
                ub.addQueryParameter(e.getKey(), e.getValue());
            }
        }
        HttpUrl url = ub.build();

        Request.Builder b = new Request.Builder()
                .url(url)
                .header("Accept", "application/json");

        if (config.apiKey() != null && !config.apiKey().isBlank()) {
            b.header("Authorization", "Bearer " + config.apiKey());
        }
        return b;
    }

    private static String joinUrl(String base, String path) {
        String b = base.endsWith("/") ? base.substring(0, base.length() - 1) : base;
        String p = (path == null || path.isBlank()) ? "" : (path.startsWith("/") ? path : "/" + path);
        return b + p;
    }

    private byte[] toJson(Object body) throws JsonProcessingException {
        return om.writeValueAsBytes(body);
    }

    private <T> T execute(Request request, Class<T> clazz) throws IOException {
        int attempt = 0;
        IOException last = null;

        if (clazz == Void.class) return null;

        while (attempt <= config.maxRetries()) {
            attempt++;
            try (Response resp = client.newCall(request).execute()) {
                String requestAsString = request.toString();
                String message = "-> Moltbook-Request:\n" + requestAsString + "\n";
                //log.debug(message);
                System.out.println (message);

                ResponseBody rb = resp.body();
                if (rb == null) return null;
                byte[] bytes = rb.bytes();

                //if (log.isDebugEnabled()) {
                    String responseAsString = new String(bytes);
                    message = "<- Moltbook-Response:\n" + responseAsString;
                    //log.debug(message);
                    System.out.println (message);
                //}

                if (resp.isSuccessful()) {
                    if (bytes.length == 0) return null;
                    return om.readValue(bytes, clazz);
                } else {
                    int i = 0;
                }

                int code = resp.code();
                String bodyText = safeBody(resp);

                // Retry on 429 or 5xx
                if (shouldRetry(code) && attempt <= config.maxRetries()) {
                    sleepBackoff(resp, attempt);
                    continue;
                }

                throw MoltbookApiException.fromHttp(code, resp.message(), bodyText);
            } catch (SocketTimeoutException e) {
                last = e;
                if (attempt <= config.maxRetries()) {
                    sleepJitter(attempt);
                    continue;
                }
                throw e;
            } catch (IOException e) {
                last = e;
                if (attempt <= config.maxRetries()) {
                    sleepJitter(attempt);
                    continue;
                }
                throw e;
            }
        }
        throw last == null ? new IOException("Request failed") : last;
    }

    private static boolean shouldRetry(int code) {
        return code == 429 || (code >= 500 && code <= 599);
    }

    private static String safeBody(Response resp) {
        try {
            ResponseBody rb = resp.body();
            if (rb == null) return "";
            return rb.string();
        } catch (Exception ignored) {
            return "";
        }
    }

    private static void sleepBackoff(Response resp, int attempt) {
        // If Retry-After exists (seconds), respect it, else exponential backoff with jitter.
        long millis = Optional.ofNullable(resp.header("Retry-After"))
                .flatMap(v -> {
                    try { return Optional.of(Long.parseLong(v.trim()) * 1000L); } catch (Exception e) { return Optional.empty(); }
                })
                .orElseGet(() -> backoffMillis(attempt));
        try { Thread.sleep(millis); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
    }

    private static void sleepJitter(int attempt) {
        long millis = backoffMillis(attempt);
        try { Thread.sleep(millis); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
    }

    private static long backoffMillis(int attempt) {
        long base = (long) (Math.pow(2, Math.min(attempt, 6)) * 200L); // 200ms, 400ms, ... capped
        long jitter = ThreadLocalRandom.current().nextLong(0, 200L);
        return Math.min(base + jitter, 10_000L);
    }

    public static OkHttpClient defaultOkHttp(MoltbookClientConfig cfg) {
        OkHttpClient.Builder b = new OkHttpClient.Builder();
        b.callTimeout(cfg.callTimeout());
        b.connectTimeout(cfg.connectTimeout());
        b.readTimeout(cfg.readTimeout());
        b.writeTimeout(cfg.writeTimeout());
        return b.build();
    }
}
