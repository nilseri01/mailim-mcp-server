package dev.nils.mailim.mcp.model.message.search;

import java.util.List;

public record MessageSearchResponse(
        Integer limit,          // always 30
        Integer offset,         // current page offset
        Boolean hasMore,        // true if X-More: 1
        Integer nextOffset,     // null if no more
        String moreHint,        // optional UX hint
        String error,           // set if request failed
        List<MessageSummary> items
) {
}
