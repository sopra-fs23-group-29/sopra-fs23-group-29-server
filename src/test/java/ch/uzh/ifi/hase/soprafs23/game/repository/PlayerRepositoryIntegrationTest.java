package ch.uzh.ifi.hase.soprafs23.game.repository;

import ch.uzh.ifi.hase.soprafs23.constant.GameMode;
import ch.uzh.ifi.hase.soprafs23.constant.GameStatus;
import ch.uzh.ifi.hase.soprafs23.constant.PlayerColor;
import ch.uzh.ifi.hase.soprafs23.game.entity.Game;
import ch.uzh.ifi.hase.soprafs23.game.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class PlayerRepositoryIntegrationTest {

  @Autowired
  private TestEntityManager entityManager;
  @Autowired
  private GameRepository gameRepository;
  @Autowired
  private PlayerRepository playerRepository;

  public Game g1;
  public Player p1;
  public Player p2;

  @BeforeEach
  public void init() {

    g1 = new Game();
    g1.setGamename("g1");
    g1.setToken("g1");
    g1.setGamestatus(GameStatus.INPROGRESS);
    g1.setGamemode(GameMode.PVP);
    g1.setBoardsize(11);
    g1.setMaxduration(11);
    g1.setMaxturns(11);

    p1 = new Player();
    p1.setPlayername("player1");
    p1.setToken("p1");
    p1.setPlayercolor(PlayerColor.NOTSET);

    p2 = new Player();
    p2.setPlayername("player2");
    p2.setToken("p2");
    p2.setPlayercolor(PlayerColor.BLUE);

    p1.setGame(g1);
    p2.setGame(g1);
  }

  @Test
  void findByGame() {

    List<Player> players_to_find = Arrays.asList(p1, p2);

    entityManager.persist(g1);
    entityManager.flush();

    List<Player> g1_players = playerRepository.findByGame(g1);

    assertEquals(players_to_find, g1_players);

  }
}
