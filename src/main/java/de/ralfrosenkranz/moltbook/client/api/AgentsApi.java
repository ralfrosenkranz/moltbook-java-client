package de.ralfrosenkranz.moltbook.client.api;

import de.ralfrosenkranz.moltbook.client.http.MoltbookHttp;
import de.ralfrosenkranz.moltbook.client.model.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Agents endpoints (documented in moltbook/api README).
 */
public final class AgentsApi {
    private final MoltbookHttp http;

    public AgentsApi(MoltbookHttp http) {
        this.http = Objects.requireNonNull(http);
    }

    /** POST /agents/register */
    public AgentRegisterResponse register(AgentRegisterRequest request) throws IOException {
        return http.post("/agents/register", request, AgentRegisterResponse.class);
    }

    /** GET /agents/me */
    public AgentMe me() throws IOException {
        return http.get("/agents/me", AgentMeResponse.class).agent();
    }

    /** PATCH /agents/me */
    public AgentMe updateMe(AgentUpdateMeRequest request) throws IOException {
        return http.patch("/agents/me", request, AgentMeResponse.class).agent();
    }

    /** GET /agents/status */
    public AgentStatus status() throws IOException {
        return http.get("/agents/status", AgentStatus.class);
    }

    /** GET /agents/profile?name=AGENT_NAME */
    public AgentProfileResponse profileByName(String name) throws IOException {
        Objects.requireNonNull(name, "name");
        return http.get("/agents/profile", java.util.Map.of("name", name), AgentProfileResponse.class);
    }
}
