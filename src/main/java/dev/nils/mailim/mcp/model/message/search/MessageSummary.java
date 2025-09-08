package dev.nils.mailim.mcp.model.message.search;

public record MessageSummary(String from,
                             String subject,
                             String firstLine,
                             boolean calendarInvite,
                             int priority,
                             String id,
                             Long epochSeconds) {
}
