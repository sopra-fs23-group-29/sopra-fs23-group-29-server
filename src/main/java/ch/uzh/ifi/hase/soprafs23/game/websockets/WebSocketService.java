package ch.uzh.ifi.hase.soprafs23.game.websockets;

import ch.uzh.ifi.hase.soprafs23.game.repository.PlayerRepository;
import ch.uzh.ifi.hase.soprafs23.game.service.PlayerService;
import ch.uzh.ifi.hase.soprafs23.game.service.UserService;
import ch.uzh.ifi.hase.soprafs23.game.websockets.dto.outgoing.UserListDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class WebSocketService {

    protected final PlayerRepository playerRepo;
    private final PlayerService playerService;
    private final UserService userService;

    @Autowired // Automatic injection of beans
    protected SimpMessagingTemplate smesg;
    Logger log = LoggerFactory.getLogger(WebSocketService.class);

    public WebSocketService(@Qualifier("PlayerRepository") PlayerRepository playerRepository,
                            @Lazy PlayerService playerService, @Lazy UserService userService) {
        this.playerRepo = playerRepository;
        this.playerService = playerService;
        this.userService = userService;
    }

    public void sendMessageToClients(String destination, Object dto) {
        this.smesg.convertAndSend(destination, dto);

    }

    public String viewUsers() {
        return userService.getUserListAsString();
    }

    public String viewUser(long userId) {
        return userService.getUserById(userId).toString();
    }
}
