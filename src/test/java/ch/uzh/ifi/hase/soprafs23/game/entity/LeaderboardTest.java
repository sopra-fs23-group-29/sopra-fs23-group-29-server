package ch.uzh.ifi.hase.soprafs23.game.entity;

import ch.uzh.ifi.hase.soprafs23.constant.PlayerColor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@WebAppConfiguration
@SpringBootTest
class LeaderboardTest {

  private Leaderboard lb;
  private LeaderboardEntry lbe1, lbe2;

  @BeforeEach
  void setup() {
    lb = new Leaderboard();
    lbe1 = new LeaderboardEntry(1L, 0, "p1", PlayerColor.INDIANRED);
    lbe2 = new LeaderboardEntry(2L, 0, "p2", PlayerColor.ORANGE);
  }

  @Test
  void putNewEntries() {
    // assert is empty initially
    assertTrue(lb.getEntries().isEmpty());
    // Add two entries
    lb.putNewPlayer(lbe1.getPlayerId(), lbe1.getPlayerName(), lbe1.getPlayerColor());
    lb.putNewPlayer(lbe2.getPlayerId(), lbe2.getPlayerName(), lbe2.getPlayerColor());
    // assert size 2
    assertEquals(lb.getEntries().size(), 2);

  }

  @Test
  void putDuplicatedEntry_throwsException() {
    // assert is empty initially
    assertTrue(lb.getEntries().isEmpty());
    // Add one entry
    lb.putNewPlayer(lbe1.getPlayerId(), lbe1.getPlayerName(), lbe1.getPlayerColor());
    // adding same entry leads to exception
    assertThrows(IllegalArgumentException.class, () -> lb.putNewPlayer(lbe1.getPlayerId(), lbe1.getPlayerName(), lbe1.getPlayerColor()));
  }

  @Test
  void getEntry() {
    // Add one entry
    lb.putNewPlayer(lbe1.getPlayerId(), lbe1.getPlayerName(), lbe1.getPlayerColor());
    // Get entry
    LeaderboardEntry lbe1_retrieved = lb.getEntry(lbe1.getPlayerId());
    // assert equality
    assertEquals(lbe1.getPlayerId(), lbe1_retrieved.getPlayerId());
    assertEquals(lbe1.getPlayerName(), lbe1_retrieved.getPlayerName());
    assertEquals(lbe1.getPlayerColor(), lbe1_retrieved.getPlayerColor());
  }

  @Test
  void getEntry_notExistent_throwsException() {
    assertThrows(IllegalArgumentException.class, () -> lb.getEntry(99L));
  }

  @Test
  void addToEntry() {
    // Add one entry
    lb.putNewPlayer(lbe1.getPlayerId(), lbe1.getPlayerName(), lbe1.getPlayerColor());

    // adding to nonexistent throws exception
    assertThrows(IllegalArgumentException.class, () -> lb.addToEntry(99L, 1));

    // adding to existing works
    lb.addToEntry(lbe1.getPlayerId(), 3);
    assertEquals(lb.getEntry(lbe1.getPlayerId()).getCurrentScore(), 3);
  }

  @Test
  void replaceEntry() {

    // given - guessed country code and guess
    String countyCode = "ABC";
    int guess = 100;

    // Add one entry
    lb.putNewPlayer(lbe1.getPlayerId(), lbe1.getPlayerName(), lbe1.getPlayerColor());

    // adding to nonexistent throws exception
    assertThrows(IllegalArgumentException.class, () -> lb.replaceEntry(99L, 1, "dummy",99));

    // replacing to existing works
    lb.replaceEntry(lbe1.getPlayerId(), 3, countyCode, guess);
    assertEquals(lb.getEntry(lbe1.getPlayerId()).getCurrentScore(), 3);
    assertEquals(lb.getEntry(lbe1.getPlayerId()).getGuess(), guess);
    assertEquals(lb.getEntry(lbe1.getPlayerId()).getGuessCountryCode(), countyCode);

    // replacing again still works
    String countyCode2 = "DEF";
    int guess2 = 200;

    lb.replaceEntry(lbe1.getPlayerId(), 6, countyCode2, guess2);
    assertEquals(lb.getEntry(lbe1.getPlayerId()).getCurrentScore(), 6);
    assertEquals(lb.getEntry(lbe1.getPlayerId()).getGuess(), guess2);
    assertEquals(lb.getEntry(lbe1.getPlayerId()).getGuessCountryCode(), countyCode2);

  }

  @Test
  void sync() {
    // Add two entries
    lb.putNewPlayer(lbe1.getPlayerId(), lbe1.getPlayerName(), lbe1.getPlayerColor());
    lb.putNewPlayer(lbe2.getPlayerId(), lbe2.getPlayerName(), lbe2.getPlayerColor());

    List<Long> playerIdsToKeep = Arrays.asList(1L,2L);
    lb.sync(playerIdsToKeep);
    assertEquals(lb.getEntries().size(), 2);

    List<Long> playerIdsToKeep2 = Arrays.asList(1L);
    lb.sync(playerIdsToKeep2);
    assertEquals(lb.getEntries().size(), 1);

    List<Long> playerIdsToKeep3 = new ArrayList<>();
    lb.sync(playerIdsToKeep3);
    assertTrue(lb.getEntries().isEmpty());
  }

  @Test
  void testToString() {
    System.out.println(lb);

    // Add two entries
    lb.putNewPlayer(lbe1.getPlayerId(), lbe1.getPlayerName(), lbe1.getPlayerColor());
    lb.putNewPlayer(lbe2.getPlayerId(), lbe2.getPlayerName(), lbe2.getPlayerColor());
    lb.addToEntry(lbe1.getPlayerId(), 3);

    System.out.println(lb);

  }
}