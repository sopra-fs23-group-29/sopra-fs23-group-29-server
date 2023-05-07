package ch.uzh.ifi.hase.soprafs23.game.controller;

import ch.uzh.ifi.hase.soprafs23.game.service.GameService;
import ch.uzh.ifi.hase.soprafs23.game.service.UserService;
import ch.uzh.ifi.hase.soprafs23.game.service.WebSocketService;
import ch.uzh.ifi.hase.soprafs23.game.entity.Game;
import ch.uzh.ifi.hase.soprafs23.game.entity.Leaderboard;
import ch.uzh.ifi.hase.soprafs23.game.entity.Turn;
import ch.uzh.ifi.hase.soprafs23.game.websockets.dto.incoming.Answer;
import ch.uzh.ifi.hase.soprafs23.game.websockets.dto.incoming.BarrierAnswer;
import ch.uzh.ifi.hase.soprafs23.game.websockets.dto.incoming.MovePlayers;
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
    Logger log = LoggerFactory.getLogger(WebSocketController.class);

    public WebSocketController(
      WebSocketService webSocketService,
      GameService gameService,
      UserService userService
    ) {
        this.webSocketService = webSocketService;
        this.gameService = gameService;
        this.userService = userService;
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
     * Also send the newly created Game for the first time
     */
    @MessageMapping("/games/{gameId}/startGame")
    public void startGame(@DestinationVariable long gameId) throws InterruptedException {
        log.info("Start Game {}", gameId);
        gameService.startGame(gameId);
        log.info("Create Turn");
        gameService.startNextTurn(gameId);
        Game gameCreated = gameService.getGameById(gameId);
        Turn nextTurn = gameCreated.getTurn();
        log.info("Created Turn {}", nextTurn.getTurnNumber());

        GameUpdateDTO gameCreatedDTO = new GameUpdateDTO(gameCreated);
        String gameCreatedAsString = new Gson().toJson(gameCreatedDTO);
        TurnOutgoingDTO nextTurnDTO = new TurnOutgoingDTO(nextTurn);
        String nextTurnDTOasString = new Gson().toJson(nextTurnDTO);

        // send an update to all players in the lobby to change the route
        webSocketService.sendMessageToClients("/topic/games/" + gameId + "/gamestart", nextTurnDTOasString);
        Thread.sleep(3000); // artifical delay to make sure all participants of the lobby have rerouted before receiving the new newturn object

        // send the new game object upon start to let the frontend know about the game parameters
        log.info("Sending message to /newgame");
        webSocketService.sendMessageToClients("/topic/games/" + gameId + "/newgame", gameCreatedAsString);

        // send the new Turn to all subscribers of the running game
        webSocketService.sendMessageToClients("/topic/games/" + gameId + "/newturn", nextTurnDTOasString);

        // inform the GameHeader client separately
        log.info("Send message seperate to newturn_gameheader");
        webSocketService.sendMessageToClients("/topic/games/" + gameId + "/newturn_gameheader", nextTurnDTOasString);

        // also send to /games to remove games not joinable anymore
        gameService.greetGames();
    }

    /**
     * Get a message that a client is ready to remove the scoreboard and move the players
     * Process turn results if all are ready
     */
    @MessageMapping("/games/{gameId}/readyMovePlayers")
    public void readyMovePlayers(@DestinationVariable long gameId, MovePlayers movePlayers) throws InterruptedException {

        // check if the game is over, if so, just send the game object
        Game gameReadyToMove = gameService.getGameById(gameId);
        if (gameReadyToMove.gameOver()) {
            log.info("Game {} is over", gameId);
            GameUpdateDTO gameOver = new GameUpdateDTO(gameReadyToMove);
            String gameOverAsString = new Gson().toJson(gameOver);
            // send the game over to all subscribers
            webSocketService.sendMessageToClients("/topic/games/" + gameId + "/gameover", gameOverAsString);
            return;
        }

        log.info("Received ready to move players for Game {}", gameId);
        boolean moveOn = gameService.processMovePlayers(movePlayers, gameId);

        // If the game says we are ready to move on, send a message to /scoreboardOver, no content
        // Then we start to process the current turn results
        if (moveOn) {
            log.info("Move Game {} on ...", gameId);
            webSocketService.sendMessageToClients("/topic/games/" + gameId + "/scoreboardOver","");

            // try to process the turn results of the given game and start moving players
            // process the next result
            // True : We have processed all results from the current turn and can move on to the next turn
            // False : We keep on processing current turn results
            while (true) {

                // As soon as this function returns TRUE, we have processed all results
                if (gameService.processTurnResults(gameId)) {
                    break;
                }

                // Add delay to not bombard the system
                Thread.sleep(1000);

            }

            log.info("Game {} next turn", gameId);
            gameService.startNextTurn(gameId);

            // check if the game is over, if so, just send the game object to the gameover topic
            Game gameNextTurn = gameService.getGameById(gameId);
            log.info("Game {} current leaderboard:", gameId);
            log.info("{}", gameNextTurn.getLeaderboard());
            if (gameNextTurn.gameOver()) {
                log.info("Game {} is over", gameId);
                GameUpdateDTO gameOver = new GameUpdateDTO(gameNextTurn);
                String gameOverAsString = new Gson().toJson(gameOver);
                // send the game over to all subscribers
                webSocketService.sendMessageToClients("/topic/games/" + gameId + "/gameover", gameOverAsString);
                return;
            }

            Turn nextTurn = gameService.getGameById(gameId).getTurn();
            log.info("Created Turn {}", nextTurn.getTurnNumber());

            TurnOutgoingDTO nextTurnDTO = new TurnOutgoingDTO(nextTurn);
            String nextTurnDTOasString = new Gson().toJson(nextTurnDTO);

            // send the new Turn to all subscribers
            webSocketService.sendMessageToClients("/topic/games/" + gameId + "/newturn", nextTurnDTOasString);
            // inform the GameHeader client separately
            webSocketService.sendMessageToClients("/topic/games/" + gameId + "/newturn_gameheader", nextTurnDTOasString);
        }
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
            webSocketService.sendMessageToClients("/topic/games/" + gameId + "/gameover", gameOverAsString);
            return;
        }

        log.info("Update Game {} Turn {} with rank answer from Player {}", gameId, turnNumber, playerId);
        Turn updatedTurn = gameService.processAnswer(answer, playerId, turnNumber, gameId);

        // check if all players have made a guess this turn
        if (updatedTurn.allPlayersGuessed()) {
            // if yes, send a leaderboardDTO to /topic/games/gameId/scoreboard

            // check if the game is over, if so, just send the game object to the gameover topic
            gameNextTurn = gameService.getGameById(gameId);
            if (gameNextTurn.gameOver()) {
                log.info("Game {} is over", gameId);
                GameUpdateDTO gameOver = new GameUpdateDTO(gameNextTurn);
                String gameOverAsString = new Gson().toJson(gameOver);
                // send the game over to all subscribers
                webSocketService.sendMessageToClients("/topic/games/" + gameId + "/gameover", gameOverAsString);
                return;
            }

            log.info("Game {} end current Turn", gameId);
            Leaderboard turnResults = gameService.endTurn(gameId, turnNumber);
            Turn currentTurn = gameService.getGameById(gameId).getTurn();
            // Make a DTO
            LeaderboardDTO turnResultsDTO = new LeaderboardDTO(turnResults, currentTurn);
            String leaderboardDTOasString = new Gson().toJson(turnResultsDTO);

            // send the updated Leaderboard to all subscribers
            webSocketService.sendMessageToClients("/topic/games/" + gameId +"/scoreboard", leaderboardDTOasString);

        } else {
            // if no, send a turnOutgoingDTO to /topic/games/gameId/updatedturn
            TurnOutgoingDTO updatedTurnDTO = new TurnOutgoingDTO(updatedTurn);
            String turnOutgoingDTOasString = new Gson().toJson(updatedTurnDTO);

            // send the updated Turn to all subscribers
            webSocketService.sendMessageToClients("/topic/games/" + gameId + "/updatedturn", turnOutgoingDTOasString);
        }
    }

    /**
     * Resolve a barrier question answer from a player for a given barrier question in a given game
     * Returns an updated Game object, either the leaderboard is increased by one or not
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
            webSocketService.sendMessageToClients("/topic/games/" + gameId + "/gameover", gameOverAsString);
            return;
        }

        log.info("Update Game {} with barrier answer from Player {}", gameId, playerId);
        gameService.processBarrierAnswer(barrierAnswer, playerId, gameId);
        gameService.updateGame(gameId);
    }

}
