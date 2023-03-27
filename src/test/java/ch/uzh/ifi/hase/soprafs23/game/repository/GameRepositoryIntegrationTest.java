package ch.uzh.ifi.hase.soprafs23.game.repository;

import ch.uzh.ifi.hase.soprafs23.constant.*;
import ch.uzh.ifi.hase.soprafs23.game.entity.Game;
import ch.uzh.ifi.hase.soprafs23.game.entity.Player;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class GameRepositoryIntegrationTest {

  @Autowired
  private TestEntityManager entityManager;

  @Autowired
  private GameRepository gameRepository;
  @Autowired
  private PlayerRepository playerRepository;

  @Test
  void include_ID_twice() {
    // given - Two games are saved to the repo
    Game g1 = new Game();
    g1.setId(1L);
    g1.setGamename("g1");
    g1.setToken("g1");
    g1.setGamestatus(GameStatus.INPROGRESS);
    g1.setGamemode(GameMode.PVP);
    g1.setBoardsize(11);
    g1.setMaxduration(11);
    g1.setMaxturns(11);

    Game g2 = new Game();
    g1.setId(1L);
    g1.setGamename("g2");
    g1.setToken("g2");
    g1.setGamestatus(GameStatus.INPROGRESS);
    g1.setGamemode(GameMode.PVP);
    g1.setBoardsize(11);
    g1.setMaxduration(11);
    g1.setMaxturns(11);

    Game g1_saved = gameRepository.save(g1);

    assertThrows(org.springframework.dao.DataIntegrityViolationException.class, () -> gameRepository.save(g2));
  }

  @Test
  void findByGamename() {
    // given - A game is saved to the repo
    Game g1 = new Game();
    g1.setGamename("g1");
    g1.setToken("g1");
    g1.setGamestatus(GameStatus.INPROGRESS);
    g1.setGamemode(GameMode.PVP);
    g1.setBoardsize(11);
    g1.setMaxduration(11);
    g1.setMaxturns(11);

    Game g1_saved = gameRepository.save(g1);
    entityManager.persistAndFlush(g1_saved);

    // when - Search game
    Game g1_found = gameRepository.findByGamename("g1");

    // then - Make sure game is there
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
    Game game_not_found = gameRepository.findByGamename("g2");

    assertNull(game_not_found);
  }

  @Test
  void findByToken() {
    // given - A game is saved to the repo
    Game g1 = new Game();
    g1.setId(1L);
    g1.setGamename("g1");
    g1.setToken("g1");
    g1.setGamestatus(GameStatus.INPROGRESS);
    g1.setGamemode(GameMode.PVP);
    g1.setBoardsize(11);
    g1.setMaxduration(11);
    g1.setMaxturns(11);

    Game g1_saved = gameRepository.save(g1);
    entityManager.persistAndFlush(g1_saved);

    // when - Search game
    Game g1_found = gameRepository.findByToken("g1");

    // then - Make sure game is there
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
    Game game_not_found = gameRepository.findByGamename("g2");

    assertNull(game_not_found);
  }

  @Test
  void findPlayers_empty() {
    // given - game without players
    Game g1 = new Game();
    g1.setId(1L);
    g1.setGamename("g1");
    g1.setToken("g1");
    g1.setGamestatus(GameStatus.INPROGRESS);
    g1.setGamemode(GameMode.PVP);
    g1.setBoardsize(11);
    g1.setMaxduration(11);
    g1.setMaxturns(11);

    Game g1_saved = gameRepository.save(g1);
    entityManager.persistAndFlush(g1_saved);

    Game g1_found = gameRepository.findByGamename("g1");

    // when - players is empty, should return empty list
    List<Player> emptyList = g1_found.getPlayers();

    assertTrue(emptyList.isEmpty());

  }

//  @Test
//  void findPlayers() {
//    // given - Create game
//    Game g1 = new Game();
//    g1.setId(1L);
//    g1.setGamename("g1");
//    g1.setToken("g1");
//    g1.setGamestatus(GameStatus.INPROGRESS);
//    g1.setGamemode(GameMode.PVP);
//    g1.setBoardsize(11);
//    g1.setMaxduration(11);
//    g1.setMaxturns(11);
//
//    // given - Add players
//    Player p1 = new Player();
//    p1.setPlayername("player1");
//    p1.setToken("p1");
//    p1.setPlayercolor(PlayerColor.NOTSET);
//
//    Player p2 = new Player();
//    p2.setPlayername("player2");
//    p2.setToken("p2");
//    p2.setPlayercolor(PlayerColor.BLUE);
//
//    g1.addPlayer(p1);
//    g1.addPlayer(p2);
//
//    // List to find
//    List<Player> players_to_find = Arrays.asList(p1, p2);
//
//    Game g1_saved = gameRepository.save(g1);
//    entityManager.persistAndFlush(g1_saved);
//
//    // when - Search game
//    Game g1_found = gameRepository.findByGamename("g1");
//
//    // then - Make sure players are there
//    List<Player> players_found = g1_found.getPlayers();
//
//    assertFalse(players_found.isEmpty());
//    assertEquals(players_to_find, players_found);
//  }
}
