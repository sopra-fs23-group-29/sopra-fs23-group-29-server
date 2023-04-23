package ch.uzh.ifi.hase.soprafs23.game.websockets.dto.outgoing;

import ch.uzh.ifi.hase.soprafs23.game.entity.Country;
import ch.uzh.ifi.hase.soprafs23.game.entity.Leaderboard;
import ch.uzh.ifi.hase.soprafs23.game.entity.LeaderboardEntry;
import ch.uzh.ifi.hase.soprafs23.game.entity.Turn;
import ch.uzh.ifi.hase.soprafs23.game.questions.restCountry.RankingQuestion;

import java.util.ArrayList;
import java.util.List;

public class LeaderboardDTO {

  private List<LeaderboardEntry> scoreboardEntries;
  private RankingQuestion rankingQuestion;

  public LeaderboardDTO(Leaderboard leaderboard, Turn currentTurn) {
    this.scoreboardEntries = new ArrayList<>();
    this.scoreboardEntries.addAll(leaderboard.getEntries());

    this.rankingQuestion = currentTurn.getRankQuestion();
  }

}
