package ch.uzh.ifi.hase.soprafs23.game.service;

import ch.uzh.ifi.hase.soprafs23.constant.*;
import ch.uzh.ifi.hase.soprafs23.game.entity.*;
import ch.uzh.ifi.hase.soprafs23.game.questions.IQuestionService;
import ch.uzh.ifi.hase.soprafs23.game.questions.restCountry.CountryService;
import ch.uzh.ifi.hase.soprafs23.game.questions.restCountry.RankingQuestion;
import ch.uzh.ifi.hase.soprafs23.game.repository.GameRepository;
import ch.uzh.ifi.hase.soprafs23.game.repository.PlayerRepository;
import ch.uzh.ifi.hase.soprafs23.game.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs23.game.websockets.dto.incoming.Answer;
import ch.uzh.ifi.hase.soprafs23.game.websockets.dto.outgoing.TurnOutgoingDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@WebAppConfiguration
@SpringBootTest
class GameServiceIntegrationTest {

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
    @Autowired
    private IQuestionService questionService;
    @Autowired
    private CountryService countryService;

    private IQuestionService dummyQuestionService;

    class DummyQuestionService implements IQuestionService {
        @Override
        public RankingQuestion generateRankQuestion(int size) {
            // always return rankingQuestion of size 1 with same countryCode
            List<Country> dummyList = new ArrayList<>();
            dummyList.add(countryService.getCountryData("GER"));
            return new RankingQuestion(RankingQuestionEnum.AREA, dummyList);
        }
    }

    private Game g1;
    private Player p1, p2;
    private GameService dummyGameService;

