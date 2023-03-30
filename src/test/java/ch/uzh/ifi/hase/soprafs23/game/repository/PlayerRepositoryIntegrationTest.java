package ch.uzh.ifi.hase.soprafs23.game.repository;

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
class PlayerRepositoryIntegrationTest {

  @Autowired
  private TestEntityManager entityManager;

  @Autowired
  private PlayerRepository playerRepository;

  public Player p1;
  public Player p2;

  @BeforeEach
  public void init() {

    p1 = new Player();
    p1.setPlayername("player1");
    p1.setToken("p1");
    p1.setPlayercolor(PlayerColor.NOTSET);

    p2 = new Player();
    p2.setPlayername("player2");
    p2.setToken("p2");
    p2.setPlayercolor(PlayerColor.BLUE);

  }

  @Test
  void findByPlayername() {

    entityManager.persist(p1);
    entityManager.flush();

    Player p1_found = playerRepository.findByToken(p1.getPlayername());

    // then - Make sure game is there
    assertNotNull(p1_found.getId());
    assertEquals(p1_found.getPlayername(), p1.getPlayername());
    assertEquals(p1_found.getToken(), p1.getToken());
    assertEquals(p1_found.getPlayercolor(), p1.getPlayercolor());
  }

}