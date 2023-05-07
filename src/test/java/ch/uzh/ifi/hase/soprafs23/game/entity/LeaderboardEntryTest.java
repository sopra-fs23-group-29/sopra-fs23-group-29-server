package ch.uzh.ifi.hase.soprafs23.game.entity;

import ch.uzh.ifi.hase.soprafs23.constant.PlayerColor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.web.WebAppConfiguration;

import static org.junit.jupiter.api.Assertions.*;

@WebAppConfiguration
@SpringBootTest
class LeaderboardEntryTest {

  private LeaderboardEntry lbe;

  @BeforeEach
  void setUp() {
    lbe = new LeaderboardEntry(1L, 0, "p1", PlayerColor.INDIANRED);
  }

  @Test
  void getPlayerId() {
    assertEquals(lbe.getPlayerId(), 1L);
  }

  @Test
  void getCurrentScore() {
    assertEquals(lbe.getCurrentScore(), 0);
  }

  @Test
  void addScore() {
    // add score, then check
    assertEquals(lbe.getCurrentScore(), 0);
    lbe.addScore(100);
    assertEquals(lbe.getCurrentScore(), 100);
    lbe.addScore(-5);
    assertEquals(lbe.getCurrentScore(), 95);
    lbe.addScore(-100);
    assertEquals(lbe.getCurrentScore(), -5);
  }

  @Test
  void replaceScore() {
    // replace score, then check, then do again
    assertEquals(lbe.getCurrentScore(), 0);
    lbe.replaceScore(5);
    assertEquals(lbe.getCurrentScore(), 5);
    lbe.replaceScore(20);
    assertEquals(lbe.getCurrentScore(), 20);
  }

  @Test
  void setGuessCountryCode() {
    assertNull(lbe.getGuessCountryCode());
    lbe.setGuessCountryCode("ABC");
    assertEquals(lbe.getGuessCountryCode(),"ABC");
  }

  @Test
  void setGuess() {
    lbe.setGuess(3);
    assertEquals(lbe.getGuess(),3);
    lbe.setGuess(10);
    assertEquals(lbe.getGuess(),10);
  }
}