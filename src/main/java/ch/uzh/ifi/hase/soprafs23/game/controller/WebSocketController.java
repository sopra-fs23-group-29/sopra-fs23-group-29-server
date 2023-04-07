package ch.uzh.ifi.hase.soprafs23.game.controller;

import ch.uzh.ifi.hase.soprafs23.game.entity.Player;
import ch.uzh.ifi.hase.soprafs23.game.repository.PlayerRepository;
import ch.uzh.ifi.hase.soprafs23.game.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs23.game.service.GameService;
import ch.uzh.ifi.hase.soprafs23.game.service.PlayerService;
import ch.uzh.ifi.hase.soprafs23.game.service.UserService;
import ch.uzh.ifi.hase.soprafs23.game.service.WebSocketService;
import ch.uzh.ifi.hase.soprafs23.game.websockets.dto.outgoing.DummyDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import java.util.concurrent.TimeUnit;

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
    public void showUsers() {
        String destination = "/topic/users";
        while(true) {
            this.webSocketService.sendMessageToClients(destination, new DummyDTO());
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        /*
        String userList = webSocketService.viewUsers();
        this.webSocketService.sendMessageToClients(destination, userList);
         */
    }

    // viewing a single user
    @MessageMapping("/users/{userId}")
    public void showUser(@DestinationVariable long userId) {
        String userString = userService.getUserById(userId).toString();
        this.webSocketService.sendMessageToClients("/users/" + userId, userString);
    }

}
