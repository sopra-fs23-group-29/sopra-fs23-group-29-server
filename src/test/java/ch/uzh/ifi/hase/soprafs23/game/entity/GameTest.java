package ch.uzh.ifi.hase.soprafs23.game.entity;

import ch.uzh.ifi.hase.soprafs23.constant.*;
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
import org.springframework.web.server.ResponseStatusException;

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

    private Long gameId, gameIdHOWFAST, gameIdHOWFAR;
    private String userToken;

    @BeforeEach
    public void setup() {

        GameRepository.clear();
        playerRepository.deleteAll();
        userRepository.deleteAll();

        gameId = gameService.createNewGame("g1", GameMode.PVP, BoardSize.SMALL, MaxDuration.NA);
        gameIdHOWFAST = gameService.createNewGame("g2", GameMode.HOWFAST, BoardSize.SMALL, MaxDuration.NA);
        gameIdHOWFAR = gameService.createNewGame("g3", GameMode.HOWFAR, BoardSize.LARGE, MaxDuration.SHORT);

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
        // assert playerIdReadyToMove is an empty list
        Game g1 = gameService.getGameById(gameId);
        assertTrue(g1.getPlayersView().isEmpty());
        assertFalse(g1.readyToMovePlayers());
    }

    @Test
    void joinableUponCreation() {
        // given the game without any players
        // assert players is an empty list
        Game g1 = gameService.getGameById(gameId);
        assertTrue(g1.getJoinable());
    }

    @Test
    void notOverUponCreation() {
        // given the game without any players
        // assert game is not over
        Game g1 = gameService.getGameById(gameId);
        assertFalse(g1.gameOver());
        Game g2 = gameService.getGameById(gameIdHOWFAST);
        assertFalse(g2.gameOver());
        Game g3 = gameService.getGameById(gameIdHOWFAR);
        assertFalse(g3.gameOver());
    }

    @Test
    void overWhenWinningCondition() {
        // given the game without any players
        Game g1 = gameService.getGameById(gameId);
        // set boardsize = 0
        g1.setBoardSize(BoardSize.DUMMY_TEST);
        // assert not over, because GameStatus.INLOBBY
        assertFalse(g1.gameOver());

        // set gameStatus INPROGRESS and endGame()
        g1.setGameStatus(GameStatus.INPROGRESS);
        g1.endGame();
        // assert game over
        assertTrue(g1.gameOver());
    }

    @Test
    void endGame() {
        // given the game without any players
        Game g1 = gameService.getGameById(gameId);
        // assert that endGame throws RuntimeExecption if gameStatus INLOBBY
        assertThrows(AssertionError.class, () -> g1.endGame());

        // set gameState to INPROGRESS
        g1.setGameStatus(GameStatus.INPROGRESS);
        g1.endGame();

        // assert not joinable no more, also not ready to move
        assertFalse(g1.getJoinable());
        assertFalse(g1.readyToMovePlayers());
    }

    @Test
    void singleNeverJoinableAfterJoin() {
        // given the game without any players
        // assert players is an empty list
        Game g2_single = gameService.getGameById(gameIdHOWFAR);

        // assert joinable
        assertTrue(g2_single.getJoinable());

        // then join player and update the players
        playerService.joinPlayer(userToken, g2_single.getGameId().intValue());
        g2_single.updatePlayers();

        // assert not joinable anymore
        assertFalse(g2_single.getJoinable());
    }

    @Test
    void howfar_hitsBarrier_false() {
        Game g = gameService.getGameById(gameIdHOWFAR);
        assertFalse(g.hitsBarrier(99L));
    }

    @Test
    void player_beforeEnd_doesNotHitBarrier() {
        // given - add player to game
        Game g1 = gameService.getGameById(gameId);
        playerService.joinPlayer(userToken, g1.getGameId().intValue());

        g1.initGame();

        // get boardsize as in
        int g1_boardsize = g1.getBoardSize().getBoardSize();

        // set player score in leaderboard to 1 before end
        Player p = g1.getPlayersView().get(0);
        g1.getLeaderboard().getEntry(p.getId()).replaceScore(g1_boardsize-2);

        // assert player does not hit a barrier
        assertFalse(g1.hitsBarrier(p.getId()));

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

        // assert not ready to move
        assertFalse(g1.readyToMovePlayers());

        // assert not over
        assertFalse(g1.gameOver());
    }

    @Test
    void readyToMove() {
        // given - add player to game
        Game g1 = gameService.getGameById(gameId);
        playerService.joinPlayer(userToken, g1.getGameId().intValue());

        // then - init the game
        g1.initGame();

        // then - start a turn
        g1.nextTurn();

        // assert that we cannot move on yet
        assertFalse(g1.readyToMovePlayers());

        // add the one player to the playerIdReadyToMove
        Player p1 = playerService.getPlayerByUserToken(userToken);
        g1.addPlayerIdReadyToMove(p1.getId());

        // assert that we can move on now
        assertTrue(g1.readyToMovePlayers());

        // start new turn
        g1.nextTurn();

        // assert that we cannot move on yet
        assertFalse(g1.readyToMovePlayers());

    }

}