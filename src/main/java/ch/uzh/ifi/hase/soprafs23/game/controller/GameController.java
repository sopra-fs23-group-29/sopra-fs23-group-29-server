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
import ch.uzh.ifi.hase.soprafs23.game.service.WebSocketService;
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
    private final WebSocketService webSocketService;

    GameController(
        UserService userService,
        GameService gameService,
        PlayerService playerService,
        WebSocketService webSocketService
    ) {
        this.userService = userService;
        this.playerService = playerService;
        this.gameService = gameService;
        this.webSocketService = webSocketService;
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

        // If that user is assigned to a player who is currently in a game, throw CONFLICT
        // A user cannot have multiple players that are in games
        // In theory, there should not be a player without a gameId
        Player playerToJoin = playerService.getPlayerByUserToken(auth_token);
        if (playerToJoin != null) {
            // given the player to the auth_token exists, if he has a gameId, then throw an error
            if (playerToJoin.getGameId() != null) {
                log.error("userToken {} with Player {} is already in game {}, cannot create a new game",
                    auth_token, playerToJoin.getId(), playerToJoin.getGameId());
                throw new ResponseStatusException(HttpStatus.CONFLICT, "cannot crate game, user is in a game already, leave game first");
            } else {
                log.warn("userToken {} with Player {} has a player without gameId!",
                    auth_token, playerToJoin.getId());
            }
        }

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
    public PlayerGetDTO joinGame(
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

        // If that user is assigned to a player who is currently in a game, throw CONFLICT
        // A user cannot have multiple players that are in games
        // In theory, there should not be a player without a gameId
        Player playerToJoin = playerService.getPlayerByUserToken(auth_token);
        if (playerToJoin != null) {
            // given the player to the auth_token exists, if he has a gameId, then throw an error
            if (playerToJoin.getGameId() != null) {
                log.error("userToken {} with Player {} is already in game {}, cannot create a new game",
                    auth_token, playerToJoin.getId(), playerToJoin.getGameId());
                throw new ResponseStatusException(HttpStatus.CONFLICT, "cannot crate game, user is in a game already, leave game first");
            } else {
                log.warn("userToken {} with Player {} has a player without gameId!",
                    auth_token, playerToJoin.getId());
            }
        }

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
        // playerLeaving is null if there is no player from the auth_token
        // in that case, throw NOT_FOUND because the player that wants to leave does not exist
        Player playerLeaving = playerService.getPlayerByUserToken(auth_token);
        if (playerLeaving == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                "User %s leaving does not have a Player entry".formatted(userLeaving.getUsername()));
        }

        // If the playerLeaving exists, is he currently in the gameId provided, otherwise throw BAD_REQUEST
        if (playerLeaving.getGameId() != gameId) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "User %s leaving is currently in game %s, cannot leave game %s".formatted(
                    userLeaving.getUsername(), playerLeaving.getGameId(), gameId
                ));
        }

        log.info("Game {}: User {} Player {} leaving", gameId, userLeaving.getUsername(), playerLeaving.getId());

        // If the user was the host and if the game is INLOBBY, delete the game, this deletes all players
        // Send an update to everybody to gamedeleted
        if (playerLeaving.getIsHost() && gameToLeave.getGameStatus().equals(GameStatus.INLOBBY)) {
            log.info("Game {}: Host left in LOBBY, delete game", gameId);
            gameService.deleteGame((long) gameId);
            webSocketService.sendMessageToClients("/topic/games/" + gameId + "/gamedeleted", "Game %s deleted".formatted(gameId));
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
