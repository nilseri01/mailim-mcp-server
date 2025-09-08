package dev.nils.mailim.mcp.model;

public class ToolsConstants {

    public static final long REFRESH_SKEW_SECONDS = 60;

    public static final int MAIL_SEARCH_PAGE_LIMIT = 30;

    public static final String API_LOGIN_URI = "/accounts/login";
    public static final String API_OTP_VERIFY_URI = "/accounts/login/otp-verify";
    public static final String API_REFRESH_TOKEN_URI = "/tokens/refresh";
    public static final String API_SEARCH_MESSAGES_URI = "/emails/messages/search";
    public static final String API_MESSAGE_DETAILS_URI = "/emails/messages/{messageId}/details?owner_email={ownerEmail}";

    public static final String HEADER_ACCEPT = "Accept";
    public static final String HEADER_DEVICE_ID = "Device-ID";
    public static final String HEADER_DEVICE_NAME = "Device-Name";
    public static final String HEADER_DEVICE_OS = "Device-OS";
    public static final String HEADER_APP_VERSION = "App-Version";
    public static final String HEADER_DEVICE_LANGUAGE = "Device-Language";
    public static final String HEADER_AUTHORIZATION = "Authorization";

    public static final String ENV_MAILIM_EMAIL_KEY = "MAILIM_EMAIL";
    public static final String ENV_MAILIM_PASSWORD_KEY = "MAILIM_PASSWORD";

    // status codes
    public static final String STATUS_BIND_FAILED = "BIND_FAILED";
    public static final String STATUS_BOUND = "BOUND";
    public static final String STATUS_LOGIN_MISSING_CREDENTIALS = "LOGIN_MISSING_CREDENTIALS";
    public static final String STATUS_LOGIN_START_FAILED = "LOGIN_START_FAILED";
    public static final String STATUS_OTP_REQUIRED = "OTP_REQUIRED";
    public static final String STATUS_NO_PENDING_LOGIN = "NO_PENDING_LOGIN";
    public static final String STATUS_OTP_VERIFY_FAILED = "OTP_VERIFY_FAILED";
    public static final String STATUS_LOGIN_OK = "LOGIN_OK";
    public static final String STATUS_NO_REFRESH_TOKEN = "NO_REFRESH_TOKEN";
    public static final String STATUS_REFRESH_FAILED = "REFRESH_FAILED";
    public static final String STATUS_REFRESH_OK = "REFRESH_OK";
    public static final String STATUS_AUTH_ERROR = "AUTH_ERROR";
    public static final String STATUS_AUTO_REFRESH_FAILED = "AUTO_REFRESH_FAILED";
    public static final String STATUS_AUTH_401_AND_REFRESH_FAILED = "AUTH_401_AND_REFRESH_FAILED";
    public static final String STATUS_REQUEST_FAILED_AFTER_REFRESH = "REQUEST_FAILED_AFTER_REFRESH";
    public static final String STATUS_REQUEST_FAILED = "REQUEST_FAILED";
    public static final String STATUS_AUTH_401 = "AUTH_401";
    public static final String STATUS_NOT_AUTHENTICATED = "NOT_AUTHENTICATED";
    public static final String STATUS_OK = "OK";
}
