package dev.nils.mailim.mcp.model.message.search;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record MessageItemResponse(
        @JsonProperty("id") String id,
        @JsonProperty("subject") String subject,
        @JsonProperty("first_line") String firstLine,
        @JsonProperty("calendar_invite") Integer calendarInvite,
        @JsonProperty("priority") Integer priority,
        @JsonProperty("time") Long time,
        @JsonProperty("from") List<From> from
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record From(
            @JsonProperty("full_name") String fullName,
            String email
    ) {
    }
}


