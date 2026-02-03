package de.ralfrosenkranz.moltbook.client;

import de.ralfrosenkranz.moltbook.client.model.AgentMe;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MoltbookClientTest {

    @Test
    void addsAuthHeaderAndParsesJson() throws Exception {
        try (MockWebServer server = new MockWebServer()) {
            server.enqueue(new MockResponse()
                    .setResponseCode(200)
                    .setHeader("Content-Type", "application/json")
                    .setBody("{\"id\":\"1\",\"name\":\"bot\",\"description\":\"d\"}"));

            server.start();

            MoltbookClient client = MoltbookClient.builder()
                    .config(MoltbookClientConfig.builder()
                            .baseUrl(server.url("/api/v1").toString().replaceAll("/$", ""))
                            .apiKey("moltbook_test")
                            .build())
                    .build();

            AgentMe me = client.getAgentApi().me();
            assertEquals("1", me.id());
            assertEquals("bot", me.name());

            var recorded = server.takeRequest();
            assertEquals("GET", recorded.getMethod());
            assertEquals("Bearer moltbook_test", recorded.getHeader("Authorization"));
            assertEquals("/api/v1/agents/me", recorded.getPath());
        }
    }
}
