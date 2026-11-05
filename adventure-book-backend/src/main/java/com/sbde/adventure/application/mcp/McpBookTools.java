package com.sbde.adventure.application.mcp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sbde.adventure.application.dto.response.BookResponse;
import com.sbde.adventure.domain.book.BookSearchCriteria;
import com.sbde.adventure.domain.book.BookService;
import com.sbde.adventure.domain.book.Difficulty;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** The library tools exposed through MCP: their descriptions and their execution. */
@Component
public class McpBookTools {

    private final BookService books;
    private final ObjectMapper mapper;

    public McpBookTools(BookService books, ObjectMapper mapper) {
        this.books = books;
        this.mapper = mapper;
    }

    public ArrayNode definitions() {
        ArrayNode tools = mapper.createArrayNode();

        ObjectNode list = tools.addObject();
        list.put("name", "list_books");
        list.put("description", "Lists the adventure books of the library, with optional search and filters");
        ObjectNode listProps = list.putObject("inputSchema").put("type", "object").putObject("properties");
        listProps.putObject("search").put("type", "string")
                .put("description", "Text to look for in the title, author and description");
        ObjectNode difficulty = listProps.putObject("difficulty").put("type", "string");
        for (Difficulty value : Difficulty.values()) {
            difficulty.withArray("enum").add(value.name());
        }
        listProps.putObject("genres").put("type", "array").put("description", "Genres of the book, any of them matches")
                .putObject("items").put("type", "string");
        listProps.putObject("page").put("type", "integer").put("description", "Page index, starting at 0");
        listProps.putObject("size").put("type", "integer").put("description", "Page size, 100 at most");

        ObjectNode get = tools.addObject();
        get.put("name", "get_book");
        get.put("description", "Gets one book of the library by its id");
        ObjectNode getSchema = get.putObject("inputSchema").put("type", "object");
        getSchema.putObject("properties").putObject("id").put("type", "string");
        getSchema.putArray("required").add("id");
        return tools;
    }

    /** @return the JSON text produced by the tool, or empty when no tool has this name */
    public Optional<String> call(String name, JsonNode arguments) {
        Object payload = switch (name) {
            case "list_books" -> books.search(toCriteria(arguments)).map(BookResponse::from);
            case "get_book" -> BookResponse.from(books.get(arguments.path("id").asText()));
            default -> null;
        };
        if (payload == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(mapper.writeValueAsString(payload));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    private static BookSearchCriteria toCriteria(JsonNode arguments) {
        BookSearchCriteria criteria = new BookSearchCriteria();
        criteria.setSearch(arguments.path("search").asText(null));
        if (arguments.hasNonNull("difficulty")) {
            criteria.setDifficulties(List.of(Difficulty.valueOf(arguments.get("difficulty").asText().toUpperCase())));
        }
        if (arguments.hasNonNull("genres")) {
            List<String> genres = new ArrayList<>();
            arguments.get("genres").forEach(genre -> genres.add(genre.asText()));
            criteria.setGenres(genres);
        }
        criteria.setPage(arguments.path("page").asInt(0));
        criteria.setSize(arguments.path("size").asInt(25));
        return criteria;
    }
}
