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
class GameRepositoryIntegrationTest {

  @Autowired
  private TestEntityManager entityManager;

  @Autowired
  private GameRepository gameRepository;
  @Autowired
  private PlayerRepository playerRepository;

  public Game g1;
  public Game g2;
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

    g2 = new Game();
    g2.setGamename("g2");
    g2.setToken("g2");
    g2.setGamestatus(GameStatus.INPROGRESS);
    g2.setGamemode(GameMode.PVP);
    g2.setBoardsize(11);
    g2.setMaxduration(11);
    g2.setMaxturns(11);

    p1 = new Player();
    p1.setPlayername("player1");
    p1.setToken("p1");
    p1.setPlayercolor(PlayerColor.NOTSET);

    p2 = new Player();
    p2.setPlayername("player2");
    p2.setToken("p2");
    p2.setPlayercolor(PlayerColor.BLUE);

    g1.addPlayer(p1);
    g1.addPlayer(p2);
  }

  @Test
  void duplicatedAdditionPlayer_doNothing() {

    // given - The list of players of the game
    List<Player> g1_players_before = g1.getPlayers();

    // when - Adding a player a second time to a game
    g1.addPlayer(p1);

    // then - Nothing changed about the players list
    List<Player> g1_players_after = g1.getPlayers();

    assertEquals(g1_players_before, g1_players_after);

  }

  @Test
  void findByGamename() {

    entityManager.persist(g1);
    entityManager.flush();

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
    entityManager.persist(g1);
    entityManager.flush();

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
    entityManager.persist(g2);
    entityManager.flush();

    Game g2_found = gameRepository.findByGamename("g2");

    // when - players is empty, should return empty list
    List<Player> emptyList = g2_found.getPlayers();

    assertTrue(emptyList.isEmpty());

  }

  @Test
  void findPlayers() {

    // List to find
    List<Player> players_to_find = Arrays.asList(p1, p2);
    System.out.println("players_to_find");
    players_to_find.forEach(System.out::println);

    // given - Game saved
    entityManager.persist(g1);
    entityManager.flush();

    // when - Search game
    Game g1_found = gameRepository.findByGamename("g1");
    assertNotNull(g1_found);

    // then - Make sure players are there
    List<Player> players_found = g1_found.getPlayers();

    // check player repository
    List<Player> players_repository = playerRepository.findAll();
    System.out.println("players_repository");
    players_repository.forEach(System.out::println);

    System.out.println("players_found from game in gameRepository");
    players_found.forEach(System.out::println);

    assertFalse(players_found.isEmpty());
    assertEquals(players_to_find, players_found);
  }

  @Test
  void findPlayers_remove() {

    // List to find - only p1 should be found
    List<Player> players_to_find = Arrays.asList(p1);

    // given - Remove p2 from g1
    g1.removePlayer(p2);

    // given - Game saved
    entityManager.persist(g1);
    entityManager.flush();

    // when - Search game
    Game g1_found = gameRepository.findByGamename("g1");
    assertNotNull(g1_found);

    // then - Make sure p1 is there
    List<Player> players_found = g1_found.getPlayers();

    assertFalse(players_found.isEmpty());
    assertEquals(players_to_find, players_found);
  }

  @Test
  void whenGameDeleted_PlayersDeleted() {
    // given - players are persisted
    // Would not be neccesary, due to CASCADE.ALL of Game
    entityManager.persist(p1);
    entityManager.flush();
    entityManager.persist(p2);
    entityManager.flush();

    // given - a game with players is persisted
    entityManager.persist(g1);

    // given - find all players associated with the game
    Game g1_repository = gameRepository.findByGamename("g1");
    List<Player> g1_players = g1_repository.getPlayers();

    // when - the players are still present
    List<Player> players_found = playerRepository.findAll();
    assertEquals(players_found.size(), g1_players.size());

    // when - the game is deleted
    entityManager.remove(g1);

    // then - the players are deleted as well
    List<Player> players_left = playerRepository.findAll();

    assertTrue(players_left.isEmpty());

  }

  @Test
  void whenAllPlayersDeleted_gameStillExists() {


    // given - a game with players is persisted
    // Due to CASCADE.ALL, the players are persisted as well
    entityManager.persist(g1);

    // when - All the players that reference the game are deleted
    entityManager.remove(p1);
    // when - Delete one player, will the game be deleted?
    assertNotNull(gameRepository.findByGamename("g1"));
    entityManager.remove(p2);

    // then - Make sure players are gone
    assertNull(playerRepository.findByPlayername("p1"));
    assertNull(playerRepository.findByPlayername("p2"));

    // then - Assert the game is still there
    Game g1_found = gameRepository.findByGamename("g1");
    assertNotNull(g1_found.getId());
    assertEquals(g1_found.getGamename(), g1.getGamename());
    assertEquals(g1_found.getToken(), g1.getToken());
    assertEquals(g1_found.getGamestatus(), g1.getGamestatus());
    assertEquals(g1_found.getGamemode(), g1.getGamemode());
    assertEquals(g1_found.getBoardsize(), g1.getBoardsize());
    assertEquals(g1_found.getMaxduration(), g1.getMaxduration());
    assertEquals(g1_found.getMaxturns(), g1.getMaxturns());


  }
}