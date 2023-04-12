package ch.uzh.ifi.hase.soprafs23.game.entity;

import ch.uzh.ifi.hase.soprafs23.game.questions.Question;
import ch.uzh.ifi.hase.soprafs23.game.questions.RankQuestion;

import java.util.List;

public class Turn {
  private List<Player> turnPlayers;
  private RankQuestion rankQuestion;

  public Turn(List<Player> turnPlayers, RankQuestion rankQuestion) {
    this.turnPlayers = turnPlayers;
    this.rankQuestion = rankQuestion;
  }
}
