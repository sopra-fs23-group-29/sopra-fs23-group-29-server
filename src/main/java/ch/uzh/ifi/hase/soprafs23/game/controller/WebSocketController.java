package ch.uzh.ifi.hase.soprafs23.game.controller;

import ch.uzh.ifi.hase.soprafs23.game.service.GameService;
import ch.uzh.ifi.hase.soprafs23.game.service.UserService;
import ch.uzh.ifi.hase.soprafs23.game.service.WebSocketService;
import ch.uzh.ifi.hase.soprafs23.game.entity.Game;
import ch.uzh.ifi.hase.soprafs23.game.entity.Leaderboard;
import ch.uzh.ifi.hase.soprafs23.game.entity.Turn;
import ch.uzh.ifi.hase.soprafs23.game.questions.IQuestionService;
import ch.uzh.ifi.hase.soprafs23.game.questions.restCountry.BarrierQuestion;
import ch.uzh.ifi.hase.soprafs23.game.websockets.dto.incoming.Answer;
import ch.uzh.ifi.hase.soprafs23.game.websockets.dto.incoming.BarrierAnswer;
import ch.uzh.ifi.hase.soprafs23.game.websockets.dto.outgoing.GameUpdateDTO;
import ch.uzh.ifi.hase.soprafs23.game.websockets.dto.outgoing.LeaderboardDTO;
import ch.uzh.ifi.hase.soprafs23.game.websockets.dto.outgoing.TurnOutgoingDTO;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketController {
    private final WebSocketService webSocketService;
    private final GameService gameService;
    private final UserService userService;
    private final IQuestionService questionService;
    Logger log = LoggerFactory.getLogger(WebSocketController.class);

    public WebSocketController(
      WebSocketService webSocketService,
      GameService gameService,
      UserService userService,
      IQuestionService questionService
    ) {
        this.webSocketService = webSocketService;
        this.gameService = gameService;
        this.userService = userService;
        this.questionService = questionService;
    }


    // viewing a single user
    @MessageMapping("/users/{userId}")
    public void showUser(@DestinationVariable long userId) {
        String userString = userService.getUserById(userId).toString();
        this.webSocketService.sendMessageToClients("/users/" + userId, userString);
    }

    /**
     * Send all games to all subscribers in /games
     */
    @MessageMapping("/games/getAllGames")
    public void getAllGames() {
        log.info("Sending getAllGames message");
        gameService.greetGames();
    }

    /**
     * Send the game at gameId to all subscribers in /games/gameId
     */
    @MessageMapping("/games/{gameId}/getGame")
    public void getGame(@DestinationVariable long gameId) {
        log.info("Sending getGame message to game {}", gameId);
        gameService.updateGame((long) gameId);
    }

    /**
     * Start a game
     * Returns a Turn object for the client to work with
     */
    @MessageMapping("/games/{gameId}/startGame")
    public void startGame(@DestinationVariable long gameId) {
        log.info("Start Game {}", gameId);
        gameService.startGame(gameId);
        log.info("Create Turn");
        gameService.startNextTurn(gameId);
        Turn nextTurn = gameService.getGameById(gameId).getTurn();
        log.info("Created Turn {}", nextTurn.getTurnNumber());

        TurnOutgoingDTO nextTurnDTO = new TurnOutgoingDTO(nextTurn);

        String nextTurnDTOasString = new Gson().toJson(nextTurnDTO);

        // send the new Turn to all subscribers
        webSocketService.sendMessageToClients("/topic/games/" + gameId, nextTurnDTOasString);
        // also send to /games to remove games not joinable anymore
        gameService.greetGames();
    }


    /**
     * Start a new turn in a game
     * The game must be in progress already, otherwise throw BAD_REQUEST
     * Sends a message containing the next turn
     * If the game has reached the winning condition, a message to /games/gameId is issued with the updated game
     * - GameStatus.FINISHED
     */
    @MessageMapping("/games/{gameId}/nextTurn")
    public void nextTurn(@DestinationVariable long gameId) {
        log.info("Game {} next turn", gameId);
        gameService.startNextTurn(gameId);

        // check if the game is over, if so, just send the game object
        Game gameNextTurn = gameService.getGameById(gameId);
        if (gameNextTurn.gameOver()) {
            log.info("Game {} is over", gameId);
            GameUpdateDTO gameOver = new GameUpdateDTO(gameNextTurn);
            String gameOverAsString = new Gson().toJson(gameOver);
            // send the game over to all subscribers
            webSocketService.sendMessageToClients("/topic/games/" + gameId, gameOverAsString);
            return;
        }

        Turn nextTurn = gameService.getGameById(gameId).getTurn();
        log.info("Created Turn {}", nextTurn.getTurnNumber());

        TurnOutgoingDTO nextTurnDTO = new TurnOutgoingDTO(nextTurn);

        String nextTurnDTOasString = new Gson().toJson(nextTurnDTO);

        // send the new Turn to all subscribers
        webSocketService.sendMessageToClients("/topic/games/" + gameId, nextTurnDTOasString);
    }


    /**
     * Save an answer from a player for a given turn rank question in a given game
     * Returns an updated Turn object for the client to work with
     * todo: Is turnNumber necessary? a gameId has only one current active turn, one could assume thats always the
     *  turn we are looking at...
     */
    @MessageMapping("/games/{gameId}/turn/{turnNumber}/player/{playerId}/saveAnswer")
    public void saveAnswer(
      @DestinationVariable long gameId,
      @DestinationVariable int turnNumber,
      @DestinationVariable long playerId,
      Answer answer
    ) {

        // check if the game is over, if so, just send the game object
        Game gameNextTurn = gameService.getGameById(gameId);
        if (gameNextTurn.gameOver()) {
            log.info("Game {} is over", gameId);
            GameUpdateDTO gameOver = new GameUpdateDTO(gameNextTurn);
            String gameOverAsString = new Gson().toJson(gameOver);
            // send the game over to all subscribers
            webSocketService.sendMessageToClients("/topic/games/" + gameId, gameOverAsString);
            return;
        }

        log.info("Update Game {} Turn {} with rank answer from Player {}", gameId, turnNumber, playerId);
        Turn updatedTurn = gameService.processAnswer(answer, playerId, turnNumber, gameId);
        TurnOutgoingDTO updatedTurnDTO = new TurnOutgoingDTO(updatedTurn);

        String turnOutgoingDTOasString = new Gson().toJson(updatedTurnDTO);

        // send the updated Turn to all subscribers
        webSocketService.sendMessageToClients("/topic/games/" + gameId, turnOutgoingDTOasString);

    }


    /**
     * Resolve a barrier question answer from a player for a given barrier question in a given game
     * Returns an updated Game object, either the leaderboard is increased by one or not
     * todo: Is turnNumber necessary? a gameId has only one current active turn, one could assume thats always the
     *  turn we are looking at...
     */
    @MessageMapping("/games/{gameId}/player/{playerId}/resolveBarrierAnswer")
    public void resolveBarrierAnswer(
      @DestinationVariable long gameId,
      @DestinationVariable long playerId,
      BarrierAnswer barrierAnswer
    ) {
        // check if the game is over, if so, just send the game object
        Game gameNextTurn = gameService.getGameById(gameId);
        if (gameNextTurn.gameOver()) {
            log.info("Game {} is over", gameId);
            GameUpdateDTO gameOver = new GameUpdateDTO(gameNextTurn);
            String gameOverAsString = new Gson().toJson(gameOver);
            // send the game over to all subscribers
            webSocketService.sendMessageToClients("/topic/games/" + gameId, gameOverAsString);
            return;
        }

        log.info("Update Game {} with barrier answer from Player {}", gameId, playerId);
        Game gameUpdated = gameService.processBarrierAnswer(barrierAnswer, playerId, gameId);
        gameService.updateGame(gameId);
    }


    /**
     * End a turn, send the turn leaderboard, stating which player can advance how many fields
     */
    @MessageMapping("/games/{gameId}/turn/{turnNumber}/endTurn")
    public void endTurn(
      @DestinationVariable long gameId,
      @DestinationVariable int turnNumber
    ) {
        // check if the game is over, if so, just send the game object
        Game gameNextTurn = gameService.getGameById(gameId);
        if (gameNextTurn.gameOver()) {
            log.info("Game {} is over", gameId);
            GameUpdateDTO gameOver = new GameUpdateDTO(gameNextTurn);
            String gameOverAsString = new Gson().toJson(gameOver);
            // send the game over to all subscribers
            webSocketService.sendMessageToClients("/topic/games/" + gameId, gameOverAsString);
            return;
        }

        log.info("Game {} end current Turn", gameId);
        Leaderboard turnResults = gameService.endTurn(gameId, turnNumber);
        Turn currentTurn = gameService.getGameById(gameId).getTurn();
        // Make a DTO
        LeaderboardDTO turnResultsDTO = new LeaderboardDTO(turnResults, currentTurn);

        String leaderboardDTOasString = new Gson().toJson(turnResultsDTO);

        // send the updated Leaderboard to all subscribers
        webSocketService.sendMessageToClients("/topic/games/" + gameId, leaderboardDTOasString);

    }

    /**
     * Ask to move playerId in gameId by one field
     * Either hit a barrier or not
     * If a barrier is hit, the Controller send a barrierQuestionDTO
     * If not, the game is updated and a gameDTO is sent
     */
    @MessageMapping("/games/{gameId}/player/{playerId}/moveByOne")
    public void movePlayerByOne(
      @DestinationVariable long gameId,
      @DestinationVariable long playerId
    ) {
        // check if the game is over, if so, just send the game object
        Game gameNextTurn = gameService.getGameById(gameId);
        if (gameNextTurn.gameOver()) {
            log.info("Game {} is over", gameId);
            GameUpdateDTO gameOver = new GameUpdateDTO(gameNextTurn);
            String gameOverAsString = new Gson().toJson(gameOver);
            // send the game over to all subscribers
            webSocketService.sendMessageToClients("/topic/games/" + gameId, gameOverAsString);
            return;
        }

        log.info("Game {} move players", gameId);
        // Ask the game service to move playerId in gameId by one field
        boolean barrierHit = gameService.movePlayerByOne(gameId, playerId);

        // If a barrier is hit, create a new barrier question, update the game with it and send it
        if (barrierHit) {
            BarrierQuestion barrierQuestion = questionService.generateBarrierQuestion();
            gameService.getGameById(gameId).setCurrentBarrierQuestion(barrierQuestion);
            String barrierQuestionAsString = new Gson().toJson(barrierQuestion);
            // send the barrierQuestion
            webSocketService.sendMessageToClients("/topic/games/" + gameId, barrierQuestionAsString);

        } else {
            // If no barrier is hit, just send the updated game
            gameService.updateGame(gameId);
        }
    }
}
