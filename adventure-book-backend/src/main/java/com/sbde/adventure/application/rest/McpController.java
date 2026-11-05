package com.sbde.adventure.application.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.sbde.adventure.application.mcp.McpServer;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/** MCP endpoint (streamable HTTP, JSON responses only); the protocol itself is handled by {@link McpServer}. */
@RestController
public class McpController {

    private final McpServer server;

    public McpController(McpServer server) {
        this.server = server;
    }

    @PostMapping(path = "/mcp", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<JsonNode> handle(@RequestBody JsonNode message) {
        return server.handle(message).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.accepted().build());
    }
}
