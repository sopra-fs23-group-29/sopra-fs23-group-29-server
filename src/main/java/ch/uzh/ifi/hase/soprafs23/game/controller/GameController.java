package ch.uzh.ifi.hase.soprafs23.game.controller;

import ch.uzh.ifi.hase.soprafs23.constant.GameStatus;
import ch.uzh.ifi.hase.soprafs23.game.entity.Game;
import ch.uzh.ifi.hase.soprafs23.game.entity.Player;
import ch.uzh.ifi.hase.soprafs23.game.entity.Turn;
import ch.uzh.ifi.hase.soprafs23.game.entity.User;
import ch.uzh.ifi.hase.soprafs23.game.repository.GameRepository;
import ch.uzh.ifi.hase.soprafs23.game.rest.dto.*;
import ch.uzh.ifi.hase.soprafs23.game.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs23.game.service.GameService;
import ch.uzh.ifi.hase.soprafs23.game.service.PlayerService;
import ch.uzh.ifi.hase.soprafs23.game.service.UserService;
import ch.uzh.ifi.hase.soprafs23.game.service.WebSocketService;
import ch.uzh.ifi.hase.soprafs23.game.websockets.dto.outgoing.GameUpdateDTO;
import ch.uzh.ifi.hase.soprafs23.game.websockets.dto.outgoing.TurnOutgoingDTO;
import com.google.gson.Gson;
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
                throw new ResponseStatusException(HttpStatus.CONFLICT, "cannot crate game, user is in a game %s already, leave game first".formatted(playerToJoin.getGameId()));
            } else {
                log.warn("userToken {} with Player {} has a player without gameId!",
                    auth_token, playerToJoin.getId());
            }
        }

        log.info("Game {}: create game ...", gamePostDTO.getGameName());

        // GameService creates the game and writes to the gameRepository
        // get the gameId
        Long newGameId = gameService.createNewGame(
            gamePostDTO.getGameName(),
            gamePostDTO.getGameMode(),
            gamePostDTO.getBoardSize(),
            gamePostDTO.getMaxDuration()
        );

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
                throw new ResponseStatusException(HttpStatus.CONFLICT, "cannot join game, user is in a game %s already, leave game first".formatted(playerToJoin.getGameId()));
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

    @DeleteMapping("/games/{gameIdInt}")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public void leaveGame(
        @PathVariable int gameIdInt,
        HttpServletRequest request
    ) throws ResponseStatusException {
        // fetch auth_token from request
        String auth_token = request.getHeader("Authorization");

        // gameId in long format
        long gameId = gameIdInt;

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
        Game gameToLeave = gameService.getGameById(gameId);

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


        // If the player is the last player in the game close the game down
        if (gameToLeave.getPlayersView().size() == 1) {
            log.info("Game {}: last player leaving, close game down", gameId);
            gameService.deleteGame(gameId);
            webSocketService.sendMessageToClients("/topic/games/" + gameId + "/gamedeleted", "Game %s deleted".formatted(gameId));
        }

        // If the user was the host and if the game is INLOBBY, delete the game, this deletes all players
        // Send an update to everybody to gamedeleted
         else if (playerLeaving.getIsHost() && gameToLeave.getGameStatus().equals(GameStatus.INLOBBY)) {
            log.info("Game {}: Host left in LOBBY, delete game", gameId);
            gameService.deleteGame(gameId);
            webSocketService.sendMessageToClients("/topic/games/" + gameId + "/gamedeleted", "Game %s deleted".formatted(gameId));
        }

        // Option 1) Just end the game right away as soon as a player leaves
        /*
        // If the game is INPROGRESS force gameover
        else if (gameToLeave.getGameStatus().equals(GameStatus.INPROGRESS)) {
            log.info("Game {} in progress, end game", gameId);
            // Otherwise, just remove that player that left
            // Tell the player repo that a player left
            // NOT_FOUND if gameId does not exist
            playerService.deletePlayerById(playerLeaving.getId());
            // in this case, let the game know that one player left
            gameService.updatePlayers(gameId);

            // Get game and set status to FINISHED, leading to game being over
            Game gameToEnd = gameService.getGameById(gameId);
            gameToEnd.setGameStatus(GameStatus.FINISHED);
            GameUpdateDTO gameToEndDTO = new GameUpdateDTO(gameToEnd);
            String gameToEndAsString = new Gson().toJson(gameToEndDTO);
            webSocketService.sendMessageToClients("/topic/games/" + gameId + "/gameover", gameToEndAsString);
        }
        */

        // Option 2) Let game INPROGRESS continue
        // If the game is INPROGRESS force the next turn in the game
        else if (gameToLeave.getGameStatus().equals(GameStatus.INPROGRESS)) {
            log.info("Game {} in progress, force next turn", gameId);
            // Otherwise, just remove that player that left
            // Tell the player repo that a player left
            // NOT_FOUND if gameId does not exist
            playerService.deletePlayerById(playerLeaving.getId());
            // in this case, let the game know that one player left
            gameService.updatePlayers(gameId);


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

        // default fallback
        else {
            // Otherwise, just remove that player that left
            // Tell the player repo that a player left
            // NOT_FOUND if gameId does not exist
            playerService.deletePlayerById(playerLeaving.getId());
            // in this case, let the game know that one player left
            gameService.updatePlayers(gameId);
            gameService.updateGame(gameId);
        }


        // let everybody know that someone left or maybe the game is deleted
        gameService.greetGames();

    }

    @DeleteMapping("/games")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public GameGetDTO leaveAllGames(
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

        // Get the player from the auth_token
        // playerLeaving is null if there is no player from the auth_token
        // in that case, the user leaving all games is in no game, throw CONFLICT
        Player playerLeaving = playerService.getPlayerByUserToken(auth_token);
        if (playerLeaving == null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
              "The user %s is in no games".formatted(userLeaving.getUsername()));
        }

        // fetch the game the player is leaving and create a response object
        Long gameIdToLeave = playerLeaving.getGameId();
        Game gameToLeave = gameService.getGameById(gameIdToLeave);
        GameGetDTO gameLeftDTO = DTOMapper.INSTANCE.convertEntityToGameGetDTO(gameToLeave);

        // If the playerLeaving exists, is he currently in a game
        // leave that game
        log.info("User {} Player {} leaving Game {}", userLeaving.getUsername(), playerLeaving.getId(), gameIdToLeave);

        // If the player is the last player in the game close the game down
        if (gameToLeave.getPlayersView().size() == 1) {
            log.info("Game {}: last player leaving, close game down", gameIdToLeave);
            gameService.deleteGame(gameIdToLeave);
            webSocketService.sendMessageToClients("/topic/games/" + gameIdToLeave + "/gamedeleted", "Game %s deleted".formatted(gameIdToLeave));
        }

        // If the user was the host and if the game is INLOBBY, delete the game, this deletes all players
        // Send an update to everybody to gamedeleted
        else if (playerLeaving.getIsHost() && gameToLeave.getGameStatus().equals(GameStatus.INLOBBY)) {
            log.info("Game {}: Host left in LOBBY, delete game", gameIdToLeave);
            gameService.deleteGame(gameIdToLeave);
            webSocketService.sendMessageToClients("/topic/games/" + gameIdToLeave + "/gamedeleted", "Game %s deleted".formatted(gameIdToLeave));
        }

        // Option 1) Just end the game right away as soon as a player leaves
        /*
        // If the game is INPROGRESS force gameover
        else if (gameToLeave.getGameStatus().equals(GameStatus.INPROGRESS)) {
            log.info("Game {} in progress, end game", gameId);
            // Otherwise, just remove that player that left
            // Tell the player repo that a player left
            // NOT_FOUND if gameId does not exist
            playerService.deletePlayerById(playerLeaving.getId());
            // in this case, let the game know that one player left
            gameService.updatePlayers(gameId);

            // Get game and set status to FINISHED, leading to game being over
            Game gameToEnd = gameService.getGameById(gameId);
            gameToEnd.setGameStatus(GameStatus.FINISHED);
            GameUpdateDTO gameToEndDTO = new GameUpdateDTO(gameToEnd);
            String gameToEndAsString = new Gson().toJson(gameToEndDTO);
            webSocketService.sendMessageToClients("/topic/games/" + gameId + "/gameover", gameToEndAsString);
        }
        */

        // Option 2) Let game INPROGRESS continue
        // If the game is INPROGRESS force the next turn in the game
        else if (gameToLeave.getGameStatus().equals(GameStatus.INPROGRESS)) {
            log.info("Game {} in progress, force next turn", gameIdToLeave);
            // Otherwise, just remove that player that left
            // Tell the player repo that a player left
            // NOT_FOUND if gameId does not exist
            playerService.deletePlayerById(playerLeaving.getId());
            // in this case, let the game know that one player left
            gameService.updatePlayers(gameIdToLeave);


            log.info("Game {} next turn", gameIdToLeave);
            gameService.startNextTurn(gameIdToLeave);

            // check if the game is over, if so, just send the game object to the gameover topic
            Game gameNextTurn = gameService.getGameById(gameIdToLeave);
            log.info("Game {} current leaderboard:", gameIdToLeave);
            log.info("{}", gameNextTurn.getLeaderboard());
            if (gameNextTurn.gameOver()) {
                log.info("Game {} is over", gameIdToLeave);
                GameUpdateDTO gameOver = new GameUpdateDTO(gameNextTurn);
                String gameOverAsString = new Gson().toJson(gameOver);
                // send the game over to all subscribers
                webSocketService.sendMessageToClients("/topic/games/" + gameIdToLeave + "/gameover", gameOverAsString);
                return gameLeftDTO;
            }

            Turn nextTurn = gameService.getGameById(gameIdToLeave).getTurn();
            log.info("Created Turn {}", nextTurn.getTurnNumber());

            TurnOutgoingDTO nextTurnDTO = new TurnOutgoingDTO(nextTurn);
            String nextTurnDTOasString = new Gson().toJson(nextTurnDTO);

            // send the new Turn to all subscribers
            webSocketService.sendMessageToClients("/topic/games/" + gameIdToLeave + "/newturn", nextTurnDTOasString);
            // inform the GameHeader client separately
            webSocketService.sendMessageToClients("/topic/games/" + gameIdToLeave + "/newturn_gameheader", nextTurnDTOasString);

        }

        // default fallback
        else {
            // Otherwise, just remove that player that left
            // Tell the player repo that a player left
            // NOT_FOUND if gameId does not exist
            playerService.deletePlayerById(playerLeaving.getId());
            // in this case, let the game know that one player left
            gameService.updatePlayers(gameIdToLeave);
            gameService.updateGame(gameIdToLeave);
        }


        // let everybody know that someone left or maybe the game is deleted
        gameService.greetGames();

        // return the GameGetDTO with the gameId of the game the user left
        return gameLeftDTO;

    }

}
