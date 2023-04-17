package ch.uzh.ifi.hase.soprafs23.game.controller;

import ch.uzh.ifi.hase.soprafs23.constant.GameStatus;
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

        // Add the creator of the game as a player
        Player playerJoined = playerService.joinPlayer(auth_token, newGameId.intValue());

        // Set host of the creator to true
        playerJoined.setIsHost(true);

        // let everybody know about the new game
        gameService.updatePlayers(newGameId);
        gameService.greetGames();

        // fetch the game created
        Game newGame = GameRepository.findByGameId(newGameId);
        log.info("Game {}: game created", newGame.getGameName());

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

        gameService.updatePlayers((long) gameId);
        // let all games know that someone joined
        gameService.greetGames();
        // let the subscribers to that specific game know someone joined
        gameService.updateGame((long) gameId);

        return playerGetDTO;

    }

    @DeleteMapping("/games/{gameId}")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public void leaveGame(
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

        // Get the user from the auth_token
        // HTTPError is thrown if userToken is not an existing user
        User userLeaving = userService.getUserByToken(auth_token);

        // Get the game to leave, otherwise throw NOT_FOUND via GameRepository
        Game gameToLeave = gameService.getGameById((long) gameId);

        // Get the player from the auth_token
        // NOT_FOUND if does not exist
        Player playerLeaving = playerService.getPlayerByUserToken(auth_token);

        log.info("Game {}: User {} leaving", gameId, userLeaving.getUsername());

        // If the user was the host and if the game is INLOBBY, delete the game, this deletes all players
        if (playerLeaving.getIsHost() && gameToLeave.getGameStatus().equals(GameStatus.INLOBBY)) {
            log.info("Game {}: Host left in LOBBY, delete game", gameId);
            gameService.deleteGame((long) gameId);
        } else {
            // Otherwise, just remove that player that left
            // Tell the player repo that a player left
            // NOT_FOUND if gameId does not exist
            playerService.deletePlayerById(playerLeaving.getId());
            // in this case, let the game know that one player left
            gameService.updatePlayers((long) gameId);
            gameService.updateGame((long) gameId);
        }

        // let everybody know that someone left or maybe the game is deleted
        gameService.greetGames();

    }

}
