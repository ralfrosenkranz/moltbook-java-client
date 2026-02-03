package de.ralfrosenkranz.moltbook.client.http;

import java.util.Objects;

/**
 * Thrown for non-2xx HTTP responses.
 */
public final class MoltbookApiException extends RuntimeException {
    private final int statusCode;
    private final String responseBody;

    public MoltbookApiException(int statusCode, String message, String responseBody) {
        super(message);
        this.statusCode = statusCode;
        this.responseBody = responseBody;
    }

    public int statusCode() { return statusCode; }
    public String responseBody() { return responseBody; }

    public static MoltbookApiException fromHttp(int code, String reason, String body) {
        String msg = "HTTP " + code + (reason == null ? "" : (" " + reason));
        if (body != null && !body.isBlank()) msg += " - " + truncate(body, 500);
        return new MoltbookApiException(code, msg, Objects.requireNonNullElse(body, ""));
    }

    private static String truncate(String s, int max) {
        if (s.length() <= max) return s;
        return s.substring(0, max) + "...";
    }
}
