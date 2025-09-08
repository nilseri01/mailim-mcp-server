package dev.nils.mailim.mcp.tools;

import dev.nils.mailim.mcp.config.McpContext;
import dev.nils.mailim.mcp.mappers.MessageDetailsMapper;
import dev.nils.mailim.mcp.mappers.MessageSummaryMapper;
import dev.nils.mailim.mcp.model.SessionState;
import dev.nils.mailim.mcp.model.ToolsConstants;
import dev.nils.mailim.mcp.model.auth.LoginRequest;
import dev.nils.mailim.mcp.model.auth.LoginResponse;
import dev.nils.mailim.mcp.model.auth.OtpVerifyRequest;
import dev.nils.mailim.mcp.model.auth.OtpVerifyResponse;
import dev.nils.mailim.mcp.model.auth.refresh.RefreshTokenResponse;
import dev.nils.mailim.mcp.model.message.details.MessageDetails;
import dev.nils.mailim.mcp.model.message.details.MessageDetailsResponse;
import dev.nils.mailim.mcp.model.message.search.MessageItemResponse;
import dev.nils.mailim.mcp.model.message.search.MessageSearchResponse;
import dev.nils.mailim.mcp.service.SessionRegistryService;
import dev.nils.mailim.mcp.util.ToolsUtils;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class MailimTools {

    /**
     * the MCP Server starter auto-registers @Tool methods as MCP tools; the client can tools/list and tools/call them.
     * The ToolContext lets you grab the MCP exchange (session) so you can keep per-connection auth/device state without passing any connection ID around.
     */

    private final WebClient webClient;

    private final SessionRegistryService sessionRegistryService;

    public MailimTools(WebClient webClient, SessionRegistryService sessionRegistryService) {
        this.webClient = webClient;
        this.sessionRegistryService = sessionRegistryService;
    }

    private SessionState getSessionState(ToolContext ctx) {
        McpSyncServerExchange ex = McpContext.exchange(ctx);
        return sessionRegistryService.addState(ex);
    }

    @Tool(name = "bind", description = "Bind this MCP connection to a username (email). If omitted, uses env MAILIM_EMAIL.")
    public String bind(
            @ToolParam(description = "Username (e.g., email). If empty, will fall back to env MAILIM_EMAIL", required = false) String username,
            ToolContext toolContext
    ) {
        SessionState sessionState = getSessionState(toolContext);

        String envEmail = ToolsUtils.getEnvironmentVariableByKey(ToolsConstants.ENV_MAILIM_EMAIL_KEY);
        if (!StringUtils.hasText(username)) {
            username = envEmail;
        }

        // if not provided via env or request param
        if (!StringUtils.hasText(username)) {
            return ToolsConstants.STATUS_BIND_FAILED + ": no username provided and MAILIM_EMAIL not set";
        }

        sessionState.username = username;
        sessionState.email = username;

        sessionState.deviceId = ToolsUtils.getDeviceId(sessionState);

        return ToolsConstants.STATUS_BOUND + " username=" + sessionState.username + " deviceId=" + sessionState.deviceId;
    }

    @Tool(name = "login", description = "Start Mailim login (password step). If args are omitted, uses env MAILIM_EMAIL and MAILIM_PASSWORD. Returns OTP requirement metadata.")
    public String login(
            @ToolParam(description = "Email for Mailim login", required = false) String email,
            @ToolParam(description = "Account password", required = false) String password,
            @ToolParam(description = "Stable Device-ID (optional)", required = false) String deviceId,
            ToolContext toolContext
    ) {
        SessionState sessionState = getSessionState(toolContext);
        if (StringUtils.hasText(deviceId)) {
            sessionState.deviceId = deviceId;
        }
        // generate if not provided
        sessionState.deviceId = ToolsUtils.getDeviceId(sessionState);

        // Apply env-based defaults when parameters are not provided
        if (!StringUtils.hasText(email)) {
            email = ToolsUtils.getEnvironmentVariableByKey(ToolsConstants.ENV_MAILIM_EMAIL_KEY);
        }

        if (!StringUtils.hasText(password)) {
            password = ToolsUtils.getEnvironmentVariableByKey(ToolsConstants.ENV_MAILIM_PASSWORD_KEY);
        }

        if (!StringUtils.hasText(email) || !StringUtils.hasText(password)) {
            return ToolsConstants.STATUS_LOGIN_MISSING_CREDENTIALS + ": provide email/password or set MAILIM_EMAIL/MAILIM_PASSWORD env";
        }

        LoginResponse loginResponse = webClient.post()
                .uri(ToolsConstants.API_LOGIN_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .header(ToolsConstants.HEADER_ACCEPT, "application/x.yaanimail.v2+json")
                .header(ToolsConstants.HEADER_DEVICE_ID, sessionState.deviceId)
                .bodyValue(new LoginRequest(email, password, 1))
                .retrieve()
                .bodyToMono(LoginResponse.class)
                .block();

        if (loginResponse == null || loginResponse.twoFaCode() == null || loginResponse.twoFaCode().isBlank()) {
            return ToolsConstants.STATUS_LOGIN_START_FAILED;
        }

        sessionState.username = email;
        sessionState.email = email;
        sessionState.twoFaCode = loginResponse.twoFaCode();

        return ToolsConstants.STATUS_OTP_REQUIRED + " mobile=" + (loginResponse.mobile() == null ? "****" : loginResponse.mobile())
                + " timeoutSec=" + loginResponse.twoFaTimeout()
                + " deviceId=" + sessionState.deviceId
                + " twoFaUuidCode=" + sessionState.twoFaCode;
    }

    @Tool(name = "otpVerify", description = "Verify Mailim OTP (second step).")
    public String otpVerify(
            @ToolParam(description = "One-time code sent to phone") String otp,
            ToolContext toolContext
    ) {
        SessionState sessionState = getSessionState(toolContext);
        if (sessionState.twoFaCode == null) {
            return ToolsConstants.STATUS_NO_PENDING_LOGIN;
        }

        OtpVerifyResponse otpVerifyResponse = webClient.post()
                .uri(ToolsConstants.API_OTP_VERIFY_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .header(ToolsConstants.HEADER_ACCEPT, "application/json, text/plain, */*")
                .header(ToolsConstants.HEADER_DEVICE_ID, sessionState.deviceId)
                .bodyValue(new OtpVerifyRequest(sessionState.twoFaCode, otp)) // API expects 'password' field to carry OTP
                .retrieve()
                .bodyToMono(OtpVerifyResponse.class)
                .block();

        if (otpVerifyResponse == null || !StringUtils.hasText(otpVerifyResponse.accessToken())) {
            return ToolsConstants.STATUS_OTP_VERIFY_FAILED;
        }

        sessionState.accessToken = otpVerifyResponse.accessToken();
        sessionState.accessExpire = otpVerifyResponse.expireTime() > 0 ? Instant.ofEpochSecond(otpVerifyResponse.expireTime()) : null;
        if (otpVerifyResponse.refreshTokenInfo() != null) {
            sessionState.refreshToken = otpVerifyResponse.refreshTokenInfo().refreshToken();
            sessionState.refreshExpire = otpVerifyResponse.refreshTokenInfo().expireTime() > 0
                    ? Instant.ofEpochSecond(otpVerifyResponse.refreshTokenInfo().expireTime()) : null;
        }

        if (StringUtils.hasText(otpVerifyResponse.deviceId())) {
            sessionState.deviceId = otpVerifyResponse.deviceId();
        }

        return ToolsConstants.STATUS_LOGIN_OK + " deviceId=" + sessionState.deviceId + " accessExp=" + otpVerifyResponse.expireTime();
    }


    @Tool(name = "refreshToken", description = "Refresh Mailim access token using stored refresh token")
    public synchronized String refreshToken(ToolContext toolContext) {
        SessionState sessionState = getSessionState(toolContext);
        if (!StringUtils.hasText(sessionState.refreshToken)) {
            return ToolsConstants.STATUS_NO_REFRESH_TOKEN;
        }

        RefreshTokenResponse refreshTokenResponse = webClient.get()
                .uri(ToolsConstants.API_REFRESH_TOKEN_URI)
                .header(ToolsConstants.HEADER_ACCEPT, "application/x.yaanimail.v2+json")
                .header(ToolsConstants.HEADER_DEVICE_ID, sessionState.deviceId)
                .header(ToolsConstants.HEADER_AUTHORIZATION, "Bearer " + sessionState.refreshToken) // Mailim expects refresh_token as Bearer
                .retrieve()
                .bodyToMono(RefreshTokenResponse.class)
                .block();

        if (refreshTokenResponse == null || !StringUtils.hasText(refreshTokenResponse.accessToken())) {
            return ToolsConstants.STATUS_REFRESH_FAILED;
        }

        sessionState.accessToken = refreshTokenResponse.accessToken();
        sessionState.accessExpire = refreshTokenResponse.expires() > 0 ? Instant.ofEpochSecond(refreshTokenResponse.expires()) : null;
        sessionState.refreshToken = refreshTokenResponse.refreshToken(); // rotate if API returns a new one
        sessionState.refreshExpire = refreshTokenResponse.refreshTokenExpire() > 0
                ? Instant.ofEpochSecond(refreshTokenResponse.refreshTokenExpire()) : null;
        if (refreshTokenResponse.attributes() != null && refreshTokenResponse.attributes().deviceId() != null) {
            sessionState.deviceId = refreshTokenResponse.attributes().deviceId();
        }

        return ToolsConstants.STATUS_REFRESH_OK + " newExp=" + refreshTokenResponse.expires();
    }

    @Tool(
            name = "listUnreadInbox",
            description = "Fetch a page (limit=30) of newest unread Inbox mails and pagination hints. Caller handles numbering/formatting."
    )
    public MessageSearchResponse listUnreadInbox(
            @ToolParam(description = "Pagination offset; multiples of 30. Defaults to 0.", required = false) Integer offset,
            ToolContext toolContext
    ) {
        int off = (offset == null ? 0 : Math.max(offset, 0));
        SessionState sessionState = getSessionState(toolContext);

        // Decide which token to use
        String accessToken = sessionState.accessToken;
        boolean isSessionTokenUsed = !StringUtils.hasText(accessToken);
        if (isSessionTokenUsed) {
            String tokenStatus = ensureFreshAccess(toolContext, sessionState);
            if (!ToolsConstants.STATUS_OK.equals(tokenStatus)) {
                return getMessageSearchResponseForError(off, ToolsConstants.STATUS_AUTH_ERROR + ": " + tokenStatus);
            }
        }

        try {
            // first attempt
            MessageSearchResponse messageSearchResponse = performSearch(toolContext, off);
            if (messageSearchResponse != null) {
                return messageSearchResponse;
            }

            // if unauthorized and we were using the session token, try one refresh and retry
            if (isSessionTokenUsed) {
                String refreshStatus = refreshToken(toolContext);
                if (!refreshStatus.startsWith(ToolsConstants.STATUS_REFRESH_OK)) {
                    return getMessageSearchResponseForError(off, ToolsConstants.STATUS_AUTH_401_AND_REFRESH_FAILED);
                }
                // token rotated in state()
                messageSearchResponse = performSearch(toolContext, off);
                return Objects.requireNonNullElseGet(messageSearchResponse, () -> getMessageSearchResponseForError(off, ToolsConstants.STATUS_REQUEST_FAILED_AFTER_REFRESH));
            }

            // if caller provided token and we failed above without 401 retry, just report generic failure
            return getMessageSearchResponseForError(off, ToolsConstants.STATUS_REQUEST_FAILED);
        } catch (WebClientResponseException.Unauthorized e) {
            // explicit token path can't auto-refresh
            if (isSessionTokenUsed) {
                String refreshStatus = refreshToken(toolContext);
                if (!refreshStatus.startsWith(ToolsConstants.STATUS_REFRESH_OK)) {
                    getMessageSearchResponseForError(off, ToolsConstants.STATUS_AUTH_401_AND_REFRESH_FAILED);
                }

                try {
                    MessageSearchResponse messageSearchResponse = performSearch(toolContext, off);
                    return Objects.requireNonNullElseGet(messageSearchResponse, () -> getMessageSearchResponseForError(off, ToolsConstants.STATUS_REQUEST_FAILED_AFTER_REFRESH));
                } catch (Exception ex) {
                    return getMessageSearchResponseForError(off, ex.getClass().getSimpleName() + ": " + Objects.toString(ex.getMessage(), "no message"));
                }
            }
            return getMessageSearchResponseForError(off, ToolsConstants.STATUS_AUTH_401);
        } catch (Exception e) {
            return getMessageSearchResponseForError(off, e.getClass().getSimpleName() + ": " + Objects.toString(e.getMessage(), "no message"));
        }
    }

    // executes the POST and maps to MessageSearchResponse; returns null if it detects 401 to let caller decide retry
    private MessageSearchResponse performSearch(ToolContext toolContext, Integer offset) {
        SessionState sessionState = getSessionState(toolContext);
        String bearerToken = sessionState.accessToken;
        String deviceId = sessionState.deviceId;

        Map<String, Object> requestBody = getSearchRequestBody(offset, sessionState.email);
        return webClient.post()
                .uri(ToolsConstants.API_SEARCH_MESSAGES_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON) // single Accept; no duplicate header
                .header(ToolsConstants.HEADER_DEVICE_ID, deviceId)
                .header(ToolsConstants.HEADER_AUTHORIZATION, "Bearer " + bearerToken)
                .bodyValue(requestBody)
                .exchangeToMono(res ->
                        (res.statusCode() == org.springframework.http.HttpStatus.UNAUTHORIZED)
                                ? reactor.core.publisher.Mono.fromCallable(() -> null)
                                : res.bodyToMono(new org.springframework.core.ParameterizedTypeReference<List<MessageItemResponse>>() {
                                })
                                .defaultIfEmpty(List.of())
                                .map(list -> {
                                    boolean hasMore = "1".equalsIgnoreCase(res.headers().asHttpHeaders().getFirst("X-More"));
                                    var items = list.stream().map(MessageSummaryMapper::toSummary).toList();
                                    int off = ((Number) requestBody.get("offset")).intValue();
                                    Integer next = hasMore ? off + ToolsConstants.MAIL_SEARCH_PAGE_LIMIT : null;
                                    String hint = hasMore ? "There are more unread mails. Call again with offset=" + next + "." : null;
                                    return new MessageSearchResponse(
                                            ToolsConstants.MAIL_SEARCH_PAGE_LIMIT, off, hasMore, next, hint, null, items
                                    );
                                })
                )
                .block();
    }

    private Map<String, Object> getSearchRequestBody(Integer offset, String ownerEmail) {
        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("query", "is:unread and in:\"inbox\"");
        requestBody.put("order", "date");
        requestBody.put("order_type", "desc");
        requestBody.put("types", "message");
        requestBody.put("limit", ToolsConstants.MAIL_SEARCH_PAGE_LIMIT);
        requestBody.put("offset", offset);
        requestBody.put("owner_email", ownerEmail);
        return requestBody;
    }

    @Tool(
            name = "getMessageDetails",
            description = "Fetch message details by id and return the HTML content so the caller can render it."
    )
    public MessageDetailsResponse getMessageDetails(
            @ToolParam(description = "Message id from listUnreadInbox") String messageId,
            ToolContext toolContext
    ) {
        SessionState sessionState = getSessionState(toolContext);

        // ensure we have (or refresh) a valid access token
        String tokenStatus = ensureFreshAccess(toolContext, sessionState);
        if (!ToolsConstants.STATUS_OK.equals(tokenStatus)) {
            return getMessageDetailsResponseForError(messageId, ToolsConstants.STATUS_AUTH_ERROR + tokenStatus);
        }

        // first attempt
        MessageDetailsResponse messageDetailsResponse = performFetchMessageDetails(sessionState, messageId);
        if (messageDetailsResponse != null) {
            return messageDetailsResponse;
        }

        // exactly one refresh and retry on 401 (first returned null)
        String refreshTokenStatus = refreshToken(toolContext);
        if (!refreshTokenStatus.startsWith(ToolsConstants.STATUS_REFRESH_OK)) {
            return getMessageDetailsResponseForError(messageId, ToolsConstants.STATUS_AUTH_401_AND_REFRESH_FAILED);
        }
        sessionState = getSessionState(toolContext);
        messageDetailsResponse = performFetchMessageDetails(sessionState, messageId);
        return Objects.requireNonNullElseGet(messageDetailsResponse, () -> getMessageDetailsResponseForError(messageId, ToolsConstants.STATUS_REQUEST_FAILED_AFTER_REFRESH));
    }

    private MessageDetailsResponse getMessageDetailsResponseForError(String id, String errorMessage) {
        return new MessageDetailsResponse(id, null, null, null, null, List.of(), errorMessage);
    }

    // returns null on 401 to signal retry; otherwise a MessageDetails (success or error-filled)
    private MessageDetailsResponse performFetchMessageDetails(SessionState sessionState, String messageId) {
        String bearerToken = sessionState.accessToken;
        String deviceId = sessionState.deviceId;
        String ownerEmail = sessionState.email;

        try {
            return webClient.get()
                    .uri(ToolsConstants.API_MESSAGE_DETAILS_URI, messageId, ownerEmail)
                    .header(ToolsConstants.HEADER_ACCEPT, "application/x.yaanimail.v2+json")
                    .header(ToolsConstants.HEADER_DEVICE_ID, deviceId)
                    .header(ToolsConstants.HEADER_AUTHORIZATION, "Bearer " + bearerToken)
                    .exchangeToMono(res -> {
                        if (res.statusCode() == org.springframework.http.HttpStatus.UNAUTHORIZED) {
                            return reactor.core.publisher.Mono.empty(); // signal caller to refresh+retry
                        }
                        if (!res.statusCode().is2xxSuccessful()) {
                            return res.bodyToMono(String.class).defaultIfEmpty("")
                                    .map(body -> getMessageDetailsResponseForError(messageId, "HTTP " + res.statusCode().value() + (body.isBlank() ? "" : " body: " + ToolsUtils.abbreviate(body, 300))
                                    ));
                        }
                        return res.bodyToMono(MessageDetails.class)
                                .map(dto -> MessageDetailsMapper.toDomain(dto, messageId));
                    })
                    .block();
        } catch (WebClientResponseException.Unauthorized e) {
            return null; // retry signal
        } catch (Exception ex) {
            return getMessageDetailsResponseForError(messageId, ex.getClass().getSimpleName() + ": " + Objects.toString(ex.getMessage(), "no message"));
        }
    }

    private MessageSearchResponse getMessageSearchResponseForError(Integer offset, String errorMessage) {
        return new MessageSearchResponse(
                ToolsConstants.MAIL_SEARCH_PAGE_LIMIT, offset, false, null, null, errorMessage, List.of()
        );
    }

    /**
     * Ensure we have a fresh access token; proactively refresh if close to expiry.
     */
    private String ensureFreshAccess(ToolContext toolContext, SessionState sessionState) {
        if (!StringUtils.hasText(sessionState.accessToken)) {
            return ToolsConstants.STATUS_NOT_AUTHENTICATED;
        }
        // If we don't know expiry, just proceed.
        if (sessionState.accessExpire == null) {
            return ToolsConstants.STATUS_OK;
        }

        Instant now = Instant.now();
        if (now.isAfter(sessionState.accessExpire.minus(ToolsConstants.REFRESH_SKEW_SECONDS, ChronoUnit.SECONDS))) {
            // Avoid stampedes: synchronize per-session during refresh
            synchronized (this) {
                // Double-check inside the lock
                if (Instant.now().isAfter(sessionState.accessExpire.minus(ToolsConstants.REFRESH_SKEW_SECONDS, ChronoUnit.SECONDS))) {
                    String token = refreshToken(toolContext);
                    if (!token.startsWith(ToolsConstants.STATUS_REFRESH_OK)) {
                        return ToolsConstants.STATUS_AUTO_REFRESH_FAILED;
                    }
                }
            }
        }
        return ToolsConstants.STATUS_OK;
    }

    @Tool(name = "whoami", description = "Debug: return token/device status for this connection.")
    public String whoami(ToolContext toolContext) {
        SessionState sessionState = getSessionState(toolContext);
        return "username=" + sessionState.username +
                " deviceId=" + sessionState.deviceId +
                " deviceName=" + ToolsUtils.getDeviceName() +
                " accessExp=" + (sessionState.accessExpire == null ? "n/a" : sessionState.accessExpire.getEpochSecond()) +
                " refreshExp=" + (sessionState.refreshExpire == null ? "n/a" : sessionState.refreshExpire.getEpochSecond());
    }
}
