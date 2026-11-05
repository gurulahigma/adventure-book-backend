package com.sbde.adventure.application.rest;

import com.sbde.adventure.application.dto.request.ChoiceRequest;
import com.sbde.adventure.application.dto.response.GameResponse;
import com.sbde.adventure.application.dto.response.SavedGameResponse;
import com.sbde.adventure.domain.game.GameService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/games")
public class GameController {

    private final GameService games;

    public GameController(GameService games) {
        this.games = games;
    }

    @PostMapping
    public ResponseEntity<GameResponse> start(@RequestParam String bookId) {
        GameResponse started = GameResponse.from(games.start(bookId));
        return ResponseEntity.created(URI.create("/api/games/" + started.id())).body(started);
    }

    @GetMapping("/saved")
    public List<SavedGameResponse> saved() {
        return games.listSaved().stream().map(SavedGameResponse::from).toList();
    }

    @GetMapping("/{id}")
    public GameResponse get(@PathVariable String id) {
        return GameResponse.from(games.get(id));
    }

    @PostMapping("/{id}/choices")
    public GameResponse choose(@PathVariable String id, @RequestBody ChoiceRequest request) {
        return GameResponse.from(games.choose(id, request.optionIndex()));
    }

    @PostMapping("/{id}/save")
    public GameResponse save(@PathVariable String id) {
        return GameResponse.from(games.save(id));
    }

    @PostMapping("/{id}/resume")
    public GameResponse resume(@PathVariable String id) {
        return GameResponse.from(games.resume(id));
    }
}
