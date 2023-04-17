package ch.uzh.ifi.hase.soprafs23.game.entity;

import ch.uzh.ifi.hase.soprafs23.constant.GameMode;
import ch.uzh.ifi.hase.soprafs23.constant.PlayerColor;
import ch.uzh.ifi.hase.soprafs23.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs23.game.repository.GameRepository;
import ch.uzh.ifi.hase.soprafs23.game.repository.PlayerRepository;
import ch.uzh.ifi.hase.soprafs23.game.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs23.game.service.GameService;
import ch.uzh.ifi.hase.soprafs23.game.service.PlayerService;
import ch.uzh.ifi.hase.soprafs23.game.service.UserService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@WebAppConfiguration
@SpringBootTest
class GameTest {

    @Qualifier("playerRepository")
    @Autowired
    private PlayerRepository playerRepository;

    @Qualifier("userRepository")
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PlayerService playerService;
    @Autowired
    private UserService userService;
    @Autowired
    private GameService gameService;

    private Long gameId;
    private String userToken;

    @BeforeEach
    public void setup() {

        GameRepository.clear();
        playerRepository.deleteAll();
        userRepository.deleteAll();

        gameId = gameService.createNewGame("g1", GameMode.PVP);

        // create a player from dummy user
        User u1;
        u1 = new User();
        u1.setUsername("username");
        u1.setPassword("password");
        u1.setStatus(UserStatus.ONLINE);
        User u1_created = userService.createUser(u1);
        userToken = u1_created.getToken();

        playerService.createPlayerFromUserToken(u1_created.getToken());
    }

    @Test
    void playersNull() {
        // given the game without any players
        // assert players is an empty list
        Game g1 = gameService.getGameById(gameId);
        assertTrue(g1.getPlayersView().isEmpty());
    }

    @Test
    void joinableUponCreation() {
        // given the game without any players
        // assert players is an empty list
        Game g1 = gameService.getGameById(gameId);
        assertTrue(g1.getJoinable());
    }

    @Test
    void initGame() {
        // given - add player to game
        Game g1 = gameService.getGameById(gameId);
        playerService.joinPlayer(userToken, g1.getGameId().intValue());

        // then - init the game
        g1.initGame();

        // assert PlayerColors are valid
        List<Player> g1_players = g1.getPlayersView();
        for (Player p : g1_players) {
            assertNotSame(p.getPlayerColor(), PlayerColor.NOTSET);
        }

        // assert not joinable
        assertFalse(g1.getJoinable());
    }

}