package ch.uzh.ifi.hase.soprafs23.game.service;

import ch.uzh.ifi.hase.soprafs23.constant.PlayerColor;
import ch.uzh.ifi.hase.soprafs23.game.entity.Game;
import ch.uzh.ifi.hase.soprafs23.game.entity.Player;
import ch.uzh.ifi.hase.soprafs23.game.entity.User;
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
import java.util.UUID;

@Service
@Transactional
public class PlayerService {

  private final Logger log = LoggerFactory.getLogger(UserService.class);

  private final PlayerRepository playerRepository;
  private final WebSocketService webSocketService;
  private final GameService gameService;
  private final UserService userService;

  @Autowired
  public PlayerService(
          @Qualifier("playerRepository") PlayerRepository playerRepository,
          WebSocketService webSocketService,
          GameService gameService,
          UserService userService) {
    this.playerRepository = playerRepository;
    this.webSocketService = webSocketService;
    this.gameService = gameService;
    this.userService = userService;
  }


  public List<Player> getPlayers() {return this.playerRepository.findAll();}

  private Player getPlayerByUserToken(String userToken) {
    Player playerSearched = this.playerRepository.findByUserToken(userToken);

    if (playerSearched == null) {
      throw new ResponseStatusException(
              HttpStatus.NOT_FOUND,
              String.format("Player with userToken %s not found", userToken)
      );
    }

    return playerSearched;
  }

  private boolean checkIfPlayerExistsByUserToken(String userToken) {
    Player playerSearched = playerRepository.findByUserToken(userToken);
    return playerSearched != null;
  }

  public void greetPlayers(Player player) {
    PlayerJoinedDTO playerJoinDTO = new PlayerJoinedDTO();
    playerJoinDTO.setPlayerName(player.getPlayerName());
    this.webSocketService.sendMessageToClients("/topic/games/" + player.getGameId(), playerJoinDTO);
  }

  public Player joinPlayer(String userTokenToJoin, int gameIdToJoin) {
    Player playerJoining;

    // Check if gameIdToJoin exists in GameRepository, throw NOT_FOUND otherwise
    Game gameToJoin = gameService.getGameById((long) gameIdToJoin);

    // Check if a user with userTokenToJoin exists, throw NOT_FOUND otherwise
    User userToJoin = userService.getUserByToken(userTokenToJoin);

    // Check if the player already exists in playerRepository
    if (checkIfPlayerExistsByUserToken(userTokenToJoin)) {
      // if yes, just set his gameId
      log.info("Player already exists, set gameId to {}", gameIdToJoin);
      playerJoining = getPlayerByUserToken(userTokenToJoin);
      playerJoining.setGameId((long) gameIdToJoin);
      savePlayer(playerJoining);
    } else {
      // if no, create a new player through userService and add
      log.info("Player is created ...");
      playerJoining = new Player();
      playerJoining.setGameId((long) gameIdToJoin);
      playerJoining.setPlayerName(userToJoin.getUsername());
      playerJoining.setUserToken(userToJoin.getToken());
      playerJoining.setToken(UUID.randomUUID().toString());
      playerJoining.setPlayerColor(PlayerColor.NOTSET);

      savePlayer(playerJoining);
    }

    return playerJoining;
  }

  private void savePlayer(Player playerToSave) {
    playerRepository.save(playerToSave);
    playerRepository.flush();
  }

}
