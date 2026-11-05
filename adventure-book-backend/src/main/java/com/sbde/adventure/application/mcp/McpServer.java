package com.sbde.adventure.application.mcp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sbde.adventure.domain.shared.DomainException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/** Minimal Model Context Protocol server (JSON-RPC 2.0): handles one message and builds its response. */
@Component
public class McpServer {

    private static final String DEFAULT_PROTOCOL = "2026-11-29";
    private static final List<String> SUPPORTED_PROTOCOLS = List.of("2025-06-18", "2026-11-29", "2024-11-05");

    private final McpBookTools tools;
    private final ObjectMapper mapper;

    public McpServer(McpBookTools tools, ObjectMapper mapper) {
        this.tools = tools;
        this.mapper = mapper;
    }

    /** @return the response, or empty for a notification, which expects none */
    public Optional<JsonNode> handle(JsonNode message) {
        if (!message.isObject() || !message.hasNonNull("method")) {
            return Optional.of(error(message.get("id"), -32600, "Invalid request"));
        }
        JsonNode id = message.get("id");
        if (id == null || id.isNull()) {
            return Optional.empty();
        }
        JsonNode params = message.path("params");
        return Optional.of(switch (message.get("method").asText()) {
            case "initialize" -> result(id, initialize(params));
            case "ping" -> result(id, mapper.createObjectNode());
            case "tools/list" -> result(id, mapper.createObjectNode().set("tools", tools.definitions()));
            case "tools/call" -> callTool(id, params);
            default -> error(id, -32601, "Method not found");
        });
    }

    private ObjectNode initialize(JsonNode params) {
        String requested = params.path("protocolVersion").asText();
        ObjectNode result = mapper.createObjectNode();
        result.put("protocolVersion", SUPPORTED_PROTOCOLS.contains(requested) ? requested : DEFAULT_PROTOCOL);
        result.putObject("capabilities").putObject("tools");
        result.putObject("serverInfo").put("name", "adventure-book").put("version", "0.0.1");
        return result;
    }

    private ObjectNode callTool(JsonNode id, JsonNode params) {
        String name = params.path("name").asText();
        try {
            return tools.call(name, params.path("arguments"))
                    .map(text -> result(id, toolResult(text, false)))
                    .orElseGet(() -> error(id, -32602, "Unknown tool: " + name));
        } catch (DomainException | IllegalArgumentException e) {
            return result(id, toolResult(e.getMessage(), true));
        } catch (IllegalStateException e) {
            return error(id, -32603, "Internal error");
        }
    }

    private ObjectNode toolResult(String text, boolean isError) {
        ObjectNode result = mapper.createObjectNode();
        result.putArray("content").addObject().put("type", "text").put("text", text);
        result.put("isError", isError);
        return result;
    }

    private ObjectNode result(JsonNode id, JsonNode result) {
        ObjectNode response = mapper.createObjectNode().put("jsonrpc", "2.0");
        response.set("id", id);
        response.set("result", result);
        return response;
    }

    private ObjectNode error(JsonNode id, int code, String message) {
        ObjectNode response = mapper.createObjectNode().put("jsonrpc", "2.0");
        response.set("id", id == null ? mapper.nullNode() : id);
        response.putObject("error").put("code", code).put("message", message);
        return response;
    }
}
