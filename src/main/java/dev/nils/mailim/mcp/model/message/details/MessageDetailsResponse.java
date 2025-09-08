package dev.nils.mailim.mcp.model.message.details;

import java.util.List;

public record MessageDetailsResponse(
        String id,
        String subject,
        String contentHtml,
        String contentType,
        Long time,
        List<Attachment> attachments,
        String error
) {
}
