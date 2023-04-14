package ch.uzh.ifi.hase.soprafs23.game.service;

import ch.uzh.ifi.hase.soprafs23.constant.GameMode;
import ch.uzh.ifi.hase.soprafs23.game.entity.Game;
import ch.uzh.ifi.hase.soprafs23.game.questions.IQuestionService;
import ch.uzh.ifi.hase.soprafs23.game.repository.GameRepository;
import ch.uzh.ifi.hase.soprafs23.game.repository.PlayerRepository;
import ch.uzh.ifi.hase.soprafs23.game.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@WebAppConfiguration
@SpringBootTest
class GameServiceIntegrationTest {

    @Qualifier("playerRepository")
    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private PlayerService playerService;

    @Autowired
    private GameService gameService;

    @Autowired
    private WebSocketService webSocketService;
    @Autowired
    private IQuestionService questionService;

    private Game g1;

    @BeforeEach
    public void setup() {
        g1 = new Game(1L, "g1", GameMode.PVP, playerService, questionService);
        playerRepository.deleteAll();
        GameRepository.clear();
    }

    @Test
    void getGameById() {
        // given - add game to repo
        GameRepository.addGame(g1.getGameId(), g1);

        // then - retrieve the game
        Game g1_found = gameService.getGameById(g1.getGameId());

        assertEquals(g1_found.getGameName(), g1.getGameName());
        assertEquals(g1_found.getGameId(), g1.getGameId());
        assertEquals(g1_found.getGameMode(), g1.getGameMode());

    }

    @Test
    void getGameById_throwsNOT_FOUND() {
        // given - empty repo
        // then - retrieve the game throws error
        assertThrows(ResponseStatusException.class, () -> gameService.getGameById(1L));
    }

    @Test
    void createNewGame() {
        // given - adding a game via the service
        Long gameIdCreated = gameService.createNewGame(g1.getGameName(), g1.getGameMode());

        // then - assert one game exists
        assertEquals(GameRepository.getSize(), 1);

        // then - assert that game has been correctly created with ID gameIdCreated
        Game g_created = gameService.getGameById(gameIdCreated);

        assertEquals(g_created.getGameId(), gameIdCreated);
        assertEquals(g_created.getGameName(), g1.getGameName());
        assertEquals(g_created.getGameMode(), g1.getGameMode());
    }

    @Test
    void updateGame() {
        // given - adding a game via the service
        Long gameIdCreated = gameService.createNewGame(g1.getGameName(), g1.getGameMode());

        // then - call updateGame
        gameService.updateGame(gameIdCreated);

        // assert gameRepository was not affected
        List<Game> allGames = GameRepository.getAllGames();
        assertEquals(allGames.size(), 1);

        Game gameToCheck = gameService.getGameById(gameIdCreated);
        assertEquals(gameToCheck.getGameId(), gameIdCreated);
        assertEquals(gameToCheck.getGameName(), g1.getGameName());
        assertEquals(gameToCheck.getGameMode(), g1.getGameMode());
    }

    @Test
    void greetGames() {
        // given - adding a game via the service
        Long gameIdCreated = gameService.createNewGame(g1.getGameName(), g1.getGameMode());

        // then - call updateGame
        gameService.greetGames();

        // assert gameRepository was not affected
        List<Game> allGames = GameRepository.getAllGames();
        assertEquals(allGames.size(), 1);

        Game gameToCheck = gameService.getGameById(gameIdCreated);
        assertEquals(gameToCheck.getGameId(), gameIdCreated);
        assertEquals(gameToCheck.getGameName(), g1.getGameName());
        assertEquals(gameToCheck.getGameMode(), g1.getGameMode());
    }
}