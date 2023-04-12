package ch.uzh.ifi.hase.soprafs23.game.websockets.dto.outgoing;

import ch.uzh.ifi.hase.soprafs23.game.entity.Leaderboard;
import ch.uzh.ifi.hase.soprafs23.game.entity.LeaderboardEntry;
import ch.uzh.ifi.hase.soprafs23.game.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class LeaderboardDTO {

  private List<LeaderboardEntry> players;

  public LeaderboardDTO(Leaderboard leaderboard) {
    this.players = new ArrayList<>();
    this.players.addAll(leaderboard.getPlayers());
  }

}
