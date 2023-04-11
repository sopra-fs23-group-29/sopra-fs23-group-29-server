package ch.uzh.ifi.hase.soprafs23.game.service;

import ch.uzh.ifi.hase.soprafs23.constant.GameMode;
import ch.uzh.ifi.hase.soprafs23.constant.GameStatus;
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

import java.util.List;

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
    private User u1,u2,u3,u4,u5,u6,u7;

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
        GameRepository.clear();
    }

    @Test
    void getPlayers() {
        // given - empty repo
        assertEquals(playerService.getPlayers().size(), 0);

        // given - creating a user
        User u1_created = userService.createUser(u1);

        // given - create a player from the user
        Player player_created = playerService.createPlayerFromUserToken(u1_created.getToken());

        // then - fetching all players
        List<Player> players_fetched = playerService.getPlayers();

        // assert one player there
        assertEquals(players_fetched.size(), 1);

        // fetch the one player
        Player player_fetched = players_fetched.get(0);

        assertNotNull(player_fetched.getToken());
        assertNotNull(player_fetched.getId());
        assertEquals(player_fetched.getUserToken(), u1_created.getToken());
        assertEquals(player_fetched.getPlayerColor(), PlayerColor.NOTSET);
        assertEquals(player_fetched.getPlayerName(), u1_created.getUsername());
        assertNull(player_fetched.getGameId());
    }

    @Test
    void getPlayers_empty() {
        // given - empty repo
        assertEquals(playerService.getPlayers().size(), 0);

        // then - fetching all players
        List<Player> players_fetched = playerService.getPlayers();

        // assert empty
        assertTrue(players_fetched.isEmpty());
    }

    @Test
    void getPlayersByGameId() {
        // given - empty repo
        assertEquals(playerService.getPlayers().size(), 0);

        // given - creating a user
        User u1_created = userService.createUser(u1);

        // given - create a player from the user
        Player player_created = playerService.createPlayerFromUserToken(u1_created.getToken());

        // create a game
        Long gameId1 = gameService.createNewGame("g1", GameMode.PVP);

        // add a player to the game
        Player player_joined = playerService.joinPlayer(u1.getToken(), gameId1.intValue());

        // then - fetching all players
        List<Player> players_fetched = playerService.getPlayersByGameId(gameId1);

        // assert one player there
        assertEquals(players_fetched.size(), 1);

        // fetch the one player
        Player player_fetched = players_fetched.get(0);

        assertNotNull(player_fetched.getToken());
        assertNotNull(player_fetched.getId());
        assertEquals(player_fetched.getGameId(), gameId1);
        assertEquals(player_fetched.getUserToken(), u1_created.getToken());
        assertEquals(player_fetched.getPlayerColor(), PlayerColor.NOTSET);
        assertEquals(player_fetched.getPlayerName(), u1_created.getUsername());
    }

    @Test
    void getPlayersByGameId_empty() {
        // given - empty repo
        assertEquals(playerService.getPlayers().size(), 0);

        // given game without players
        Long gameId1 = gameService.createNewGame("g1", GameMode.PVP);

        // then - fetching all players
        List<Player> players_fetched = playerService.getPlayersByGameId(gameId1);

        // assert empty
        assertTrue(players_fetched.isEmpty());
    }

    @Test
    void getPlayerById() {
        // given - creating a user
        User u1_created = userService.createUser(u1);

        // given - create a player from the user
        Player player_created = playerService.createPlayerFromUserToken(u1_created.getToken());

        // then - fetch player
        Player player_fetched = playerService.getPlayerById(player_created.getId());

        // assert is there
        assertNotNull(player_fetched.getToken());
        assertNotNull(player_fetched.getId());
        assertEquals(player_fetched.getUserToken(), u1_created.getToken());
        assertEquals(player_fetched.getPlayerColor(), PlayerColor.NOTSET);
        assertEquals(player_fetched.getPlayerName(), u1_created.getUsername());
        assertNull(player_fetched.getGameId());
    }

    @Test
    void getPlayerById_throwsNOT_FOUND() {
        // given empty playerRepository
        // given - create a player from user that does not exist
        assertThrows(
                ResponseStatusException.class,
                () -> playerService.getPlayerById(1L)
        );
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

        // given - empty game repo
        assertEquals(GameRepository.getAllGames().size(), 0);
        assertEquals(playerService.getPlayers().size(), 0);
        assertEquals(userService.getUsers().size(), 0);

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

    @Test
    void joinPlayer_errorNotINLOBBY() {

        // given - creating a user
        User u1_created = userService.createUser(u1);

        // given - create a player from the user
        Player player_created = playerService.createPlayerFromUserToken(u1_created.getToken());

        // create a game
        Long gameId1 = gameService.createNewGame("g1", GameMode.PVP);

        // change GameStatus to GameStatus.INPROGRESS
        Game g1 = gameService.getGameById(gameId1);
        g1.setGameStatus(GameStatus.INPROGRESS);

        // then - add a player gives error
        assertThrows(
                ResponseStatusException.class,
                () -> playerService.joinPlayer(u1_created.getToken(), gameId1.intValue())
        );

    }

    @Test
    void joinPlayer_errorFullGame() {

        // given - creating 7 users
        u2 = new User();
        u2.setUsername("username2");
        u2.setPassword("password");
        u2.setStatus(UserStatus.ONLINE);

        u3 = new User();
        u3.setUsername("username3");
        u3.setPassword("password");
        u3.setStatus(UserStatus.ONLINE);

        u4 = new User();
        u4.setUsername("username4");
        u4.setPassword("password");
        u4.setStatus(UserStatus.ONLINE);

        u5 = new User();
        u5.setUsername("username5");
        u5.setPassword("password");
        u5.setStatus(UserStatus.ONLINE);

        u6 = new User();
        u6.setUsername("username6");
        u6.setPassword("password");
        u6.setStatus(UserStatus.ONLINE);

        u7 = new User();
        u7.setUsername("username7");
        u7.setPassword("password");
        u7.setStatus(UserStatus.ONLINE);

        User u1_created = userService.createUser(u1);
        User u2_created = userService.createUser(u2);
        User u3_created = userService.createUser(u3);
        User u4_created = userService.createUser(u4);
        User u5_created = userService.createUser(u5);
        User u6_created = userService.createUser(u6);
        User u7_created = userService.createUser(u7);


        // given - create a player from the user
        Player player_created = playerService.createPlayerFromUserToken(u1_created.getToken());
        Player player2_created = playerService.createPlayerFromUserToken(u2_created.getToken());
        Player player3_created = playerService.createPlayerFromUserToken(u3_created.getToken());
        Player player4_created = playerService.createPlayerFromUserToken(u4_created.getToken());
        Player player5_created = playerService.createPlayerFromUserToken(u5_created.getToken());
        Player player6_created = playerService.createPlayerFromUserToken(u6_created.getToken());
        Player player7_created = playerService.createPlayerFromUserToken(u7_created.getToken());

        // create a game
        Long gameId1 = gameService.createNewGame("g1", GameMode.PVP);

        // add 6 players
        playerService.joinPlayer(u1.getToken(), gameId1.intValue());
        playerService.joinPlayer(u2.getToken(), gameId1.intValue());
        playerService.joinPlayer(u3.getToken(), gameId1.intValue());
        playerService.joinPlayer(u4.getToken(), gameId1.intValue());
        playerService.joinPlayer(u5.getToken(), gameId1.intValue());
        playerService.joinPlayer(u6.getToken(), gameId1.intValue());

        // then - add next player gives error
        assertThrows(
                ResponseStatusException.class,
                () -> playerService.joinPlayer(u7.getToken(), gameId1.intValue())
        );

    }

    @Test
    void deletePlayer() {
        // given - creating a user
        User u1_created = userService.createUser(u1);

        // given - create a player from the user
        Player player_created = playerService.createPlayerFromUserToken(u1_created.getToken());

        // assert there is one player
        List<Player> players_fetched = playerService.getPlayers();
        assertEquals(players_fetched.size(), 1);

        // then - delete the player
        playerService.deletePlayerById(player_created.getId());

        // assert there is no player
        assertTrue(playerService.getPlayers().isEmpty());
    }

    @Test
    void deletePlayer_throwsNOT_FOUND() {
        // given empty playerRepository

        // then - delete the player throws
        assertThrows(
                ResponseStatusException.class,
                () -> playerService.deletePlayerById(1L)
        );
    }
}