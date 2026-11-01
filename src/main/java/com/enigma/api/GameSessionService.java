package com.enigma.api;

import com.enigma.GameController;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Keeps one in-memory game state per browser session. */
@Service
public class GameSessionService {
    private final Map<String, GameController> games = new ConcurrentHashMap<>();

    public String createGame(String name, int age, String gender) {
        String gameId = UUID.randomUUID().toString();
        games.put(gameId, new GameController(name, age, gender));
        return gameId;
    }

    public GameController getGame(String gameId) {
        GameController game = games.get(gameId);
        if (game == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Game session was not found.");
        }
        return game;
    }
}
