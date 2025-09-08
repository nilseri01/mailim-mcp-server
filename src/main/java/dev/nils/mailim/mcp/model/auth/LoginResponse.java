package dev.nils.mailim.mcp.model.auth;

import com.fasterxml.jackson.annotation.JsonProperty;

public record LoginResponse(
        String mobile,
        @JsonProperty("two_fa_security") int twoFaSecurity,
        @JsonProperty("two_fa_time_out") int twoFaTimeout,
        @JsonProperty("two_fa_code") String twoFaCode
) {
}
