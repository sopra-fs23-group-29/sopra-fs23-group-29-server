package ch.uzh.ifi.hase.soprafs23.game.controller;

import ch.uzh.ifi.hase.soprafs23.game.entity.Player;
import ch.uzh.ifi.hase.soprafs23.game.entity.User;
import ch.uzh.ifi.hase.soprafs23.game.rest.dto.*;
import ch.uzh.ifi.hase.soprafs23.game.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs23.game.service.GameService;
import ch.uzh.ifi.hase.soprafs23.game.service.PlayerService;
import ch.uzh.ifi.hase.soprafs23.game.service.UserService;
import ch.uzh.ifi.hase.soprafs23.game.service.WebSocketService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * Game Controller
 * This class is responsible for handling all REST request that are related to
 * the game creation and joining of players.
 * The controller will receive the request and delegate the execution to the
 * Services and finally return the result.
 */
@RestController
public class GameController {

    Logger log = LoggerFactory.getLogger(WebSocketController.class);

    private final GameService gameService;
    private final UserService userService;
    private final PlayerService playerService;
    private final WebSocketService webSocketService;

    GameController(
        UserService userService,
        GameService gameService,
        WebSocketService webSocketService,
        PlayerService playerService
    ) {
        this.userService = userService;
        this.playerService = playerService;
        this.gameService = gameService;
        this.webSocketService = webSocketService;
    }

    @PostMapping("/games")
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public GameGetDTO createGame(@RequestBody GamePostDTO gamePostDTO) {
        log.info("Game {}: create game ...", gamePostDTO.getGameName());

        // GameService creates the game and writes to the gameRepository
        // get the gameId
        int newGameId = gameService.createNewGame(gamePostDTO.getGameName(), gamePostDTO.getGameMode());
        // return object
        GameGetDTO newGameGetDTO = new GameGetDTO();
        newGameGetDTO.setGameId(newGameId);

        // let everybody know about the new game
        gameService.greetGames(gameService.getGameById(newGameId));

        log.info("Game {}: game created", gamePostDTO.getGameName());
        return newGameGetDTO;
    }

    @PostMapping("/games/{gameId}")
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public PlayerGetDTO createPlayer(
        @PathVariable Long gameId,
        @RequestHeader("Authorization") String auth_token,
        HttpServletResponse response
    ) {

        // Get the user from the auth_token
        // HTTPError is thrown if userToken is not an existing user
        User userJoining = userService.getUserByToken(auth_token);

        log.info("Game {}: User {} joining", gameId, userJoining.getUsername());

        // Create an empty Player who is the player currently joining
        Player playerJoining;

        // Check if the player already exists in playerRepository
        if (playerService.checkIfPlayerExistsByUserToken(auth_token)) {
            // if yes, just set his gameId
            log.info("Player already exists, set gameId");
            playerJoining = playerService.getPlayerByUserToken(auth_token);
            playerJoining.setGameId(gameId);
            playerService.savePlayer(playerJoining);
        } else {
            // if no, create a new player through userService and add
            log.info("Player is created ...");
            playerJoining = userService.addUserToGame(gameId, auth_token);
        }

        // let all players in the game know who joined
        playerService.greetPlayers(playerJoining);

        // todo: Convert playerJoining into a DTO entity with the mapper
        // return it

        PlayerGetDTO dummy = new PlayerGetDTO();
        return dummy;
    }

}