    @BeforeEach
    public void setup() {

        User u1, u2;
        this.dummyGameService = new GameService(playerService, new DummyQuestionService(), new WebSocketService());

        playerRepository.deleteAll();
        userRepository.deleteAll();

        g1 = new Game(1L, "g1", GameMode.PVP, playerService, questionService);

        u1 = new User();
        u1.setUsername("u1");
        u1.setPassword("p1");
        u1.setStatus(UserStatus.ONLINE);
        userService.createUser(u1);

        u2 = new User();
        u2.setUsername("u2");
        u2.setPassword("p2");
        u2.setStatus(UserStatus.ONLINE);
        userService.createUser(u2);

        p1 = playerService.createPlayerFromUserToken(u1.getToken());
        p2 = playerService.createPlayerFromUserToken(u2.getToken());

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
    void startGame() {
        // given - adding a game via the service
        Long gameIdCreated = gameService.createNewGame(g1.getGameName(), g1.getGameMode());

        // then - add two players and start the game
        playerService.joinPlayer(p1.getUserToken(), gameIdCreated.intValue());
        playerService.joinPlayer(p2.getUserToken(), gameIdCreated.intValue());
        gameService.startGame(gameIdCreated);

        // then - fetch the game started
        Game gameStarted = GameRepository.findByGameId(gameIdCreated);

        // assert - GameStatus INPROGRESS
        assertEquals(gameStarted.getGameStatus(), GameStatus.INPROGRESS);
        // assert - All players are there and have valid colours
        List<Player> gameStartedPlayers = gameStarted.getPlayersView();
        assertEquals(gameStartedPlayers.size(), 2);
        for (Player p : gameStartedPlayers) {
            assertNotSame(p.getPlayerColor(), PlayerColor.NOTSET);
        }
        // assert - leaderboards are filled, initialized to 0
        List<LeaderboardEntry> gameStartedLeaderboardEntries = gameStarted.getLeaderboard().getPlayers();
        assertEquals(gameStartedLeaderboardEntries.size(), 2);
        for (LeaderboardEntry lbe : gameStartedLeaderboardEntries) {
            assertEquals(lbe.getCurrentScore(), 0);
        }
        // assert - barrierLeaderboards are filled, initialized to 0
        List<LeaderboardEntry> gameStartedBarrierLeaderboardEntries = gameStarted.getBarrierLeaderboard().getPlayers();
        assertEquals(gameStartedBarrierLeaderboardEntries.size(), 2);
        for (LeaderboardEntry lbe : gameStartedBarrierLeaderboardEntries) {
            assertEquals(lbe.getCurrentScore(), 0);
        }
    }

    @Test
    void startGame_errorAlreadyStarted() {
        // given - adding a game via the service
        Long gameIdCreated = gameService.createNewGame(g1.getGameName(), g1.getGameMode());

        // then - start the game and try to start again
        gameService.startGame(gameIdCreated);

        assertThrows(ResponseStatusException.class, () -> gameService.startGame(gameIdCreated));
    }

    @Test
    void startNextTurn() {
        // given - adding a game via the service
        Long gameIdCreated = gameService.createNewGame(g1.getGameName(), g1.getGameMode());

        // given - add two players and start the game
        Player p1_added = playerService.joinPlayer(p1.getUserToken(), gameIdCreated.intValue());
        Player p2_added = playerService.joinPlayer(p2.getUserToken(), gameIdCreated.intValue());
        gameService.startGame(gameIdCreated);
        List<Long> playerIdsAdded = Stream.of(p1_added, p2_added).map(Player::getId).toList();
        List<String> playerTokenAdded = Stream.of(p1_added, p2_added).map(Player::getToken).toList();

        // fetch the game
        Game gameStarted = GameRepository.findByGameId(gameIdCreated);

        // given - turn is still null
        assertNull(gameStarted.getTurn());

        // then - start a next turn
        gameService.startNextTurn(gameIdCreated);
        // fetch the turn and its players
        Turn turnCreated = gameStarted.getTurn();
        List<Player> turnCreatedPlayers = turnCreated.getTurnPlayers();

        // assert - turnNumber is 1
        assertEquals(turnCreated.getTurnNumber(), 1);
        // assert - All players are there by ID and Token
        for (Player p : turnCreatedPlayers) {
          assertTrue(playerIdsAdded.contains(p.getId()));
          assertTrue(playerTokenAdded.contains(p.getToken()));
        }

    }

//    @Test
//    void processAnswer() {
//        // given - adding a game with a dummy questionService via the service
//        Long gameIdCreated = dummyGameService.createNewGame("g_dummy", GameMode.PVP);
//
//        // given - add two players and start the game
//        Player p1_added = playerService.joinPlayer(p1.getUserToken(), gameIdCreated.intValue());
//        Player p2_added = playerService.joinPlayer(p2.getUserToken(), gameIdCreated.intValue());
//        gameService.startGame(gameIdCreated);
//
//        // given - start next turn
//        gameService.startNextTurn(gameIdCreated);
//
//        // assert that the answers are empty
//        Turn currentTurn = gameService.getGameById(gameIdCreated).getTurn();
//        assertTrue(currentTurn.getSavedGuesses().isEmpty());
//        assertTrue(currentTurn.getSavedColors().isEmpty());
//        assertTrue(currentTurn.getTurnPlayersDone().isEmpty());
//
//        // then - process a correct answer by p1_added
//        Answer answer_correct = new Answer();
//        answer_correct.setGuess(1);
//        answer_correct.setCountryCode("GER");
//        answer_correct.setUserToken(p1_added.getUserToken());
//        // fetch the current turn id
//        int currentTurnNumber = gameService.getGameById(gameIdCreated).getTurnNumber();
//        TurnOutgoingDTO turnWithAnswer = gameService.processAnswer(answer_correct, p1_added.getId(), currentTurnNumber, gameIdCreated);
//
//        // assert - there is a guess at the right player, the other one has not yet made a guess
//        assertEquals(turnWithAnswer.getSavedGuesses().size(), 1);
//        assertEquals(turnWithAnswer.getTurnPlayersDone().get(p1_added), "GER");
//        assertEquals(turnWithAnswer.getSavedGuesses().get("GER"), 1);
//        assertEquals(turnWithAnswer.getSavedColors().get("GER"), p1_added.getPlayerColor());
//
//    }

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