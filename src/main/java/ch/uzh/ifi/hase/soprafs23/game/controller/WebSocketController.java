package ch.uzh.ifi.hase.soprafs23.game.controller;

import ch.uzh.ifi.hase.soprafs23.game.entity.Game;
import ch.uzh.ifi.hase.soprafs23.game.entity.Leaderboard;
import ch.uzh.ifi.hase.soprafs23.game.entity.Turn;
import ch.uzh.ifi.hase.soprafs23.game.questions.IQuestionService;
import ch.uzh.ifi.hase.soprafs23.game.questions.restCountry.BarrierQuestion;
import ch.uzh.ifi.hase.soprafs23.game.repository.GameRepository;
import ch.uzh.ifi.hase.soprafs23.game.repository.PlayerRepository;
import ch.uzh.ifi.hase.soprafs23.game.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs23.game.service.*;
import ch.uzh.ifi.hase.soprafs23.game.websockets.dto.incoming.Answer;
import ch.uzh.ifi.hase.soprafs23.game.websockets.dto.incoming.BarrierAnswer;
import ch.uzh.ifi.hase.soprafs23.game.websockets.dto.incoming.DummyIncomingDTO;
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


//    // receive a message in the backend
//    @MessageMapping("/users")
//    public void receiveMessage(DummyIncomingDTO dummyIncomingDTO) {
//        System.out.println("Received dummyIncomingDTO");
//        log.info("Received dummyIncomingDTO");
//        System.out.println("Dummy message: " + dummyIncomingDTO.getMessage());
//        log.info("Dummy message: " + dummyIncomingDTO.getMessage());
//    }

    // viewing a single user
    @MessageMapping("/users/{userId}")
    public void showUser(@DestinationVariable long userId) {
        String userString = userService.getUserById(userId).toString();
        this.webSocketService.sendMessageToClients("/users/" + userId, userString);
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
        webSocketService.sendMessageToClients("/games/" + gameId, nextTurnDTOasString);

        // Debugging, send message to /users as well
        log.info("Debugging sending startGame to /topic/users ...");
        webSocketService.sendMessageToClients("/topic/users", nextTurnDTOasString);
    }


    /**
     * Start a new turn in a game
     * The game must be in progress already, otherwise throw BAD_REQUEST
     * Returns a Turn object for the client to work with
     */
    @MessageMapping("/games/{gameId}/nextTurn")
    public void nextTurn(@DestinationVariable long gameId) {
        log.info("Game {} next turn", gameId);
        gameService.startNextTurn(gameId);
        Turn nextTurn = gameService.getGameById(gameId).getTurn();
        log.info("Created Turn {}", nextTurn.getTurnNumber());

        TurnOutgoingDTO nextTurnDTO = new TurnOutgoingDTO(nextTurn);

        String nextTurnDTOasString = new Gson().toJson(nextTurnDTO);

        // send the new Turn to all subscribers
        webSocketService.sendMessageToClients("/games/" + gameId, nextTurnDTOasString);

        // Debugging, send message to /users as well
        log.info("Debugging sending startGame to /topic/users ...");
        webSocketService.sendMessageToClients("/topic/users", nextTurnDTOasString);
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
        log.info("Update Game {} Turn {} with rank answer from Player {}", gameId, turnNumber, playerId);
        Turn updatedTurn = gameService.processAnswer(answer, playerId, turnNumber, gameId);
        TurnOutgoingDTO updatedTurnDTO = new TurnOutgoingDTO(updatedTurn);

        String turnOutgoingDTOasString = new Gson().toJson(updatedTurnDTO);

        // send the updated Turn to all subscribers
        webSocketService.sendMessageToClients("/games/" + gameId, turnOutgoingDTOasString);

        // Debugging, send message to /users as well
        log.info("Debugging sending saveAnswer to /topic/users ...");
        webSocketService.sendMessageToClients("/topic/users", turnOutgoingDTOasString);

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
        log.info("Game {} end current Turn", gameId);
        Leaderboard turnResults = gameService.endTurn(gameId, turnNumber);
        // Make a DTO
        LeaderboardDTO turnResultsDTO = new LeaderboardDTO(turnResults);

        String leaderboardDTOasString = new Gson().toJson(turnResultsDTO);

        // send the updated Leaderboard to all subscribers
        webSocketService.sendMessageToClients("/games/" + gameId, leaderboardDTOasString);

        // Debugging, send message to /users as well
        log.info("Debugging sending endTurn to /topic/users ...");
        webSocketService.sendMessageToClients("/topic/users", leaderboardDTOasString);
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
        log.info("Game {} move players", gameId);
        // Ask the game service to move playerId in gameId by one field
        boolean barrierHit = gameService.movePlayerByOne(gameId, playerId);

        // If a barrier is hit, create a new barrier question, update the game with it and send it
        if (barrierHit) {
            BarrierQuestion barrierQuestion = questionService.generateBarrierQuestion();
            gameService.getGameById(gameId).setCurrentBarrierQuestion(barrierQuestion);
            String barrierQuestionAsString = new Gson().toJson(barrierQuestion);
            // send the barrierQuestion
            webSocketService.sendMessageToClients("/games/" + gameId, barrierQuestionAsString);

            // Debugging, send message to /users as well
            log.info("Debugging sending movePlayerByOne to /topic/users ...");
            webSocketService.sendMessageToClients("/topic/users", barrierQuestionAsString);
        } else {
            // If no barrier is hit, just send the updated game
            gameService.updateGame(gameId);
        }
    }
}
