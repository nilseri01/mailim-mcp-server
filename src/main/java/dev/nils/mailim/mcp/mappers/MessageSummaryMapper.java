package dev.nils.mailim.mcp.mappers;

import dev.nils.mailim.mcp.model.message.search.MessageItemResponse;
import dev.nils.mailim.mcp.model.message.search.MessageSummary;

public class MessageSummaryMapper {

    public static MessageSummary toSummary(MessageItemResponse d) {
        String fromText = null;
        if (d.from() != null && !d.from().isEmpty()) {
            var p = d.from().getFirst();
            fromText = (p.fullName() != null && !p.fullName().isBlank()) ? p.fullName() : p.email();
        }
        return new MessageSummary(
                fromText,
                d.subject(),
                d.firstLine(),
                d.calendarInvite() == 1,
                d.priority(),
                d.id(),
                d.time()
        );
    }
}
