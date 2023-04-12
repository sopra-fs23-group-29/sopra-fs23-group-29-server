package ch.uzh.ifi.hase.soprafs23.game.websockets.dto.outgoing;

import ch.uzh.ifi.hase.soprafs23.constant.PlayerColor;
import ch.uzh.ifi.hase.soprafs23.game.entity.Player;
import ch.uzh.ifi.hase.soprafs23.game.entity.Turn;
import ch.uzh.ifi.hase.soprafs23.game.questions.RankQuestion;

import java.util.HashMap;
import java.util.List;

public class TurnOutgoingDTO {

  private int turnNumber;
  private List<Player> turnPlayers;
  private RankQuestion rankQuestion;
  private HashMap<String, Integer> savedGuesses; // <CountryCode, Guess taken>
  private HashMap<String, PlayerColor> savedColors; // <CountryCode, PlayerColor the guess was taken>

  public TurnOutgoingDTO(Turn turn) {
    this.turnNumber = turn.getTurnNumber();
    this.turnPlayers = turn.getTurnPlayers();
    this.rankQuestion = turn.getRankQuestion();
    this.savedGuesses = turn.getSavedGuesses();
    this.savedColors = turn.getSavedColors();
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
  public RankQuestion getRankQuestion() {
    return rankQuestion;
  }
  public void setRankQuestion(RankQuestion rankQuestion) {
    this.rankQuestion = rankQuestion;
  }
  public HashMap<String, Integer> getSavedGuesses() {
    return savedGuesses;
  }
  public void setSavedGuesses(HashMap<String, Integer> savedGuesses) {
    this.savedGuesses = savedGuesses;
  }
  public HashMap<String, PlayerColor> getSavedColors() {
    return savedColors;
  }
  public void setSavedColors(HashMap<String, PlayerColor> savedColors) {
    this.savedColors = savedColors;
  }

}
