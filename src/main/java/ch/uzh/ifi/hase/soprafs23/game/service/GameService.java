package ch.uzh.ifi.hase.soprafs23.game.service;

import ch.uzh.ifi.hase.soprafs23.constant.GameMode;
import ch.uzh.ifi.hase.soprafs23.constant.GameStatus;
import ch.uzh.ifi.hase.soprafs23.game.entity.Game;
import ch.uzh.ifi.hase.soprafs23.game.entity.Player;
import ch.uzh.ifi.hase.soprafs23.game.repository.GameRepository;
import ch.uzh.ifi.hase.soprafs23.game.repository.PlayerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class GameService {

  private final Logger log = LoggerFactory.getLogger(UserService.class);

  private final GameRepository gameRepository;
  private final PlayerRepository playerRepository;
  private final PlayerService playerService;

  @Autowired
  public GameService(
          @Qualifier("gameRepository") GameRepository gameRepository,
          @Qualifier("playerRepository") PlayerRepository playerRepository,
          PlayerService playerService) {
    this.gameRepository = gameRepository;
    this.playerRepository = playerRepository;
    this.playerService = playerService;
  }

  /**
   * Create a game from the UserToken of the owner and the desired GameMode
   * @param userToken The userToken of the User who initialized the game
   * @param gameMode The desired gamemode
   * @return Game instance
   */
  public Game createGame(String userToken, GameMode gameMode) {

    // todo: Check if the User that initiated the request has already a game open

    // Create token
    String gameToken = UUID.randomUUID().toString();

    // Create dummy name
    String gameName = "testGameName";

    // Create a player from the supplied User
    Player playerOwner = playerService.createPlayer(userToken);

    // Create the new game instance
    Game newGame = new Game(gameName, gameToken, gameMode, playerOwner);

    // Set GameStatus to INLOBBY
    newGame.setGamestatus(GameStatus.INLOBBY);

    // Set dummy boardsize, maxturns, maxduration
    newGame.setMaxturns(10);
    newGame.setMaxduration(10);
    newGame.setBoardsize(10);

    // Save Game
    Game savedGame = gameRepository.save(newGame);
    gameRepository.flush();

    log.debug("Saved Game {}", savedGame);
    return savedGame;
  }

}
