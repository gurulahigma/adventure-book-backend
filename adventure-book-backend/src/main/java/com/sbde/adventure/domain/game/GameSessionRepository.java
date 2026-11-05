package com.sbde.adventure.domain.game;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/** Repository of the GameSession aggregate. */
public interface GameSessionRepository extends JpaRepository<GameSession, String> {

    List<GameSession> findByCheckpointSavedAtIsNotNullOrderByCheckpointSavedAtDesc();
}
