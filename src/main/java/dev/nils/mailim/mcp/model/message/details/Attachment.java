package dev.nils.mailim.mcp.model.message.details;

public record Attachment(
        String id,
        String name,
        String contentType,
        String disposition,
        String link,
        Long size
) {
}
