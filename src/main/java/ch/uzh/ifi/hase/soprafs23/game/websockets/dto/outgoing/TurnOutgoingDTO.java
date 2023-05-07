package ch.uzh.ifi.hase.soprafs23.game.websockets.dto.outgoing;

import ch.uzh.ifi.hase.soprafs23.game.entity.Guess;
import ch.uzh.ifi.hase.soprafs23.game.questions.restCountry.RankingQuestion;
import ch.uzh.ifi.hase.soprafs23.game.entity.Player;
import ch.uzh.ifi.hase.soprafs23.game.entity.Turn;

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

  public List<Guess> getTakenGuesses() {
    return takenGuesses;
  }

}
