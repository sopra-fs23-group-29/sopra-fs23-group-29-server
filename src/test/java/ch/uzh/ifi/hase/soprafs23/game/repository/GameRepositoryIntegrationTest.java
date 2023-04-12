package ch.uzh.ifi.hase.soprafs23.game.repository;

import ch.uzh.ifi.hase.soprafs23.constant.GameMode;
import ch.uzh.ifi.hase.soprafs23.game.entity.Game;
import ch.uzh.ifi.hase.soprafs23.game.service.PlayerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

class GameRepositoryIntegrationTest {


    @Autowired
    private PlayerService playerService;

    private Game g1, g1dup, g2;

    @BeforeEach
    void setUp() {
        g1 = new Game(1L, "g1", GameMode.PVP, playerService);
        g1dup = new Game(1L, "g1dup", GameMode.PVP, playerService);
        g2 = new Game(2L, "g2", GameMode.PVP, playerService);
        GameRepository.clear();
    }

    @Test
    void addGame() {
        // given new game added
        GameRepository.addGame(g1.getGameId(), g1);

        // then - assert game is there
        assertEquals(GameRepository.getSize(), 1);

        Game g1_found = GameRepository.findByGameId(1L);

        assertEquals(g1_found.getGameName(), "g1");
        assertEquals(g1_found.getGameId(), 1L);
        assertEquals(g1_found.getGameMode(), GameMode.PVP);
    }

    @Test
    void addGameTwice() {
        // given new game added
        GameRepository.addGame(g1.getGameId(), g1);
        // add same again, assert error thrown
        assertThrows(ResponseStatusException.class, () -> GameRepository.addGame(g1dup.getGameId(), g1dup));
    }

    @Test
    void removeGame() {
        // given two games added
        GameRepository.addGame(g1.getGameId(), g1);
        GameRepository.addGame(g2.getGameId(), g2);

        // given remove second game
        GameRepository.removeGame(g2.getGameId());

        // Make sure g1 is the only one there
        assertEquals(GameRepository.getSize(), 1);

        Game g1_found = GameRepository.findByGameId(1L);

        assertEquals(g1_found.getGameName(), "g1");
        assertEquals(g1_found.getGameId(), 1L);
        assertEquals(g1_found.getGameMode(), GameMode.PVP);

    }

    @Test
    void getAllGames() {
        // given empty repo
        assertEquals(GameRepository.getAllGames().size(), 0);

        // add two games
        GameRepository.addGame(g1.getGameId(), g1);
        GameRepository.addGame(g2.getGameId(), g2);

        // assert list of len 2
        assertEquals(
            new HashSet<>(GameRepository.getAllGames()),
            new HashSet<>(Arrays.asList(g1,g2))
        );

    }
}