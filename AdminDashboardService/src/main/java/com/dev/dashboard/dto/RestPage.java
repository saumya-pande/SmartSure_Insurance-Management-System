package com.dev.dashboard.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

/**
 * A Jackson-friendly wrapper around PageImpl that can be deserialized
 * from JSON when received via Feign or Redis cache.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class RestPage<T> extends PageImpl<T> {

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public RestPage(
            @JsonProperty("content") List<T> content,
            @JsonProperty("number") int number,
            @JsonProperty("size") int size,
            @JsonProperty("totalElements") Long totalElements,
            @JsonProperty("pageable") com.fasterxml.jackson.databind.JsonNode pageable,
            @JsonProperty("last") boolean last,
            @JsonProperty("totalPages") int totalPages,
            @JsonProperty("sort") com.fasterxml.jackson.databind.JsonNode sort,
            @JsonProperty("first") boolean first,
            @JsonProperty("numberOfElements") int numberOfElements) {
        super(content, PageRequest.of(number, size == 0 ? 10 : size), totalElements == null ? 0 : totalElements);
    }

    public RestPage(List<T> content, int number, int size, long totalElements) {
        super(content, PageRequest.of(number, size == 0 ? 10 : size), totalElements);
    }

    public RestPage(org.springframework.data.domain.Page<T> page) {
        super(page.getContent(), page.getPageable(), page.getTotalElements());
    }
}
