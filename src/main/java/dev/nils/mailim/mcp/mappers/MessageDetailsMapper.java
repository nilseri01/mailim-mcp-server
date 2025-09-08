package dev.nils.mailim.mcp.mappers;

import dev.nils.mailim.mcp.model.message.details.Attachment;
import dev.nils.mailim.mcp.model.message.details.MessageDetails;
import dev.nils.mailim.mcp.model.message.details.MessageDetailsResponse;

import java.util.List;

public class MessageDetailsMapper {

    public static MessageDetailsResponse toDomain(MessageDetails dto, String fallbackId) {
        var atts = (dto.parts() == null ? List.<Attachment>of()
                : dto.parts().stream()
                .map(p -> new Attachment(p.id(), p.name(), p.contentType(), p.disposition(), p.link(), p.size()))
                .toList());

        String subject = (dto.headers() == null ? null : dto.headers().subject());
        Long time = (dto.headers() == null ? null : dto.headers().time());

        return new MessageDetailsResponse(
                (dto.id() == null || dto.id().isBlank() ? fallbackId : dto.id()),
                subject,
                dto.content(),
                dto.contentType(),
                time,
                atts,
                null
        );
    }
}
