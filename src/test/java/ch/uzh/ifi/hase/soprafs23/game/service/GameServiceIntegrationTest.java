package ch.uzh.ifi.hase.soprafs23.game.service;

import ch.uzh.ifi.hase.soprafs23.constant.*;
import ch.uzh.ifi.hase.soprafs23.game.entity.*;
import ch.uzh.ifi.hase.soprafs23.game.questions.IQuestionService;
import ch.uzh.ifi.hase.soprafs23.game.questions.restCountry.BarrierQuestion;
import ch.uzh.ifi.hase.soprafs23.game.questions.restCountry.CountryService;
import ch.uzh.ifi.hase.soprafs23.game.questions.restCountry.RankingQuestion;
import ch.uzh.ifi.hase.soprafs23.game.repository.GameRepository;
import ch.uzh.ifi.hase.soprafs23.game.repository.PlayerRepository;
import ch.uzh.ifi.hase.soprafs23.game.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs23.game.websockets.dto.incoming.Answer;
import ch.uzh.ifi.hase.soprafs23.game.websockets.dto.incoming.BarrierAnswer;
import ch.uzh.ifi.hase.soprafs23.game.websockets.dto.incoming.MovePlayers;
import ch.uzh.ifi.hase.soprafs23.game.websockets.dto.outgoing.TurnOutgoingDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
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

        @Override
        public BarrierQuestion generateBarrierQuestion() {
            BarrierQuestion dummyBarrierQuestion = new BarrierQuestion(
              BarrierQuestionEnum.LANDLOCKED,
              countryService.getCountryData("GER"),
              new ArrayList<>()
            );
            return dummyBarrierQuestion;
        }
    }

    private Game g1;
    private Player p1, p2;
    private GameService dummyGameService;
    private IQuestionService getDummyQuestionService;

    @BeforeEach
    public void setup() {

        User u1, u2;
        this.dummyGameService = new GameService(playerService, new DummyQuestionService(), new WebSocketService());
        this.dummyQuestionService = new DummyQuestionService(); // used for the barrier question tests

        playerRepository.deleteAll();
        userRepository.deleteAll();

        g1 = new Game(1L, "g1", GameMode.PVP, BoardSize.SMALL, MaxDuration.NA, playerService, questionService);

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
        Long gameIdCreated = gameService.createNewGame(g1.getGameName(), g1.getGameMode(), g1.getBoardSize(), g1.getMaxDuration());

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
        Long gameIdCreated = gameService.createNewGame(g1.getGameName(), g1.getGameMode(), g1.getBoardSize(), g1.getMaxDuration());

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
        List<LeaderboardEntry> gameStartedLeaderboardEntries = gameStarted.getLeaderboard().getEntries();
        assertEquals(gameStartedLeaderboardEntries.size(), 2);
        for (LeaderboardEntry lbe : gameStartedLeaderboardEntries) {
            assertEquals(lbe.getCurrentScore(), 0);
        }
        // assert - barrierLeaderboards are filled, initialized to 0
        List<LeaderboardEntry> gameStartedBarrierLeaderboardEntries = gameStarted.getBarrierLeaderboard().getEntries();
        assertEquals(gameStartedBarrierLeaderboardEntries.size(), 2);
        for (LeaderboardEntry lbe : gameStartedBarrierLeaderboardEntries) {
            assertEquals(lbe.getCurrentScore(), 0);
        }

        // assert - Update players and make sure everything is still in order
        gameService.updatePlayers(gameStarted.getGameId());
        List<Player> gameStartedPlayersUpdated = gameStarted.getPlayersView();
        assertEquals(gameStartedPlayersUpdated.size(), 2);
        for (Player p : gameStartedPlayersUpdated) {
            assertNotSame(p.getPlayerColor(), PlayerColor.NOTSET);
        }
    }

    @Test
    void startGame_errorAlreadyStarted() {
        // given - adding a game via the service
        Long gameIdCreated = gameService.createNewGame(g1.getGameName(), g1.getGameMode(), g1.getBoardSize(), g1.getMaxDuration());

        // then - start the game and try to start again
        gameService.startGame(gameIdCreated);

        assertThrows(ResponseStatusException.class, () -> gameService.startGame(gameIdCreated));
    }

    @Test
    void startNextTurn() {
        // given - adding a game via the service
        Long gameIdCreated = gameService.createNewGame(g1.getGameName(), g1.getGameMode(), g1.getBoardSize(), g1.getMaxDuration());

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

    @Test
    void processAnswer() {
        // given - adding a game with a dummy questionService via the service
        Long gameIdCreated = dummyGameService.createNewGame("g_dummy", GameMode.PVP, BoardSize.SMALL, MaxDuration.NA);

        // given - add two players and start the game
        Player p1_added = playerService.joinPlayer(p1.getUserToken(), gameIdCreated.intValue());
        Player p2_added = playerService.joinPlayer(p2.getUserToken(), gameIdCreated.intValue());
        gameService.startGame(gameIdCreated);

        // given - start next turn
        gameService.startNextTurn(gameIdCreated);

        // assert that the taken guesses so far are empty
        Turn currentTurn = gameService.getGameById(gameIdCreated).getTurn();
        assertTrue(currentTurn.getTakenGuesses().isEmpty());

        // then - process a correct answer by p1_added
        Answer answer_correct = new Answer();
        answer_correct.setGuess(1);
        answer_correct.setCountryCode("GER");
        answer_correct.setUserToken(p1_added.getUserToken());
        // fetch the current turn id
        int currentTurnNumber = gameService.getGameById(gameIdCreated).getTurnNumber();
        Turn turnWithAnswer = gameService.processAnswer(answer_correct, p1_added.getId(), currentTurnNumber, gameIdCreated);
        TurnOutgoingDTO turnWithAnswerDTO = new TurnOutgoingDTO(turnWithAnswer);

        // fetch the takenGuesses
        List<Guess> takenGuesses = turnWithAnswerDTO.getTakenGuesses();
        // assert - there is a guess at the right player, the other one has not yet made a guess
        assertEquals(takenGuesses.size(), 1);

        // assert - player 1 made a guess
        // fetch the one guess made
        Guess takenGuess = takenGuesses.get(0);
        assertEquals(takenGuess.guessCountryCode(), "GER");
        assertEquals(takenGuess.guessPlayerId(), p1_added.getId());
        assertNotSame(takenGuess.guessPlayerColor(), PlayerColor.NOTSET);
        assertEquals(takenGuess.guess(), 1);

    }

    @Test
    void processAnswer_tokensNotMatching_throwsUnauthorized() {
        // given - adding a game with a dummy questionService via the service
        Long gameIdCreated = dummyGameService.createNewGame("g_dummy", GameMode.PVP, BoardSize.SMALL, MaxDuration.NA);

        // given - add two players and start the game
        Player p1_added = playerService.joinPlayer(p1.getUserToken(), gameIdCreated.intValue());
        Player p2_added = playerService.joinPlayer(p2.getUserToken(), gameIdCreated.intValue());
        gameService.startGame(gameIdCreated);

        // given - start next turn
        gameService.startNextTurn(gameIdCreated);

        // assert that the taken guesses so far are empty
        Turn currentTurn = gameService.getGameById(gameIdCreated).getTurn();
        assertTrue(currentTurn.getTakenGuesses().isEmpty());

        // then - set up answer with wrong player token
        Answer answer_incorrect = new Answer();
        answer_incorrect.setGuess(1);
        answer_incorrect.setCountryCode("GER");
        answer_incorrect.setUserToken(p2_added.getUserToken());
        // fetch the current turn id
        int currentTurnNumber = gameService.getGameById(gameIdCreated).getTurnNumber();

        // assert throws error
        assertThrows(ResponseStatusException.class, () -> gameService.processAnswer(answer_incorrect, p1_added.getId(), currentTurnNumber, gameIdCreated));

    }

    @Test
    void processAnswer_playerNotPartOfTurn_throwsBadRequest() {
        // given - adding a game with a dummy questionService via the service
        Long gameIdCreated = dummyGameService.createNewGame("g_dummy", GameMode.PVP, BoardSize.SMALL, MaxDuration.NA);

        // given - add one player and start the game
        Player p1_added = playerService.joinPlayer(p1.getUserToken(), gameIdCreated.intValue());
        gameService.startGame(gameIdCreated);

        // given - start next turn
        gameService.startNextTurn(gameIdCreated);

        // assert that the taken guesses so far are empty
        Turn currentTurn = gameService.getGameById(gameIdCreated).getTurn();
        assertTrue(currentTurn.getTakenGuesses().isEmpty());

        // then - set up answer with wrong player token who is not part of the turn
        Answer answer_correct = new Answer();
        answer_correct.setGuess(1);
        answer_correct.setCountryCode("GER");
        answer_correct.setUserToken(p1.getUserToken());
        // fetch the current turn id
        int currentTurnNumber = gameService.getGameById(gameIdCreated).getTurnNumber();

        // assert throws error when p2 tries to answer
        assertThrows(ResponseStatusException.class, () -> gameService.processAnswer(answer_correct, p2.getId(), currentTurnNumber, gameIdCreated));

    }

    @Test
    void processAnswer_turnNumberNotMatching_throwsBadRequest() {
        // given - adding a game with a dummy questionService via the service
        Long gameIdCreated = dummyGameService.createNewGame("g_dummy", GameMode.PVP, BoardSize.SMALL, MaxDuration.NA);

        // given - add two players and start the game
        Player p1_added = playerService.joinPlayer(p1.getUserToken(), gameIdCreated.intValue());
        Player p2_added = playerService.joinPlayer(p2.getUserToken(), gameIdCreated.intValue());
        gameService.startGame(gameIdCreated);

        // given - start next turn
        gameService.startNextTurn(gameIdCreated);

        // assert that the taken guesses so far are empty
        Turn currentTurn = gameService.getGameById(gameIdCreated).getTurn();
        assertTrue(currentTurn.getTakenGuesses().isEmpty());

        // then - set up correct answer
        Answer answer_correct = new Answer();
        answer_correct.setGuess(1);
        answer_correct.setCountryCode("GER");
        answer_correct.setUserToken(p1_added.getUserToken());
        // fetch the current turn id
        int currentTurnNumber = gameService.getGameById(gameIdCreated).getTurnNumber();

        // assert throws error when wrong turn number is given
        int wrongTurnNumber = currentTurnNumber + 1;
        assertThrows(ResponseStatusException.class, () -> gameService.processAnswer(answer_correct, p1_added.getId(), wrongTurnNumber, gameIdCreated));

    }

    @Test
    void processAnswer_playerAlreadyAnswered_throwsBadRequest() {
        // given - adding a game with a dummy questionService via the service
        Long gameIdCreated = dummyGameService.createNewGame("g_dummy", GameMode.PVP, BoardSize.SMALL, MaxDuration.NA);

        // given - add two players and start the game
        Player p1_added = playerService.joinPlayer(p1.getUserToken(), gameIdCreated.intValue());
        Player p2_added = playerService.joinPlayer(p2.getUserToken(), gameIdCreated.intValue());
        gameService.startGame(gameIdCreated);

        // given - start next turn
        gameService.startNextTurn(gameIdCreated);

        // assert that the taken guesses so far are empty
        Turn currentTurn = gameService.getGameById(gameIdCreated).getTurn();
        assertTrue(currentTurn.getTakenGuesses().isEmpty());

        // then - set up correct answer
        Answer answer_correct = new Answer();
        answer_correct.setGuess(1);
        answer_correct.setCountryCode("GER");
        answer_correct.setUserToken(p1_added.getUserToken());
        // fetch the current turn id
        int currentTurnNumber = gameService.getGameById(gameIdCreated).getTurnNumber();

        // given - answer once correct
        gameService.processAnswer(answer_correct, p1_added.getId(), currentTurnNumber, gameIdCreated);

        // assert a second answer throws error, everything else correct
        assertThrows(ResponseStatusException.class, () -> gameService.processAnswer(answer_correct, p1_added.getId(), currentTurnNumber, gameIdCreated));

    }

    @Test
    void processBarrierAnswer() {
        // given - adding a game with a dummy questionService via the service
        Long gameIdCreated = dummyGameService.createNewGame("g_dummy", GameMode.PVP, BoardSize.SMALL, MaxDuration.NA);

        // Add a player
        Player p1_added = playerService.joinPlayer(p1.getUserToken(), gameIdCreated.intValue());

        // Start the game
        gameService.startGame(gameIdCreated);
        gameService.startNextTurn(gameIdCreated);

        // Add a barrierQuestion
        Game gameCreated = gameService.getGameById(gameIdCreated);
        gameCreated.setCurrentBarrierQuestion(dummyQuestionService.generateBarrierQuestion());

        // create correct barrier answer
        BarrierAnswer answer_correct = new BarrierAnswer();
        answer_correct.setGuess("no");
        answer_correct.setUserToken(p1_added.getUserToken());

        // assert that there is a barrier question
        BarrierQuestion bq = gameService.getGameById(gameIdCreated).getCurrentBarrierQuestion();
        assertNotNull(bq);

        // given - The leaderboard of the player answering correct before the answer
        int scoreBefore = gameService.getGameById(gameIdCreated).getLeaderboard().getEntry(p1_added.getId()).getCurrentScore();

        // then - process the correct answer
        gameService.processBarrierAnswer(answer_correct, p1_added.getId(), gameIdCreated);

        // assert - Leaderboard entry of player answering raised by 1
        int scoreAfter = gameService.getGameById(gameIdCreated).getLeaderboard().getEntry(p1_added.getId()).getCurrentScore();

        assertEquals(scoreBefore, scoreAfter-1);

    }

    @Test
    void processBarrierAnswer_tokensNotMatching_throwsUnauthorized() {
        // given - adding a game with a dummy questionService via the service
        Long gameIdCreated = dummyGameService.createNewGame("g_dummy", GameMode.PVP, BoardSize.SMALL, MaxDuration.NA);

        // Add a player
        Player p1_added = playerService.joinPlayer(p1.getUserToken(), gameIdCreated.intValue());

        // Start the game
        gameService.startGame(gameIdCreated);
        gameService.startNextTurn(gameIdCreated);

        // Add a barrierQuestion
        Game gameCreated = gameService.getGameById(gameIdCreated);
        gameCreated.setCurrentBarrierQuestion(dummyQuestionService.generateBarrierQuestion());

        // create correct barrier answer
        BarrierAnswer answer_correct = new BarrierAnswer();
        answer_correct.setGuess("no");
        answer_correct.setUserToken(p1_added.getUserToken());

        // create wrong barrier answer with wrong userToken
        BarrierAnswer answer_wrong_token = new BarrierAnswer();
        answer_wrong_token.setGuess("no");
        answer_wrong_token.setUserToken(p2.getUserToken());

        // assert that there is a barrier question
        BarrierQuestion bq = gameService.getGameById(gameIdCreated).getCurrentBarrierQuestion();
        assertNotNull(bq);

        // assert - processing with wrong player token or wrong id throws exception
        assertThrows(ResponseStatusException.class, () -> gameService.processBarrierAnswer(answer_wrong_token, p1_added.getId(), gameIdCreated));
        assertThrows(ResponseStatusException.class, () -> gameService.processBarrierAnswer(answer_correct, p2.getId(), gameIdCreated));

    }

    @Test
    void processMovePlayers() {
        // given - adding a game with a dummy questionService via the service
        Long gameIdCreated = dummyGameService.createNewGame("g_dummy", GameMode.PVP, BoardSize.SMALL, MaxDuration.NA);

        // Add two players
        Player p1_added = playerService.joinPlayer(p1.getUserToken(), gameIdCreated.intValue());
        Player p2_added = playerService.joinPlayer(p2.getUserToken(), gameIdCreated.intValue());

        // Start the game
        gameService.startGame(gameIdCreated);
        gameService.startNextTurn(gameIdCreated);

        // create MovePlayers objects for all players in the game
        MovePlayers mp1 = new MovePlayers();
        mp1.setUserToken(p1_added.getUserToken());
        MovePlayers mp2 = new MovePlayers();
        mp2.setUserToken(p2_added.getUserToken());

        // assert - when not all players are ready, returns false
        assertFalse(gameService.processMovePlayers(mp1, gameIdCreated));

        // assert - when all players are ready, returns true
        assertTrue(gameService.processMovePlayers(mp2, gameIdCreated));
    }

    @Test
    void movePlayerByOne_noBarrier() {
        // given - adding a game with a dummy questionService via the service
        Long gameIdCreated = dummyGameService.createNewGame("g_dummy", GameMode.PVP, BoardSize.SMALL, MaxDuration.NA);

        // given - add two players and start the game
        Player p1_added = playerService.joinPlayer(p1.getUserToken(), gameIdCreated.intValue());
        Player p2_added = playerService.joinPlayer(p2.getUserToken(), gameIdCreated.intValue());
        gameService.startGame(gameIdCreated);

        // given - start next turn
        gameService.startNextTurn(gameIdCreated);

        int scoreBefore = gameService.getGameById(gameIdCreated).getLeaderboard().getEntry(p1_added.getId()).getCurrentScore();

        // assert - moving a player by 1 should not hit a barrier
        assertFalse(gameService.movePlayerByOne(gameIdCreated, p1_added.getId()));
        // assert - leaderboard entry increased by one
        int scoreAfter = gameService.getGameById(gameIdCreated).getLeaderboard().getEntry(p1_added.getId()).getCurrentScore();
        assertEquals(scoreBefore + 1, scoreAfter);
    }

    @Test
    void movePlayerByOne_hitBarrier() {
        // given - adding a game with a dummy questionService via the service
        Long gameIdCreated = dummyGameService.createNewGame("g_dummy", GameMode.PVP, BoardSize.SMALL, MaxDuration.NA);

        // given - add two players and start the game
        Player p1_added = playerService.joinPlayer(p1.getUserToken(), gameIdCreated.intValue());
        Player p2_added = playerService.joinPlayer(p2.getUserToken(), gameIdCreated.intValue());
        gameService.startGame(gameIdCreated);

        // given - start next turn
        gameService.startNextTurn(gameIdCreated);

        // given - moving player eventually leads to hit in barrier
        int FIRSTBARRIER = Game.BARRIERPOSITION;
        IntStream.range(0, FIRSTBARRIER-1).forEachOrdered(n -> {
            gameService.movePlayerByOne(gameIdCreated, p1_added.getId());
        });
        assertTrue(gameService.movePlayerByOne(gameIdCreated, p1_added.getId()));

    }

    @Test
    void movePlayerByOne_playerNotInGame_throwsBadRequest() {
        // given - adding a game with a dummy questionService via the service
        Long gameIdCreated = dummyGameService.createNewGame("g_dummy", GameMode.PVP, BoardSize.SMALL, MaxDuration.NA);

        // given - add two players and start the game
        Player p1_added = playerService.joinPlayer(p1.getUserToken(), gameIdCreated.intValue());
        gameService.startGame(gameIdCreated);

        // given - start next turn
        gameService.startNextTurn(gameIdCreated);

        // assert - moving p2 who is not in the game throws exception
        assertThrows(ResponseStatusException.class, () -> gameService.movePlayerByOne(gameIdCreated, p2.getId()));

    }

    @Test
    void endTurn() {
        // given - adding a game with a dummy questionService via the service
        Long gameIdCreated = dummyGameService.createNewGame("g_dummy", GameMode.PVP, BoardSize.SMALL, MaxDuration.NA);

        // given - add two players and start the game
        Player p1_added = playerService.joinPlayer(p1.getUserToken(), gameIdCreated.intValue());
        Player p2_added = playerService.joinPlayer(p2.getUserToken(), gameIdCreated.intValue());
        gameService.startGame(gameIdCreated);

        // given - start next turn
        gameService.startNextTurn(gameIdCreated);

        // assert that the taken guesses so far are empty, and the leaderboard of the turn as well
        Turn currentTurn = gameService.getGameById(gameIdCreated).getTurn();
        assertTrue(currentTurn.getTakenGuesses().isEmpty());

        // then - process a correct answer by p1_added
        Answer answer_correct = new Answer();
        answer_correct.setGuess(1);
        answer_correct.setCountryCode("GER");
        answer_correct.setUserToken(p1_added.getUserToken());
        int currentTurnNumber = gameService.getGameById(gameIdCreated).getTurnNumber();
        Turn turnWithAnswer = gameService.processAnswer(answer_correct, p1_added.getId(), currentTurnNumber, gameIdCreated);

        // then - end the turn and fetch the turn leaderboard
        gameService.endTurn(gameIdCreated, turnWithAnswer.getTurnNumber());
        Leaderboard turnLeaderboardAfterUpdate = gameService.getGameById(gameIdCreated).getTurn().getTurnResult();

        // assert the leaderboard has two entries
        assertEquals(turnLeaderboardAfterUpdate.getEntries().size(), 2);
        // assert p1_added has 3 points, and p2_added 0 because he did not answer
        assertEquals(turnLeaderboardAfterUpdate.getEntry(p1_added.getId()).getCurrentScore(), 3);
        assertEquals(turnLeaderboardAfterUpdate.getEntry(p2_added.getId()).getCurrentScore(), 0);
    }

    @Test
    void updateGame() {
        // given - adding a game via the service
        Long gameIdCreated = gameService.createNewGame(g1.getGameName(), g1.getGameMode(), g1.getBoardSize(), g1.getMaxDuration());

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
        Long gameIdCreated = gameService.createNewGame(g1.getGameName(), g1.getGameMode(), g1.getBoardSize(), g1.getMaxDuration());

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

    @Test
    void deleteGame_inLobbyIsDeleted() {
        // given - adding a game via the service
        Long gameIdCreated = gameService.createNewGame(g1.getGameName(), g1.getGameMode(), g1.getBoardSize(), g1.getMaxDuration());

        // give - add two players
        Player p1_joined = playerService.joinPlayer(p1.getUserToken(), gameIdCreated.intValue());
        Player p2_joined = playerService.joinPlayer(p2.getUserToken(), gameIdCreated.intValue());

        // assert one game in repo and two players in the repo
        assertEquals(1, GameRepository.getAllGames().size());
        assertEquals(2, playerService.getPlayers().size());

        // then - delete game
        gameService.deleteGame(gameIdCreated);

        // Assert empty GameRepository, empty PlayerRepository
        assertEquals(0, GameRepository.getAllGames().size());
        assertEquals(0, playerService.getPlayers().size());
    }
}