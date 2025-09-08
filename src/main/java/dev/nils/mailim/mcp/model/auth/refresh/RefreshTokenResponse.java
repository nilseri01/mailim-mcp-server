package dev.nils.mailim.mcp.model.auth.refresh;

import com.fasterxml.jackson.annotation.JsonProperty;

public record RefreshTokenResponse(@JsonProperty("access_token") String accessToken,
                                   long expires,
                                   @JsonProperty("insert_time") long insertTime,
                                   Attributes attributes,
                                   @JsonProperty("client_id") String clientId,
                                   @JsonProperty("refresh_token") String refreshToken,
                                   @JsonProperty("refresh_token_expire") long refreshTokenExpire) {
    public record Attributes(
            @JsonProperty("device_id") String deviceId,
            @JsonProperty("device_name") String deviceName,
            @JsonProperty("device_os") String deviceOs,
            String type,
            @JsonProperty("expire_time") long expireTime,
            String agent,
            String ip
    ) {
    }
}
