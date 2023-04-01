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
  private GameRepository gameRepository;
  @Autowired
  private PlayerRepository playerRepository;

  public Game g1;
  public Player p1;
  public Player p2;
  public User u1;

  @BeforeEach
  public void init() {

    u1 = new User();
    u1.setUsername("u1");
    u1.setToken("u1");
    u1.setPassword("u1");
    u1.setStatus(UserStatus.ONLINE);

    p1 = new Player();
    p1.setPlayername("player1");
    p1.setToken("p1");
    p1.setPlayercolor(PlayerColor.NOTSET);

    p2 = new Player();
    p2.setPlayername("player2");
    p2.setToken("p2");
    p2.setPlayercolor(PlayerColor.BLUE);

    g1 = new Game("g1", "g1", GameMode.PVP, p1);
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

    // given - set the user for a player
    p1.setUser(u1);

    // when - persist the player
    entityManager.persist(u1);
    entityManager.flush();
    entityManager.persist(p1);
    entityManager.flush();

    // then - retrieved user matches
    Player p1_found = playerRepository.findByUserToken("u1");

    assertNotNull(p1_found.getId());
    assertEquals(p1_found.getToken(), p1.getToken());
    assertEquals(p1_found.getPlayername(), p1.getPlayername());

  }


}
