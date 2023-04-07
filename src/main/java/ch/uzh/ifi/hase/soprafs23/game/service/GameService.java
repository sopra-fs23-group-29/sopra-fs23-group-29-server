package ch.uzh.ifi.hase.soprafs23.game.service;

import ch.uzh.ifi.hase.soprafs23.constant.GameMode;
import ch.uzh.ifi.hase.soprafs23.game.entity.Game;
import ch.uzh.ifi.hase.soprafs23.game.entity.Player;
import ch.uzh.ifi.hase.soprafs23.game.repository.GameRepository;
import ch.uzh.ifi.hase.soprafs23.game.repository.PlayerRepository;
import ch.uzh.ifi.hase.soprafs23.game.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs23.game.websockets.dto.outgoing.GameJoinedDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class GameService {

  private final Logger log = LoggerFactory.getLogger(UserService.class);
  private final PlayerRepository playerRepository;
  private final WebSocketService webSocketService;
  private int gameCounter;

  @Autowired
  public GameService(
          @Qualifier("playerRepository") PlayerRepository playerRepository,
          WebSocketService webSocketService) {
    this.playerRepository = playerRepository;
    this.webSocketService = webSocketService;
  }

  public Game getGameById(Long gameId) {
    return GameRepository.findByGameId(gameId);
  }

  /**
   * Create a new game and return the corresponding int
   * @return gameId of the created game
   * @throws org.springframework.web.server.ResponseStatusException
   */
  public Long createNewGame(String gameName, GameMode gameMode) {
    gameCounter++;
    removeAllPlayersFromGame((long) gameCounter);
    Game newGame = new Game((long) gameCounter, gameName, gameMode);
    GameRepository.addGame((long) gameCounter, newGame);
    return (long) gameCounter;
  }

  private void removeAllPlayersFromGame(Long gameId) {
    List<Player> players = playerRepository.findByGameId(gameId);
    for (Player player : players) {
      log.info("Deleted Player: {}", player.getPlayerName());
      playerRepository.deleteById(player.getId());
    }

  }

  public void greetGames(Game game) {
    GameJoinedDTO gameJoinedDTO = new GameJoinedDTO();
    gameJoinedDTO.setGameName(game.getGameName());
    gameJoinedDTO.setGameMode(game.getGameMode());
    this.webSocketService.sendMessageToClients("/topic/games", gameJoinedDTO);
  }

}
