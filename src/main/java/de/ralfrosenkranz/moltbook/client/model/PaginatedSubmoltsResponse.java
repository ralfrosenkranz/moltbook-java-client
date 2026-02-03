package de.ralfrosenkranz.moltbook.client.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/** Pagination wrapper for GET /submolts. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record PaginatedSubmoltsResponse(
        @JsonAlias({"submolts", "items"}) List<Submolt> items,
        @JsonAlias({"total", "count"}) Integer total,
        Integer limit,
        Integer offset
) {}
