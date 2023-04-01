package ch.uzh.ifi.hase.soprafs23.game.service;

import ch.uzh.ifi.hase.soprafs23.constant.PlayerColor;
import ch.uzh.ifi.hase.soprafs23.game.entity.Player;
import ch.uzh.ifi.hase.soprafs23.game.entity.User;
import ch.uzh.ifi.hase.soprafs23.game.repository.PlayerRepository;
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
public class PlayerService {

  private final Logger log = LoggerFactory.getLogger(UserService.class);

  private final PlayerRepository playerRepository;
  private final UserService userService;

  @Autowired
  public PlayerService(
          @Qualifier("playerRepository") PlayerRepository playerRepository,
          UserService userService) {
    this.playerRepository = playerRepository;
    this.userService = userService;
  }

  /**
   * Create and save a new Player from a UserToken
   * @param userToken A valid UserToken
   * @return Player created and saved
   */
  public Player createPlayer(String userToken) {

    // fetch the user from the token
    User fetchedUser = userService.getUserByToken(userToken);
    String playername = fetchedUser.getUsername();

    // Create token
    String playerToken = UUID.randomUUID().toString();

    // Create Player instance
    Player newPlayer = new Player(playername, playerToken, userToken);

    // Set PlayerColor to not set
    newPlayer.setPlayercolor(PlayerColor.NOTSET);

    // todo: Check if a player with that userToken already exists?

    // Save Player
    Player savedPlayer = playerRepository.save(newPlayer);
    playerRepository.flush();

    log.debug("Saved Player {}", savedPlayer);
    return savedPlayer;

  }

  /**
   * Save a new Player from an existing Player instance
   * @param newPlayer
   * @return Player created and saved
   */
  public Player createPlayer(Player newPlayer) {

    // Create token
    newPlayer.setToken(UUID.randomUUID().toString());

    // Set PlayerColor to not set
    newPlayer.setPlayercolor(PlayerColor.NOTSET);

    // todo: Check for unique playername and game combination?

    // Save Player
    Player savedPlayer = playerRepository.save(newPlayer);
    playerRepository.flush();

    log.debug("Saved Player {}", savedPlayer);
    return savedPlayer;
  }

  public List<Player> getPlayers() {return this.playerRepository.findAll();}

}
