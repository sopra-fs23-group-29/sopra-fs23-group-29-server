package ch.uzh.ifi.hase.soprafs23.game.controller;

import ch.uzh.ifi.hase.soprafs23.game.service.WebSocketService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import java.util.concurrent.TimeUnit;

@Controller
public class WebSocketController {
    private final WebSocketService webSocketService;
    Logger log = LoggerFactory.getLogger(WebSocketController.class);

    public WebSocketController(WebSocketService webSocketService) {
        this.webSocketService = webSocketService;
    }

    private void sendConstantStream(String destination) {
        while (true) {
            this.webSocketService.sendMessageToClients(destination, "server is running");
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
    /*
    @MessageMapping("/lobbies/{lobbyId}/end-round")
    public void endRound(@DestinationVariable Long lobbyId) {
        log.info("Lobby {}: round is over", lobbyId);
        // GameService rüeft evaluator uf
        LeaderboardDTO leaderboard = gameService.endRound(lobbyId);
        this.webSocketService.sendMessageToClients(destination + lobbyId, leaderboard);
    }
     */

    // viewing the user list
    @MessageMapping("/users")
    public void showUsers() {
        String destination = "/topic/users";
        while(true) {
            this.webSocketService.sendMessageToClients(destination, "hello user");
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
        String user = webSocketService.viewUser(userId);
        this.webSocketService.sendMessageToClients("/users/" + userId, user);
    }

}
