package dev.nils.mailim.mcp.service;

import dev.nils.mailim.mcp.model.SessionState;
import dev.nils.mailim.mcp.util.SessionIdUtils;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

@Service
public class SessionRegistryService {

    // you can use a database or Redis, but for this demo we'll just keep it in memory
    private final ConcurrentHashMap<String, SessionState> sessionRegistry = new ConcurrentHashMap<>();

    public SessionState addState(McpSyncServerExchange exchange) {
        return sessionRegistry.computeIfAbsent(SessionIdUtils.getSessionId(exchange), k -> new SessionState());
    }

    public void removeStateBySessionId(String sessionId) {
        sessionRegistry.remove(sessionId);
    }

    public void clearAll() {
        sessionRegistry.clear();
    }
}
