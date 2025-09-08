package dev.nils.mailim.mcp.util;

import dev.nils.mailim.mcp.model.SessionState;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.UUID;

@Component
public class ToolsUtils {

    public static String getDeviceId(SessionState sessionState) {
        if (sessionState != null && StringUtils.hasText(sessionState.deviceId)) {
            return sessionState.deviceId;
        }
        // generate if not provided
        return UUID.randomUUID().toString();

    }

    public static String getDeviceName() {
        String osName = System.getProperty("os.name");
        String osVersion = System.getProperty("os.version");
        String javaVersion = System.getProperty("java.version");
        return String.format("%s %s - Java %s", osName, osVersion, javaVersion);
    }

    public static String getEnvironmentVariableByKey(String key) {
        String environmentVariable = System.getenv(key);
        if (!StringUtils.hasText(environmentVariable)) {
            return null;
        }
        return environmentVariable;
    }


    public static String abbreviate(String s, int max) {
        if (s == null) return "";
        String t = s.trim().replaceAll("\\s+", " ");
        return t.length() <= max ? t : t.substring(0, Math.max(0, max)) + "…";
    }
}
