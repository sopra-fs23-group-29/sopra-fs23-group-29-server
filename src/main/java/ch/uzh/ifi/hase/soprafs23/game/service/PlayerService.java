package ch.uzh.ifi.hase.soprafs23.game.service;

import ch.uzh.ifi.hase.soprafs23.game.entity.Player;
import ch.uzh.ifi.hase.soprafs23.game.repository.PlayerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.List;

public class PlayerService {

  private final Logger log = LoggerFactory.getLogger(UserService.class);

  private final PlayerRepository playerRepository;

  @Autowired
  public PlayerService(@Qualifier("playerRepository") PlayerRepository playerRepository) {
    this.playerRepository = playerRepository;
  }

  public List<Player> getPlayers() {return this.playerRepository.findAll();}



}
