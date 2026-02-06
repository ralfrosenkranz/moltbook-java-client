package de.ralfrosenkranz.moltbook.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Author/User reference.
 *
 * Moltbook responses sometimes encode the author as a string (e.g. an id/handle)
 * and sometimes as a full object.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record Author(
        @JsonProperty("id") String id,
        @JsonProperty("name") String name,
        @JsonProperty("username") String username,
        @JsonProperty("handle") String handle,
        @JsonProperty("url") String url
) {
    public String displayLabel() {
        if (name != null && !name.isBlank()) return name;
        if (username != null && !username.isBlank()) return username;
        if (handle != null && !handle.isBlank()) return handle;
        return id;
    }
}
