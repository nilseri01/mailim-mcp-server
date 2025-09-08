package dev.nils.mailim.mcp.util;

import io.modelcontextprotocol.server.McpAsyncServerExchange;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpServerSession;

import java.lang.reflect.Field;

public class SessionIdUtils {

    static final Field exchangeField;
    static final Field sessionField;
    static final Field idField;

    static {
        exchangeField = getField(McpSyncServerExchange.class, "exchange");
        sessionField = getField(McpAsyncServerExchange.class, "session");
        idField = getField(McpServerSession.class, "id");
    }

    static Field getField(Class<?> clazz, String fieldName) {
        try {
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field;
        } catch (Exception e) {
            return null;
        }
    }


    public static String getSessionId(McpSyncServerExchange syncServerExchange) {
        try {
            McpAsyncServerExchange asyncServerExchange = (McpAsyncServerExchange) exchangeField.get(syncServerExchange);
            McpServerSession session = (McpServerSession) sessionField.get(asyncServerExchange);
            return (String) idField.get(session);
        } catch (Exception e) {
            return null;
        }
    }
}
