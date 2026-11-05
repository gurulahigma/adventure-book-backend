package com.sbde.adventure;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:adventure-test;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class AdventureBookApiTest {

    @Autowired
    MockMvc mvc;
    @Autowired
    ObjectMapper mapper;

    @Test
    void seedBooksAreListedWithTheirValidity() throws Exception {
        mvc.perform(get("/api/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(4))
                .andExpect(jsonPath("$.content[?(@.id=='dragon-quest')].valid").value(true))
                .andExpect(jsonPath("$.content[?(@.id=='crystal-caverns')].valid").value(false))
                .andExpect(jsonPath("$.content[?(@.id=='the-prisoner')].valid").value(false))
                .andExpect(jsonPath("$.content[?(@.id=='pirates-jade-sea')].valid").value(false));
    }

    @Test
    void searchesAndFiltersBooks() throws Exception {
        mvc.perform(get("/api/books").param("search", "CRYSTAL"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].id").value("crystal-caverns"));
        mvc.perform(get("/api/books").param("difficulties", "HARD", "MEDIUM"))
                .andExpect(jsonPath("$.totalElements").value(3));
        mvc.perform(get("/api/books").param("genres", "Fantasy").param("sortBy", "title").param("direction", "DESC"))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[0].id").value("crystal-caverns"));
        mvc.perform(get("/api/books").param("genres", "Fantasy", "Adventure"))
                .andExpect(jsonPath("$.totalElements").value(3));
        mvc.perform(get("/api/books").param("valid", "true"))
                .andExpect(jsonPath("$.totalElements").value(1));
        mvc.perform(get("/api/books/genres"))
                .andExpect(jsonPath("$[0]").value("Adventure"));
    }

    @Test
    void invalidBooksCannotBePlayed() throws Exception {
        mvc.perform(post("/api/games").param("bookId", "the-prisoner"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.errors").isNotEmpty());
        mvc.perform(post("/api/games").param("bookId", "nope")).andExpect(status().isNotFound());
    }

    @Test
    void playsSavesAndResumesAGame() throws Exception {
        JsonNode game = json(mvc.perform(post("/api/games").param("bookId", "dragon-quest"))
                .andExpect(status().isCreated()).andReturn());
        String id = game.get("id").asText();
        assertThat(game.get("healthPoints").asInt()).isEqualTo(10);
        assertThat(game.path("currentSection").path("type").asText()).isEqualTo("BEGIN");

        // climbing the cliff costs 3 health points and leads to section 20
        JsonNode hurt = choose(id, 1);
        assertThat(hurt.get("healthPoints").asInt()).isEqualTo(7);
        assertThat(hurt.get("progressPercent").asInt()).isPositive();

        mvc.perform(post("/api/games/{id}/save", id)).andExpect(status().isOk())
                .andExpect(jsonPath("$.savedAt").isNotEmpty());
        mvc.perform(get("/api/games/saved"))
                .andExpect(jsonPath("$[?(@.gameId=='" + id + "')].healthPoints").value(7));

        mvc.perform(post("/api/games/{id}/choices", id).contentType(MediaType.APPLICATION_JSON)
                .content("{\"optionIndex\":99}")).andExpect(status().isConflict());

        // the lava leap costs 7 health points: the player dies
        choose(id, 1);
        JsonNode dead = choose(id, 2);
        assertThat(dead.get("status").asText()).isEqualTo("LOST");
        assertThat(dead.get("outcome").asText()).isEqualTo("LOSE");
        assertThat(dead.get("healthPoints").asInt()).isZero();
        mvc.perform(post("/api/games/{id}/choices", id).contentType(MediaType.APPLICATION_JSON)
                .content("{\"optionIndex\":0}")).andExpect(status().isConflict());

        JsonNode resumed = json(mvc.perform(post("/api/games/{id}/resume", id)).andExpect(status().isOk()).andReturn());
        assertThat(resumed.get("healthPoints").asInt()).isEqualTo(7);
        assertThat(resumed.get("currentSection").get("id").asInt()).isEqualTo(20);
        assertThat(resumed.get("status").asText()).isEqualTo("IN_PROGRESS");
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void importsABookFromJson() throws Exception {
        String book = """
                {"title":"Tiny Tale","author":"Me","difficulty":"EASY","genres":["Mystery"],
                 "sections":[
                  {"id":"1","text":"Start","type":"BEGIN","options":[{"description":"Go","gotoId":2,
                    "consequence":{"type":"LOSE_HEALTH","value":"2","text":"Ouch"}}]},
                  {"id":2,"text":"The end","type":"END"}]}
                """;
        mvc.perform(post("/api/books").contentType(MediaType.APPLICATION_JSON).content(book))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("tiny-tale"))
                .andExpect(jsonPath("$.valid").value(true));
        mvc.perform(post("/api/books").contentType(MediaType.APPLICATION_JSON).content(book))
                .andExpect(status().isConflict());
        mvc.perform(post("/api/books").contentType(MediaType.APPLICATION_JSON).content("{\"author\":\"x\"}"))
                .andExpect(status().isBadRequest());

        JsonNode game = json(mvc.perform(post("/api/games").param("bookId", "tiny-tale")).andReturn());
        JsonNode over = choose(game.get("id").asText(), 0);
        assertThat(over.get("status").asText()).isEqualTo("WON");
        assertThat(over.get("outcome").asText()).isEqualTo("WIN");
        assertThat(over.get("healthPoints").asInt()).isEqualTo(8);
    }

    @Test
    void exposesTheLibraryThroughMcp() throws Exception {
        mvc.perform(post("/mcp").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"initialize\",\"params\":{\"protocolVersion\":\"2025-03-26\"}}"))
                .andExpect(jsonPath("$.result.serverInfo.name").value("adventure-book"));
        mvc.perform(post("/mcp").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"jsonrpc\":\"2.0\",\"method\":\"notifications/initialized\"}"))
                .andExpect(status().isAccepted());
        mvc.perform(post("/mcp").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"jsonrpc\":\"2.0\",\"id\":2,\"method\":\"tools/list\"}"))
                .andExpect(jsonPath("$.result.tools[0].name").value("list_books"));
        MvcResult call = mvc.perform(post("/mcp").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"jsonrpc\":\"2.0\",\"id\":3,\"method\":\"tools/call\",\"params\":{\"name\":\"list_books\",\"arguments\":{\"difficulty\":\"easy\"}}}"))
                .andExpect(jsonPath("$.result.isError").value(false)).andReturn();
        String text = json(call).at("/result/content/0/text").asText();
        assertThat(mapper.readTree(text).get("content").get(0).get("id").asText()).isEqualTo("crystal-caverns");
    }

    private JsonNode choose(String gameId, int option) throws Exception {
        return json(mvc.perform(post("/api/games/{id}/choices", gameId).contentType(MediaType.APPLICATION_JSON)
                .content("{\"optionIndex\":" + option + "}")).andExpect(status().isOk()).andReturn());
    }

    private JsonNode json(MvcResult result) throws Exception {
        return mapper.readTree(result.getResponse().getContentAsString());
    }
}
