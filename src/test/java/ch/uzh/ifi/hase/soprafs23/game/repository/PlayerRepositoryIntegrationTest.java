package ch.uzh.ifi.hase.soprafs23.game.repository;

import ch.uzh.ifi.hase.soprafs23.constant.*;
import ch.uzh.ifi.hase.soprafs23.game.entity.Game;
import ch.uzh.ifi.hase.soprafs23.game.entity.Lobby;
import ch.uzh.ifi.hase.soprafs23.game.entity.Player;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.event.annotation.BeforeTestClass;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class PlayerRepositoryIntegrationTest {

  @Autowired
  private TestEntityManager entityManager;

//  @Autowired
//  private PlayerRepository playerRepository;

  @Autowired
  private GameRepository gameRepository;

//  @Autowired
//  private LobbyRepository lobbyRepository;

  final int boardsize = 10;
  final int maxduration = 10;
  final int maxturns = 10;

//  g1.setId(1L);
//  g1.setGamename("g1");
//  g1.setToken("g1");
//  g1.setGamestatus(GameStatus.INPROGRESS);
//  g1.setGamemode(GameMode.PVP);
//  g1.setBoardsize(boardsize);
//  g1.setMaxduration(maxduration);
//  g1.setMaxturns(maxturns);

//  Lobby l1 = new Lobby();
//  l1.setId(1L);
//  l1.setToken("l1");
//  l1.setLobbyname("l1");
//  l1.setLobbystatus(LobbyStatus.INLOBBY);
//  l1.setGamemode(GameMode.PVP);
//  l1.setBoardsize(boardsize);
//  l1.setMaxduration(maxduration);
//  l1.setMaxturns(maxturns);

//  @BeforeAll
//  public static void initBeforeEach() {
//
////    Game g1 = new Game();
////    g1.setId(1L);
////    g1.setGamename("g1");
////    g1.setToken("g1");
////    g1.setGamestatus(GameStatus.INPROGRESS);
////    g1.setGamemode(GameMode.PVP);
////    g1.setBoardsize(boardsize);
////    g1.setMaxduration(maxduration);
////    g1.setMaxturns(maxturns);
////
////    Lobby l1 = new Lobby();
////    l1.setId(1L);
////    l1.setToken("l1");
////    l1.setLobbyname("l1");
////    l1.setLobbystatus(LobbyStatus.INLOBBY);
////    l1.setGamemode(GameMode.PVP);
////    l1.setBoardsize(boardsize);
////    l1.setMaxduration(maxduration);
////    l1.setMaxturns(maxturns);
//
////    p1.setId(1L);
////    p1.setToken("p1");
////    p1.setPlayername("p1");
////    p1.setPlayercolor(PlayerColor.NOTSET);
////    p1.setGame(g1);
////    p1.setLobby(l1);
////
////    p2.setId(2L);
////    p2.setToken("p2");
////    p2.setPlayername("p2");
////    p2.setPlayercolor(PlayerColor.BLUE);
////    p2.setLobby(l1);
//
//  }

  @Test
  public void findByUsername_success() {

    // given - persist a game
    Game g1 = new Game();
    g1.setId(1L);
    g1.setGamename("g1");
    g1.setToken("g1");
    g1.setGamestatus(GameStatus.INPROGRESS);
    g1.setGamemode(GameMode.PVP);
    g1.setBoardsize(boardsize);
    g1.setMaxduration(maxduration);
    g1.setMaxturns(maxturns);
    entityManager.persist(g1);

//     given - persist a lobby
//    entityManager.persist(l1);

//    // given - create a user
//    Player p1 = new Player();
//    p1.setId(1L);
//    p1.setToken("p1");
//    p1.setPlayername("p1");
//    p1.setPlayercolor(PlayerColor.NOTSET);
//    p1.setGame(g1);
//    p1.setLobby(l1);
//
//    System.out.println(p1);
//
//    // given - Save player to repository
//    entityManager.persist(p1);
//    entityManager.flush();
//
//    // when - Retrieve the player
//    Player playerFound = playerRepository.findByPlayername("p1");
//
//    // then - Make sure we have the right player
//    assertEquals(playerFound.getId(), 1);
//    assertEquals(playerFound.getPlayername(), "p1");
//    assertEquals(playerFound.getPlayercolor(), PlayerColor.NOTSET);
//    assertEquals(playerFound.getGame(), g1);
//    assertEquals(playerFound.getLobby(), l1);

  }

}