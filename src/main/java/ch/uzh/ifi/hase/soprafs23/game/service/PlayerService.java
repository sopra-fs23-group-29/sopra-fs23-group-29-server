package ch.uzh.ifi.hase.soprafs23.game.service;

import ch.uzh.ifi.hase.soprafs23.game.entity.Player;
import ch.uzh.ifi.hase.soprafs23.game.repository.PlayerRepository;
import ch.uzh.ifi.hase.soprafs23.game.websockets.dto.outgoing.PlayerJoinedDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional
public class PlayerService {

  private final Logger log = LoggerFactory.getLogger(UserService.class);

  private final PlayerRepository playerRepository;
  private final WebSocketService webSocketService;

  @Autowired
  public PlayerService(
          @Qualifier("playerRepository") PlayerRepository playerRepository,
          WebSocketService webSocketService) {
    this.playerRepository = playerRepository;
    this.webSocketService = webSocketService;
  }


  public List<Player> getPlayers() {return this.playerRepository.findAll();}

  public Player getPlayerByUserToken(String userToken) {
    Player playerSearched = this.playerRepository.findByUserToken(userToken);

    if (playerSearched == null) {
      throw new ResponseStatusException(
              HttpStatus.NOT_FOUND,
              String.format("Player with userToken %s not found", userToken)
      );
    }

    return playerSearched;
  }

  public boolean checkIfPlayerExistsByUserToken(String userToken) {
    Player playerSearched = playerRepository.findByUserToken(userToken);
    return playerSearched != null;
  }

  public void greetPlayers(Player player) {
    PlayerJoinedDTO playerJoinDTO = new PlayerJoinedDTO();
    playerJoinDTO.setPlayerName(player.getPlayerName());
    this.webSocketService.sendMessageToClients("/topic/games/" + player.getGameId(), playerJoinDTO);
  }

  public void savePlayer(Player playerToSave) {
    playerRepository.save(playerToSave);
    playerRepository.flush();
  }

}
