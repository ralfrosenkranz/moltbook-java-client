package de.ralfrosenkranz.moltbook.client;

import java.time.Duration;
import java.util.Objects;

/**
 * Client configuration.
 */
public final class MoltbookClientConfig {
    private final String baseUrl;
    private final String apiKey;
    private final Duration callTimeout;
    private final Duration connectTimeout;
    private final Duration readTimeout;
    private final Duration writeTimeout;
    private final int maxRetries;

    private MoltbookClientConfig(Builder b) {
        this.baseUrl = Objects.requireNonNullElse(b.baseUrl, "https://www.moltbook.com/api/v1");
        this.apiKey = b.apiKey;
        this.callTimeout = Objects.requireNonNullElse(b.callTimeout, Duration.ofSeconds(30));
        this.connectTimeout = Objects.requireNonNullElse(b.connectTimeout, Duration.ofSeconds(10));
        this.readTimeout = Objects.requireNonNullElse(b.readTimeout, Duration.ofSeconds(30));
        this.writeTimeout = Objects.requireNonNullElse(b.writeTimeout, Duration.ofSeconds(30));
        this.maxRetries = b.maxRetries == null ? 3 : b.maxRetries;
    }

    public String baseUrl() { return baseUrl; }
    public String apiKey() { return apiKey; }
    public Duration callTimeout() { return callTimeout; }
    public Duration connectTimeout() { return connectTimeout; }
    public Duration readTimeout() { return readTimeout; }
    public Duration writeTimeout() { return writeTimeout; }
    public int maxRetries() { return maxRetries; }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private String baseUrl;
        private String apiKey;
        private Duration callTimeout;
        private Duration connectTimeout;
        private Duration readTimeout;
        private Duration writeTimeout;
        private Integer maxRetries;

        public Builder baseUrl(String baseUrl) { this.baseUrl = baseUrl; return this; }
        public Builder apiKey(String apiKey) { this.apiKey = apiKey; return this; }
        public Builder callTimeout(Duration callTimeout) { this.callTimeout = callTimeout; return this; }
        public Builder connectTimeout(Duration connectTimeout) { this.connectTimeout = connectTimeout; return this; }
        public Builder readTimeout(Duration readTimeout) { this.readTimeout = readTimeout; return this; }
        public Builder writeTimeout(Duration writeTimeout) { this.writeTimeout = writeTimeout; return this; }
        public Builder maxRetries(int maxRetries) { this.maxRetries = maxRetries; return this; }

        public MoltbookClientConfig build() { return new MoltbookClientConfig(this); }
    }
}
