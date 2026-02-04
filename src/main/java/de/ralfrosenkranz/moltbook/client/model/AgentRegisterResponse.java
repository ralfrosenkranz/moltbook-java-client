package de.ralfrosenkranz.moltbook.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.util.Map;

public record AgentRegisterResponse(
        @JsonProperty("success") boolean success,
        @JsonProperty("message") String message,
        @JsonProperty("agent") AgentRegisterResponseAgent agent,
        @JsonProperty("setup") Map<String, SetupStep> setup,
        @JsonProperty("skill_files") Map<String, String> skillFiles,
        @JsonProperty("tweet_template") String tweetTemplate,
        @JsonProperty("status") String status
) {
    public record AgentRegisterResponseAgent(
            @JsonProperty("id") String id,
            @JsonProperty("name") String name,
            @JsonProperty("api_key") String apiKey,
            @JsonProperty("claim_url") String claimUrl,
            @JsonProperty("verification_code") String verificationCode,
            @JsonProperty("profile_url") String profileUrl,
            @JsonProperty("created_at") String createdAt
    ) {
    }

    public record SetupStep(
            @JsonProperty("action") String action,
            @JsonProperty("details") String details,
            @JsonProperty("url") String url,
            @JsonProperty("why") String why,
            @JsonProperty("critical") boolean critical,
            @JsonProperty("message_template") String messageTemplate
    ) {
        public String asJson() {
            try {
                ObjectMapper mapper = new ObjectMapper();
                mapper.enable(SerializationFeature.INDENT_OUTPUT);
                return mapper.writeValueAsString(this);
            } catch (Exception e) {
                throw new RuntimeException("Failed to serialize SetupStep", e);
            }
        }
    }

    public String asJson() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            return mapper.writeValueAsString(this);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize AgentRegisterResponse", e);
        }
    }
}
