package dev.nils.mailim.mcp.model;

import java.time.Instant;

public class SessionState {
    public String username; // user's email (bind tool)
    public String deviceId;

    public String twoFaCode;
    public String email;

    public String accessToken;
    public Instant accessExpire;
    public String refreshToken;
    public Instant refreshExpire;

    public SessionState() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getTwoFaCode() {
        return twoFaCode;
    }

    public void setTwoFaCode(String twoFaCode) {
        this.twoFaCode = twoFaCode;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public Instant getAccessExpire() {
        return accessExpire;
    }

    public void setAccessExpire(Instant accessExpire) {
        this.accessExpire = accessExpire;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public Instant getRefreshExpire() {
        return refreshExpire;
    }

    public void setRefreshExpire(Instant refreshExpire) {
        this.refreshExpire = refreshExpire;
    }
}
