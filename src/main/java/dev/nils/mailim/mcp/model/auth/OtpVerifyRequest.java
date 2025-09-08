package dev.nils.mailim.mcp.model.auth;

import com.fasterxml.jackson.annotation.JsonProperty;

public record OtpVerifyRequest(String code, @JsonProperty("password") String otp) {
}
