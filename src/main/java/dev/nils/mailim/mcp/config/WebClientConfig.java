package dev.nils.mailim.mcp.config;

import dev.nils.mailim.mcp.model.ToolsConstants;
import dev.nils.mailim.mcp.util.ToolsUtils;
import io.netty.handler.logging.LogLevel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.transport.logging.AdvancedByteBufFormat;

import java.util.concurrent.TimeUnit;

@Configuration
public class WebClientConfig {

    @Value("${http.client.connect-timeout-ms:5000}")
    private int connectTimeoutMs;

    @Value("${http.client.response-timeout-ms:15000}")
    private long responseTimeoutMs;

    @Value("${http.client.read-timeout-ms:15000}")
    private long readTimeoutMs;

    @Value("${http.client.write-timeout-ms:15000}")
    private long writeTimeoutMs;

    @Value("${api.base.url}")
    private String API_BASE_URL;

    @Value("${spring.ai.mcp.server.name}")
    private String mcpServerName;

    @Value("${spring.ai.mcp.server.version}")
    private String mcpServerVersion;

    @Bean
    public WebClient webClient(WebClient.Builder builder) {
        // increase buffer if you expect large responses
        var strategies = ExchangeStrategies.builder()
                .codecs(c -> c.defaultCodecs().maxInMemorySize(4 * 1024 * 1024))
                .build();

        HttpClient httpClient = HttpClient.create()
                .wiretap("reactor.netty.http.client", LogLevel.DEBUG, AdvancedByteBufFormat.TEXTUAL) // headers + body
                .option(io.netty.channel.ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeoutMs)
                .responseTimeout(java.time.Duration.ofMillis(responseTimeoutMs))
                .doOnConnected(conn -> conn
                        .addHandlerLast(new io.netty.handler.timeout.ReadTimeoutHandler(readTimeoutMs, TimeUnit.MILLISECONDS))
                        .addHandlerLast(new io.netty.handler.timeout.WriteTimeoutHandler(writeTimeoutMs, TimeUnit.MILLISECONDS))
                );

        return builder
                .baseUrl(API_BASE_URL)
                .exchangeStrategies(strategies)
                .clientConnector(new org.springframework.http.client.reactive.ReactorClientHttpConnector(httpClient))
                // Default headers that rarely change
                .defaultHeader(ToolsConstants.HEADER_DEVICE_OS, mcpServerName)
                .defaultHeader(ToolsConstants.HEADER_DEVICE_NAME, ToolsUtils.getDeviceName())
                .defaultHeader(ToolsConstants.HEADER_APP_VERSION, mcpServerVersion)
                .defaultHeader(ToolsConstants.HEADER_DEVICE_LANGUAGE, "en_US") // or "tr_TR"
                .build();
    }
}
