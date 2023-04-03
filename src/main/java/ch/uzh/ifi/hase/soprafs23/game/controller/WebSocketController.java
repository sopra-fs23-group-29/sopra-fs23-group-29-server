package ch.uzh.ifi.hase.soprafs23.game.controller;

import ch.uzh.ifi.hase.soprafs23.game.entity.Player;
import ch.uzh.ifi.hase.soprafs23.game.repository.PlayerRepository;
import ch.uzh.ifi.hase.soprafs23.game.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs23.game.service.GameService;
import ch.uzh.ifi.hase.soprafs23.game.service.PlayerService;
import ch.uzh.ifi.hase.soprafs23.game.service.UserService;
import ch.uzh.ifi.hase.soprafs23.game.service.WebSocketService;
import ch.uzh.ifi.hase.soprafs23.game.websockets.dto.incoming.GameCreationDTO;
import ch.uzh.ifi.hase.soprafs23.game.websockets.dto.incoming.PlayerJoinDTO;
import ch.uzh.ifi.hase.soprafs23.game.websockets.dto.outgoing.GameIdDTO;
import ch.uzh.ifi.hase.soprafs23.game.websockets.dto.outgoing.PlayerDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import java.util.Optional;

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

    /**
     * Crate a new game
     * @param gameCreationDTO The message from the client with the information of the game to be created
     */
    @MessageMapping("/games")
    public void createGame(GameCreationDTO gameCreationDTO) {
        log.info("Game {}: game created", gameCreationDTO.getGameName());
        // GameService creates the game and writes to the gameRepository
        GameIdDTO newGameIdDTO = new GameIdDTO();
        newGameIdDTO.setGameId(gameService.createNewGame(
            gameCreationDTO.getGameName(),
            gameCreationDTO.getGameMode()
        ));
        this.webSocketService.sendMessageToClients("/games/", newGameIdDTO);
    }

    /**
     * Add a player to the game
     * @param gameId The id of the game to join
     */
    @MessageMapping("/games/{gameId}")
    public void joinGame(@DestinationVariable Long gameId, PlayerJoinDTO playerJoinDTO) {
        log.info("Game {} being joined by {}", gameId, playerJoinDTO.getPlayerName());

        Player playerJoining;

        // Check if the player with the userToken exists in userRepository, if not create him
        if (userRepository.findByToken(playerJoinDTO.getUserToken()) == null) {
            log.error("The player does not have a userToken, nothing is done");
            return;
        }

        // Check if the player already exists in playerRepository
        playerJoining = playerRepository.findByToken(playerJoinDTO.getUserToken());
        if (playerJoining != null) {
            // if yes, just set his gameId
            playerJoining.setGameId(gameId);
            playerRepository.save(playerJoining);
            playerRepository.flush();
        } else {
            // if no, create a new player through userService and add
            playerJoining = userService.addUserToGame(gameId, playerJoinDTO.getUserToken());
        }

        PlayerDTO playerJoiningDTO = new PlayerDTO();
        playerJoiningDTO.setId(playerJoining.getId());
        playerJoiningDTO.setToken(playerJoining.getToken());
        playerJoiningDTO.setPlayerName(playerJoining.getPlayername());
        playerJoiningDTO.setUserToken(playerJoining.getUserToken());
        playerJoiningDTO.setGameId(playerJoining.getGameId());

        this.webSocketService.sendMessageToClients("/games/" + gameId, playerJoiningDTO);
    }

    // viewing the user list
    @MessageMapping("/users")
    public void showUsers() {
        String userList = webSocketService.viewUsers();
        this.webSocketService.sendMessageToClients("/users", userList);
    }

    // viewing a single user
    @MessageMapping("/users/{userId}")
    public void showUser(@DestinationVariable long userId) {
        String user = webSocketService.viewUser(userId);
        this.webSocketService.sendMessageToClients("/users/" + userId, user);
    }

}
