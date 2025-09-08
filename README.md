# Mailim MCP Server

A Spring Boot (Java 21) server that implements the Model Context Protocol (MCP) to let AI clients (e.g., Claude Desktop)
interact with a Mailim account. It exposes tools for login (with OTP), token refresh, listing unread emails,
and fetching message details.

[![Java](https://img.shields.io/badge/Java-%23ED8B00.svg?logo=openjdk&logoColor=white)](#)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-6DB33F?logo=springboot&logoColor=fff)](#)
[![Claude](https://img.shields.io/badge/Claude-D97757?logo=claude&logoColor=fff)](#)

## Development

- Java 21, Spring Boot, Spring WebFlux.
- MCP server config via Spring AI MCP properties.
- WebClient is pre-configured with default headers and timeouts.

You can
visit [my Medium post](https://senoritadeveloper.medium.com/building-an-mcp-server-with-spring-ai-and-testing-with-claude-desktop-e815b5bbd908)
to
read the details about the implementation.

## Features

- MCP SYNC server over STDIO
- Tools:
    - bind: Bind the MCP connection to an email (optional)
    - login: Start password login (returns OTP requirement)
    - otpVerify: Verify the OTP code to complete login
    - refreshToken: Refresh access token using stored refresh token
    - listUnreadInbox: Fetch paginated unread messages (limit=30)
    - getMessageDetails: Fetch HTML message body and attachments by ID
    - whoami: Show connection/session status (debug)
- Uses Spring WebFlux WebClient with configurable timeouts

## Requirements

- Java 21
- Maven
- An AI client that supports MCP (e.g., Claude Desktop)

## Build

```sh
mvn -U clean package
```

The output JAR file will be located in `target/` folder (e.g., `mcp-0.0.1-SNAPSHOT.jar`).

### Claude Desktop integration

In your Claude Desktop config, define an MCP server with command and env:

```json
{
  "mcpServers": {
    "ym-mcp-server": {
      "command": "/opt/homebrew/Cellar/openjdk@21/21.0.7/bin/java",
      "args": [
        "-Dio.netty.resolver.dns.useJdkDnsServerAddressStreamProvider=true",
        "-Dvertx.disableDnsResolver=true",
        "-Djava.net.preferIPv4Stack=true",
        "-jar",
        "***PATH_TO_JAR_FILE***"
      ],
      "env": {
        "MAILIM_EMAIL": "YOUR_MAIL_ADDRESS",
        "MAILIM_PASSWORD": "YOUR_PASSWORD"
      }
    }
  }
}
```

## Tool Usage (from an MCP client)

- bind
    - Description: Bind this MCP connection to a username (email). If omitted, uses env MAILIM_EMAIL.
    - Params: username? (string)
- login
    - Description: Start Mailim login (password step). If args are omitted, uses env MAILIM_EMAIL and MAILIM_PASSWORD.
      Returns OTP requirement metadata.
    - Params: email? (string), password? (string), deviceId? (string)
- otpVerify
    - Description: Verify Mailim OTP (second step).
    - Params: otp (string)
- refreshToken
    - Description: Refresh access token using stored refresh token.
    - Params: none
- listUnreadInbox
    - Description: Fetch newest unread inbox mails with pagination (limit=30).
    - Params: offset (integer; multiples of 30, default 0)
- getMessageDetails
    - Description: Fetch message details by id. Returns HTML for rendering and attachment metadata.
    - Params: messageId (string)
- whoami
    - Description: Debug status: username, deviceId, token expiration info.
    - Params: none

Typical flow:

1) bind (optional; uses env when omitted)
2) login (returns OTP_REQUIRED)
3) otpVerify with the code
4) listUnreadInbox
5) getMessageDetails
6) whoami (optional; get auth state)

## Screenshots

Here are some screenshots from Claude Desktop:

<img src="https://raw.githubusercontent.com/nilseri01/mailim-mcp-server/main/screenshots/screenshot-01.png" width="500" />

<img src="https://raw.githubusercontent.com/nilseri01/mailim-mcp-server/main/screenshots/screenshot-02.png" width="500" />

<img src="https://raw.githubusercontent.com/nilseri01/mailim-mcp-server/main/screenshots/screenshot-03.png" width="500" />

<img src="https://raw.githubusercontent.com/nilseri01/mailim-mcp-server/main/screenshots/screenshot-04.png" width="500" />

<img src="https://raw.githubusercontent.com/nilseri01/mailim-mcp-server/main/screenshots/screenshot-05.png" width="500" />

<img src="https://raw.githubusercontent.com/nilseri01/mailim-mcp-server/main/screenshots/screenshot-06.png" width="500" />

<img src="https://raw.githubusercontent.com/nilseri01/mailim-mcp-server/main/screenshots/screenshot-07.png" width="500" />

<img src="https://raw.githubusercontent.com/nilseri01/mailim-mcp-server/main/screenshots/screenshot-08.png" width="500" />

<img src="https://raw.githubusercontent.com/nilseri01/mailim-mcp-server/main/screenshots/screenshot-09.png" width="500" />

## Security Notes

- Do not print secrets (passwords, tokens) to logs in Production. Update application.properties with the following:

```
logging.level.reactor.netty.http.client=WARN
logging.level.reactor.netty.transport=WARN
logging.level.io.netty=WARN
```

- Use env vars or a secrets manager for credentials.
- Tokens are stored in session state per MCP connection.

## Troubleshooting

- Timeouts:
    - Increase `http.client.*-timeout-ms` values for slower networks or large responses.

## Contributors

<img src="https://readme-typing-svg.demolab.com?font=Open+Sans&size=16&pause=1000&color=A6F73F&height=50&width=200&lines=Nil+Seri"/>

[Github 1](https://github.com/senoritadeveloper01)

[Github 2](https://github.com/nilseri01)

[Medium](https://senoritadeveloper.medium.com/)

## Copyright & Licensing Information

This project is licensed under the terms of the MIT license.