package ch.uzh.ifi.hase.soprafs23.game.websockets.dto.outgoing;

import ch.uzh.ifi.hase.soprafs23.constant.PlayerColor;
import ch.uzh.ifi.hase.soprafs23.game.entity.Guess;
import ch.uzh.ifi.hase.soprafs23.game.questions.restCountry.RankingQuestion;
import ch.uzh.ifi.hase.soprafs23.game.entity.Player;
import ch.uzh.ifi.hase.soprafs23.game.entity.Turn;

import java.util.HashMap;
import java.util.List;

public class TurnOutgoingDTO {

  private int turnNumber;
  private List<Player> turnPlayers;
  private RankingQuestion rankQuestion;
  private List<Guess> takenGuesses; // Whenever an answer is saved, the guess is recorded as a Guess object

  public TurnOutgoingDTO(Turn turn) {
    this.turnNumber = turn.getTurnNumber();
    this.turnPlayers = turn.getTurnPlayers();
    this.rankQuestion = turn.getRankQuestion();
    this.takenGuesses = turn.getTakenGuesses();
  }

  public int getTurnNumber() {
    return turnNumber;
  }
  public void setTurnNumber(int turnNumber) {
    this.turnNumber = turnNumber;
  }
  public List<Player> getTurnPlayers() {
    return turnPlayers;
  }
  public void setTurnPlayers(List<Player> turnPlayers) {this.turnPlayers = turnPlayers;}
  public RankingQuestion getRankQuestion() {
    return rankQuestion;
  }
  public void setRankQuestion(RankingQuestion rankQuestion) {
    this.rankQuestion = rankQuestion;
  }
  public List<Guess> getTakenGuesses() {
    return takenGuesses;
  }
  public void setTakenGuesses(List<Guess> takenGuesses) {
    this.takenGuesses = takenGuesses;
  }
}
