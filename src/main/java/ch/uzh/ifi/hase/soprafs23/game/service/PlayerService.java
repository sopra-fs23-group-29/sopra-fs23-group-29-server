package ch.uzh.ifi.hase.soprafs23.game.service;

import ch.uzh.ifi.hase.soprafs23.constant.PlayerColor;
import ch.uzh.ifi.hase.soprafs23.game.entity.Player;
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

  @Autowired
  public PlayerService(@Qualifier("playerRepository") PlayerRepository playerRepository) {
    this.playerRepository = playerRepository;
  }

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
