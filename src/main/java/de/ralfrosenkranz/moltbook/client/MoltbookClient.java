package de.ralfrosenkranz.moltbook.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import de.ralfrosenkranz.moltbook.client.api.*;
import de.ralfrosenkranz.moltbook.client.http.MoltbookHttp;
import de.ralfrosenkranz.moltbook.client.http.RawApi;
import okhttp3.OkHttpClient;

import java.util.Objects;

/**
 * Main entry point for the Moltbook Java client.
 */
public final class MoltbookClient implements AutoCloseable {
    private final MoltbookClientConfig config;
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final MoltbookHttp http;
    private final AgentsApi agents;
    private final PostsApi posts;
    private final CommentsApi comments;
    private final VotingApi voting;
    private final SubmoltsApi submolts;
    private final FollowingApi following;
    private final FeedApi feed;
    private final SearchApi search;
    private final RawApi raw;

    private MoltbookClient(Builder b) {
        this.config = Objects.requireNonNull(b.config, "config");
        this.objectMapper = Objects.requireNonNullElseGet(b.objectMapper, () -> {
            ObjectMapper om = new ObjectMapper();
            om.registerModule(new JavaTimeModule());
            return om;
        });

        this.httpClient = Objects.requireNonNullElseGet(b.httpClient, () ->
                MoltbookHttp.defaultOkHttp(config)
        );

        this.http = new MoltbookHttp(config, httpClient, objectMapper);
        this.agents = new AgentsApi(http);
        this.posts = new PostsApi(http);
        this.comments = new CommentsApi(http);
        this.voting = new VotingApi(http);
        this.submolts = new SubmoltsApi(http);
        this.following = new FollowingApi(http);
        this.feed = new FeedApi(http);
        this.search = new SearchApi(http);
        this.raw = new RawApi(http);
    }

    public MoltbookClientConfig config() { return config; }

    public AgentsApi getAgentApi() { return agents; }
    public PostsApi getPostsApi() { return posts; }
    public CommentsApi getCommentsApi() { return comments; }
    public VotingApi getVotingApi() { return voting; }
    public SubmoltsApi getSubmoltsApi() { return submolts; }
    public FollowingApi getFollowingApi() { return following; }
    public FeedApi getFeedApi() { return feed; }
    public SearchApi getSearchApi() { return search; }

    /**
     * Generic endpoint access for undocumented/new endpoints.
     */
    public RawApi raw() { return raw; }

    public static Builder builder() { return new Builder(); }

    @Override
    public void close() {
        // OkHttp resources are managed by OkHttp; nothing to close explicitly here.
    }

    public static final class Builder {
        private MoltbookClientConfig config;
        private OkHttpClient httpClient;
        private ObjectMapper objectMapper;

        public Builder config(MoltbookClientConfig config) { this.config = config; return this; }
        public Builder httpClient(OkHttpClient httpClient) { this.httpClient = httpClient; return this; }
        public Builder objectMapper(ObjectMapper objectMapper) { this.objectMapper = objectMapper; return this; }

        public MoltbookClient build() { return new MoltbookClient(this); }
    }
}
