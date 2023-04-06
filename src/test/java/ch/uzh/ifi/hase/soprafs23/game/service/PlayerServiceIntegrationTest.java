package ch.uzh.ifi.hase.soprafs23.game.service;

import ch.uzh.ifi.hase.soprafs23.constant.GameMode;
import ch.uzh.ifi.hase.soprafs23.constant.PlayerColor;
import ch.uzh.ifi.hase.soprafs23.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs23.game.entity.Game;
import ch.uzh.ifi.hase.soprafs23.game.entity.Player;
import ch.uzh.ifi.hase.soprafs23.game.entity.User;
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

import static org.junit.jupiter.api.Assertions.*;

@WebAppConfiguration
@SpringBootTest
class PlayerServiceIntegrationTest {

    @Qualifier("playerRepository")
    @Autowired
    private PlayerRepository playerRepository;

    @Qualifier("userRepository")
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GameService gameService;

    @Autowired
    private WebSocketService webSocketService;

    @Autowired
    private UserService userService;

    @Autowired
    private PlayerService playerService;

    private Player p1;
    private User u1;

    @BeforeEach
    public void setup() {

        u1 = new User();
        u1.setUsername("username");
        u1.setPassword("password");
        u1.setStatus(UserStatus.ONLINE);

        p1 = new Player();
        p1.setUserToken("userToken");
        p1.setPlayerName("playerName");
        p1.setGameId(1L);
        p1.setPlayerColor(PlayerColor.BLUE);

        playerRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void getPlayers() {
        // given - empty repo
        assertEquals(playerService.getPlayers().size(), 0);
    }

    @Test
    void createPlayerFromUserToken() {
        // given - creating a user
        User u1_created = userService.createUser(u1);

        // given - create a player from the user
        Player player_created = playerService.createPlayerFromUserToken(u1_created.getToken());
        Player player_persisted = playerService.getPlayerById(player_created.getId());

        // then - gameId is still NULL in both
        assertNull(player_created.getGameId());
        assertNull(player_persisted.getGameId());

        // then - player_persisted has ID
        assertNotNull(player_persisted.getId());

        // then - otherwise player is created and persisted
        assertNotNull(player_created.getToken());
        assertNotNull(player_created.getId());
        assertEquals(player_created.getPlayerName(), u1_created.getUsername());
        assertEquals(player_created.getUserToken(), u1_created.getToken());
        assertEquals(player_created.getPlayerColor(), PlayerColor.NOTSET);
    }

    @Test
    void createPlayerFromUserToken_duplicatedPlayer() {
        // given - creating a user
        User u1_created = userService.createUser(u1);

        // given - create a player from the user
        Player player_created = playerService.createPlayerFromUserToken(u1_created.getToken());

        // then - create a second player from that user
        Player second_player_created = playerService.createPlayerFromUserToken(u1_created.getToken());

        // then - assert they are the same
        assertEquals(player_created.getId(), second_player_created.getId());
        assertEquals(player_created.getToken(), second_player_created.getToken());
        assertEquals(player_created.getUserToken(), second_player_created.getUserToken());
    }


    @Test
    void createPlayerFromUserToken_noUserThrows() {
        // given - create a player from user that does not exist
        assertThrows(
            ResponseStatusException.class,
            () -> playerService.createPlayerFromUserToken("userDoesNotExist")
        );
    }

    @Test
    void joinPlayer() {

        // given - creating a user
        User u1_created = userService.createUser(u1);

        // given - create a player from the user
        Player player_created = playerService.createPlayerFromUserToken(u1_created.getToken());
        Player player_persisted = playerService.getPlayerById(player_created.getId());

        // then - gameId is still NULL in both
        assertNull(player_created.getGameId());
        assertNull(player_persisted.getGameId());

        // create two games
        Long gameId1 = gameService.createNewGame("g1", GameMode.PVP);
        Long gameId2 = gameService.createNewGame("g2", GameMode.PVP);

        // add a player to the game
        Player player_joined = playerService.joinPlayer(u1.getToken(), gameId1.intValue());

        // then - gameId of the player_joined should match the one from gameId
        assertEquals(player_joined.getGameId(), gameId1);

        // add the player to another game
        Player player_joined_2 = playerService.joinPlayer(u1.getToken(), gameId2.intValue());

        // then - players are the same entity, gameId has changed
        assertEquals(player_joined.getId(), player_joined_2.getId());
        assertEquals(player_joined.getToken(), player_joined_2.getToken());
        assertEquals(player_joined.getUserToken(), player_joined_2.getUserToken());

        // in the playerRepository, only one should exist
        assertEquals(playerService.getPlayers().size(), 1);
        Player player_existing = playerService.getPlayerByUserToken(u1_created.getToken());
        assertEquals(player_existing.getGameId(), gameId2);

    }
}