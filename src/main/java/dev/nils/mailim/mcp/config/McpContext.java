package dev.nils.mailim.mcp.config;

import io.modelcontextprotocol.server.McpSyncServerExchange;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.mcp.McpToolUtils;

public final class McpContext {

    private McpContext() {
    }

    //  https://stackoverflow.com/questions/79670450/how-to-get-sessionid-spring-boot-mcp-server-mcp-messagesessionid-63bb3566-bd1d
    public static McpSyncServerExchange exchange(ToolContext ctx) {
        return McpToolUtils.getMcpExchange(ctx)
                .orElseThrow(() -> new IllegalStateException("No MCP exchange in ToolContext"));
    }
}
