package ch.uzh.ifi.hase.soprafs23.game.service;

import ch.uzh.ifi.hase.soprafs23.constant.GameMode;
import ch.uzh.ifi.hase.soprafs23.constant.GameStatus;
import ch.uzh.ifi.hase.soprafs23.game.entity.Game;
import ch.uzh.ifi.hase.soprafs23.game.entity.Player;
import ch.uzh.ifi.hase.soprafs23.game.repository.GameRepository;
import ch.uzh.ifi.hase.soprafs23.game.repository.PlayerRepository;
import ch.uzh.ifi.hase.soprafs23.game.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class GameService {

  private final Logger log = LoggerFactory.getLogger(UserService.class);

  private final UserRepository userRepository;
  private final PlayerRepository playerRepository;
  private final PlayerService playerService;
  private int gameCounter;

  @Autowired
  public GameService(
          @Qualifier("userRepository") UserRepository userRepository,
          @Qualifier("playerRepository") PlayerRepository playerRepository,
          PlayerService playerService) {
    this.userRepository = userRepository;
    this.playerRepository = playerRepository;
    this.playerService = playerService;
  }

  /**
   * Create a new game and return the corresponding int
   * @return
   */
  public int createNewGame(String gameName, GameMode gameMode) {
    gameCounter++;
    removeAllPlayersFromGame(gameCounter);
    Game newGame = new Game(gameName, gameMode, userRepository);
    GameRepository.addGame(gameCounter, newGame);
    return gameCounter;
  }

  private void removeAllPlayersFromGame(int gameId) {
    List<Player> players = playerRepository.findByGameId((long) gameId);
    for (Player player : players) {
      log.info("Deleted Player: {}", player.getPlayername());
      playerRepository.deleteById(player.getId());
    }

  }

}
