package dev.nils.mailim.mcp.config;

import dev.nils.mailim.mcp.tools.MailimTools;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class McpServerConfig {

    @Bean(name = "mailimToolCallbacks")
    public List<ToolCallback> mailimToolCallbacks(MailimTools mailimTools) {
        return List.of(ToolCallbacks.from(mailimTools));
    }
}
