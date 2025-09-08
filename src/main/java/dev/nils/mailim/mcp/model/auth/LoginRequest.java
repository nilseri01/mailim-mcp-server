package dev.nils.mailim.mcp.model.auth;

public record LoginRequest(String email, String password, int details) {
}
