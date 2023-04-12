package ch.uzh.ifi.hase.soprafs23.game.controller;

import ch.uzh.ifi.hase.soprafs23.game.entity.Game;
import ch.uzh.ifi.hase.soprafs23.game.entity.Player;
import ch.uzh.ifi.hase.soprafs23.game.entity.User;
import ch.uzh.ifi.hase.soprafs23.game.repository.GameRepository;
import ch.uzh.ifi.hase.soprafs23.game.rest.dto.*;
import ch.uzh.ifi.hase.soprafs23.game.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs23.game.service.GameService;
import ch.uzh.ifi.hase.soprafs23.game.service.PlayerService;
import ch.uzh.ifi.hase.soprafs23.game.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;

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

    GameController(
        UserService userService,
        GameService gameService,
        PlayerService playerService
    ) {
        this.userService = userService;
        this.playerService = playerService;
        this.gameService = gameService;
    }

    @PostMapping("/games")
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public GameGetDTO createGame (
            @RequestBody GamePostDTO gamePostDTO,
            HttpServletRequest request
    ) throws ResponseStatusException {

        // fetch auth_token from request
        String auth_token = request.getHeader("Authorization");

        // check if token was provided
        if (auth_token == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "A token is required");
        }

        // check the auth_token, the ID must match the token! idToCheck is null, it just needs to be a valid userToken
        // if the token does not apply to a valid user, throw UNAUTHORIZED
        userService.checkToken(auth_token, null);

        log.info("Game {}: create game ...", gamePostDTO.getGameName());

        // GameService creates the game and writes to the gameRepository
        // get the gameId
        Long newGameId = gameService.createNewGame(gamePostDTO.getGameName(), gamePostDTO.getGameMode());

        // fetch the game created
        Game newGame = GameRepository.findByGameId(newGameId);

        log.info("Game {}: game created", newGame.getGameName());

        // Add the creator of the game as a player
        Player playerJoined = playerService.joinPlayer(auth_token, newGameId.intValue());

        // let everybody know about the new game
        gameService.updatePlayers(newGameId);
        gameService.greetGames();

        return DTOMapper.INSTANCE.convertEntityToGameGetDTO(newGame);

    }

    @PostMapping("/games/{gameId}")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public PlayerGetDTO createPlayer(
        @PathVariable int gameId,
        HttpServletRequest request
        ) throws ResponseStatusException {

        // fetch auth_token from request
        String auth_token = request.getHeader("Authorization");

        // check if token was provided
        if (auth_token == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "A token is required");
        }

        // check the auth_token, the ID must match the token! idToCheck is null, it just needs to be a valid userToken
        // if the token does not apply to a valid user, throw UNAUTHORIZED
        userService.checkToken(auth_token, null);

        // check if the gameId exists, otherwise throw NOT_FOUND via GameRepository
        gameService.getGameById((long) gameId);

        // Get the user from the auth_token
        // HTTPError is thrown if userToken is not an existing user
        User userJoining = userService.getUserByToken(auth_token);

        log.info("Game {}: User {} joining", gameId, userJoining.getUsername());

        // Join/create the user
        // throw UNAUTHORIZED if the game cannot be joined, the player will not be created
        Player playerJoining = playerService.joinPlayer(auth_token, gameId);

        PlayerGetDTO playerGetDTO = DTOMapper.INSTANCE.convertEntityToPlayerGetDTO(playerJoining);

        playerGetDTO.setId(playerJoining.getId());
        playerGetDTO.setPlayerName(playerJoining.getPlayerName());
        playerGetDTO.setToken(playerJoining.getToken());
        playerGetDTO.setUserToken(playerJoining.getUserToken());
        playerGetDTO.setPlayerColor(playerJoining.getPlayerColor());
        playerGetDTO.setGameId(playerJoining.getGameId());

        // let all players in the game know who joined
        gameService.updatePlayers((long) gameId);
        gameService.updateGame((long) gameId);

        return playerGetDTO;

    }

}
