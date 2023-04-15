package ch.uzh.ifi.hase.soprafs23.game.websockets.dto.outgoing;

import ch.uzh.ifi.hase.soprafs23.game.entity.Leaderboard;
import ch.uzh.ifi.hase.soprafs23.game.entity.LeaderboardEntry;

import java.util.ArrayList;
import java.util.List;

public class LeaderboardDTO {

  private List<LeaderboardEntry> entries;

  public LeaderboardDTO(Leaderboard leaderboard) {
    this.entries = new ArrayList<>();
    this.entries.addAll(leaderboard.getEntries());
  }

}
