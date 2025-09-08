package dev.nils.mailim.mcp.model.auth.refresh;

import com.fasterxml.jackson.annotation.JsonProperty;

public record RefreshTokenInfo(long id,
                               @JsonProperty("refresh_token") String refreshToken,
                               @JsonProperty("expire_time") long expireTime) {
}

