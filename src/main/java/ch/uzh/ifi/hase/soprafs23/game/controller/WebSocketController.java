package ch.uzh.ifi.hase.soprafs23.game.controller;

import ch.uzh.ifi.hase.soprafs23.game.entity.Turn;
import ch.uzh.ifi.hase.soprafs23.game.repository.PlayerRepository;
import ch.uzh.ifi.hase.soprafs23.game.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs23.game.service.GameService;
import ch.uzh.ifi.hase.soprafs23.game.service.PlayerService;
import ch.uzh.ifi.hase.soprafs23.game.service.UserService;
import ch.uzh.ifi.hase.soprafs23.game.service.WebSocketService;
import ch.uzh.ifi.hase.soprafs23.game.websockets.dto.incoming.Answer;
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
    private final PlayerService playerService;
    private final UserRepository userRepository;
    private final PlayerRepository playerRepository;
    Logger log = LoggerFactory.getLogger(WebSocketController.class);

    public WebSocketController(
        WebSocketService webSocketService,
        GameService gameService,
        UserService userService,
        PlayerService playerService,
        UserRepository userRepository,
        PlayerRepository playerRepository
    ) {
        this.webSocketService = webSocketService;
        this.gameService = gameService;
        this.userService = userService;
        this.playerService = playerService;
        this.userRepository = userRepository;
        this.playerRepository = playerRepository;
    }


    // viewing the user list
    @MessageMapping("/users")
    public void receiveMessage(DummyIncomingDTO dummyIncomingDTO) {
        System.out.println("Received dummyIncomingDTO");
        log.info("Received dummyIncomingDTO");
        System.out.println("Dummy message: " + dummyIncomingDTO.getMessage());
        log.info("Dummy message: " + dummyIncomingDTO.getMessage());
    }

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
        webSocketService.sendMessageToClients("/games" + gameId, nextTurnDTOasString);

        // Debugging, send message to /users as well
        log.info("Debugging sending startGame to /topic/users ...");
        webSocketService.sendMessageToClients("/topic/users", nextTurnDTOasString);
    }

    /**
     * Save an answer from a player for a given turn in a given game
     * Returns an updated Turn object for the client to work with
     */
    @MessageMapping("/games/{gameId}/turn/{turnNumber}/player/{playerId}/saveAnswer")
    public void saveAnswer(
            @DestinationVariable long gameId,
            @DestinationVariable int turnNumber,
            @DestinationVariable long playerId,
            Answer answer
    ) {
        log.info("Update Game {} Turn {} with answer from Player {}", gameId, turnNumber, playerId);
        TurnOutgoingDTO turnOutgoingDTO = gameService.processAnswer(answer, playerId, turnNumber, gameId);

        String turnOutgoingDTOasString = new Gson().toJson(turnOutgoingDTO);

        // send the updated Turn to all subscribers
        webSocketService.sendMessageToClients("/games" + gameId, turnOutgoingDTOasString);

        // Debugging, send message to /users as well
        log.info("Debugging sending saveAnswer to /topic/users ...");
        webSocketService.sendMessageToClients("/topic/users", turnOutgoingDTOasString);

    }

    /**
     * End a turn, send the new leaderboard with updated scores
     *
     * todo: Rather just send the delta? Or turnResults?
     */
    @MessageMapping("/games/{gameId}/endTurn")
    public void endTurn(
            @DestinationVariable long gameId
    ) {
        log.info("Game {} end current Turn", gameId);
        LeaderboardDTO leaderboardDTO = gameService.endTurn(gameId);

        String leaderboardDTOasString = new Gson().toJson(leaderboardDTO);

        // send the updated Leaderboard to all subscribers
        webSocketService.sendMessageToClients("/games" + gameId, leaderboardDTOasString);

        // Debugging, send message to /users as well
        log.info("Debugging sending endTurn to /topic/users ...");
        webSocketService.sendMessageToClients("/topic/users", leaderboardDTOasString);

    }
}
