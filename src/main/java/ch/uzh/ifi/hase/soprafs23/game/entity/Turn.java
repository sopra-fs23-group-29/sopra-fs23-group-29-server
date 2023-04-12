package ch.uzh.ifi.hase.soprafs23.game.entity;

import ch.uzh.ifi.hase.soprafs23.game.questions.Question;

import java.util.List;

public class Turn {
  private List<Player> turnPlayers;
  private Question question;

  public Turn(List<Player> turnPlayers, Question question) {
    this.turnPlayers = turnPlayers;
    this.question = question;
  }
}
