package de.ralfrosenkranz.moltbook.client.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Pagination wrapper for GET /submolts.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record PaginatedSubmoltsResponse(
        @JsonAlias({"submolts", "items"}) @JsonProperty("items") List<Submolt> items,
        @JsonAlias({"total", "count"}) @JsonProperty("total") Integer total,
        @JsonProperty("limit") Integer limit,
        @JsonProperty("offset") Integer offset
) {
}
