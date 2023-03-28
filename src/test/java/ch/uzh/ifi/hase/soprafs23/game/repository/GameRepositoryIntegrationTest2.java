package ch.uzh.ifi.hase.soprafs23.game.repository;

import ch.uzh.ifi.hase.soprafs23.constant.GameMode;
import ch.uzh.ifi.hase.soprafs23.constant.GameStatus;
import ch.uzh.ifi.hase.soprafs23.constant.PlayerColor;
import ch.uzh.ifi.hase.soprafs23.game.entity.Game;
import ch.uzh.ifi.hase.soprafs23.game.entity.Player;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class GameRepositoryIntegrationTest2 {

  @Autowired
  private TestEntityManager entityManager;

  @Autowired
  private GameRepository gameRepository;
  @Autowired
  private PlayerRepository playerRepository;

  private static final Game g1 = new Game();
  private static final Game g2 = new Game();
  private static final Player p1 = new Player();
  private static final Player p2 = new Player();

  @BeforeAll
  public static void init() {

    g1.setGamename("g1");
    g1.setToken("g1");
    g1.setGamestatus(GameStatus.INPROGRESS);
    g1.setGamemode(GameMode.PVP);
    g1.setBoardsize(11);
    g1.setMaxduration(11);
    g1.setMaxturns(11);

    g2.setGamename("g2");
    g2.setToken("g2");
    g2.setGamestatus(GameStatus.INPROGRESS);
    g2.setGamemode(GameMode.PVP);
    g2.setBoardsize(11);
    g2.setMaxduration(11);
    g2.setMaxturns(11);

    p1.setPlayername("player1");
    p1.setToken("p1");
    p1.setPlayercolor(PlayerColor.NOTSET);

    p2.setPlayername("player2");
    p2.setToken("p2");
    p2.setPlayercolor(PlayerColor.BLUE);

    g1.addPlayer(p1);
    g1.addPlayer(p2);
  }

  @BeforeEach
  public void initEach() {
    entityManager.clear();
    entityManager.flush();
  }

  @Test
  void findByGamename() {

    entityManager.persistAndFlush(g1);

    Game g1_found = gameRepository.findByGamename("g1");

    // then - Make sure game is there
    assertNotNull(g1_found.getId());
    assertEquals(g1_found.getGamename(), g1.getGamename());
    assertEquals(g1_found.getToken(), g1.getToken());
    assertEquals(g1_found.getGamestatus(), g1.getGamestatus());
    assertEquals(g1_found.getGamemode(), g1.getGamemode());
    assertEquals(g1_found.getBoardsize(), g1.getBoardsize());
    assertEquals(g1_found.getMaxduration(), g1.getMaxduration());
    assertEquals(g1_found.getMaxturns(), g1.getMaxturns());
  }

  @Test
  void findByGamename_failure() {
    // given - Empty repo

    // when - Search gam
    Game game_not_found = gameRepository.findByGamename("unknown");

    assertNull(game_not_found);
  }

  @Test
  void findByToken() {
    // given - Game saved
    entityManager.persistAndFlush(g1);

    // when - Search game
    Game g1_found = gameRepository.findByToken("g1");

    // then - Make sure game is there
    assertNotNull(g1_found.getId());
    assertEquals(g1_found.getGamename(), g1.getGamename());
    assertEquals(g1_found.getToken(), g1.getToken());
    assertEquals(g1_found.getGamestatus(), g1.getGamestatus());
    assertEquals(g1_found.getGamemode(), g1.getGamemode());
    assertEquals(g1_found.getBoardsize(), g1.getBoardsize());
    assertEquals(g1_found.getMaxduration(), g1.getMaxduration());
    assertEquals(g1_found.getMaxturns(), g1.getMaxturns());
  }

  @Test
  void findByToken_failure() {
    // given - Empty repo

    // when - Search gam
    Game game_not_found = gameRepository.findByGamename("unknown");

    assertNull(game_not_found);
  }

  @Test
  void findPlayers_empty() {
    // given - empty repo
    entityManager.persistAndFlush(g2);

    Game g2_found = gameRepository.findByGamename("g2");

    // when - players is empty, should return empty list
    List<Player> emptyList = g2_found.getPlayers();

    assertTrue(emptyList.isEmpty());

  }

  @Test
  void findPlayers() {

    // given - Add players
    g1.addPlayer(p1);
    g1.addPlayer(p2);

    // List to find
    List<Player> players_to_find = Arrays.asList(p1, p2);

    // given - Game saved
    entityManager.persistAndFlush(g1);

    // when - Search game
    Game g1_found = gameRepository.findByGamename("g1");
    assertNotNull(g1_found);

    // then - Make sure players are there
    List<Player> players_found = g1_found.getPlayers();

    assertFalse(players_found.isEmpty());
    assertEquals(players_to_find, players_found);
  }
}