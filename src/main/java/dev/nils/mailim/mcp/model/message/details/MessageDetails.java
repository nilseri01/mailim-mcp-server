package dev.nils.mailim.mcp.model.message.details;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record MessageDetails(
        String id,
        Headers headers,
        String content,
        @JsonProperty("content_type") String contentType,
        List<Part> parts
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Headers(
            String subject,
            Long time
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Part(
            String id,
            String name,
            @JsonProperty("content-type") String contentType,
            String disposition,
            String link,
            Long size
    ) {
    }
}
