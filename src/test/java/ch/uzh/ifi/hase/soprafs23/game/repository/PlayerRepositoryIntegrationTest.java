package ch.uzh.ifi.hase.soprafs23.game.repository;

import ch.uzh.ifi.hase.soprafs23.constant.GameMode;
import ch.uzh.ifi.hase.soprafs23.constant.GameStatus;
import ch.uzh.ifi.hase.soprafs23.constant.PlayerColor;
import ch.uzh.ifi.hase.soprafs23.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs23.game.entity.Game;
import ch.uzh.ifi.hase.soprafs23.game.entity.Player;
import ch.uzh.ifi.hase.soprafs23.game.entity.User;
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
  private PlayerRepository playerRepository;

  public Game g1;
  public Player p1;
  public Player p2;
  public User u1;

  @BeforeEach
  public void init() {

    p1 = new Player("player1", "p1", "userToken1");
    p1.setPlayercolor(PlayerColor.BLUE);
    p2 = new Player("player2", "p2", "userToken2");
    p2.setPlayercolor(PlayerColor.BLUE);

    g1 = new Game("g1", "g1", GameMode.PVP, p1);
    g1.setGamestatus(GameStatus.INLOBBY);
    g1.setBoardsize(11);
    g1.setMaxduration(11);
    g1.setMaxturns(11);

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

  @Test
  void findByUserToken() {

    // when - persist the player
    entityManager.persist(p1);
    entityManager.flush();

    // then - retrieved user matches
    Player p1_found = playerRepository.findByUserToken("userToken1");

    assertNotNull(p1_found.getId());
    assertEquals(p1_found.getToken(), p1.getToken());
    assertEquals(p1_found.getPlayername(), p1.getPlayername());

  }


}
