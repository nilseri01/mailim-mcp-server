package dev.nils.mailim.mcp.model.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.nils.mailim.mcp.model.auth.refresh.RefreshTokenInfo;

public record OtpVerifyResponse(@JsonProperty("access_token") String accessToken,
                                String clientId,
                                @JsonProperty("expire_time") long expireTime,
                                int id,
                                String username,
                                String name,
                                String title,
                                String mobile,
                                @JsonProperty("used_quota") long usedQuota,
                                @JsonProperty("total_quota") long totalQuota,
                                int mfa,
                                String image,
                                String archive,
                                @JsonProperty("active_sync") String activeSync,
                                String bulkmail,
                                @JsonProperty("device_id") String deviceId,
                                @JsonProperty("sso_device_id") String ssoDeviceId,
                                @JsonProperty("app_version") String appVersion,
                                @JsonProperty("two_fa_security") int twoFaSecurity,
                                @JsonProperty("push_notification_id") String pushNotificationId,
                                String language,
                                String signature,
                                @JsonProperty("change_password") boolean changePassword,
                                @JsonProperty("destination_url") String destinationUrl,
                                @JsonProperty("is_passcode_required") boolean isPasscodeRequired,
                                @JsonProperty("refresh_token_info") RefreshTokenInfo refreshTokenInfo
) {
}
