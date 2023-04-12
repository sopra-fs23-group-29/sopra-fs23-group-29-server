package ch.uzh.ifi.hase.soprafs23.game.entity;

import ch.uzh.ifi.hase.soprafs23.constant.PlayerColor;
import ch.uzh.ifi.hase.soprafs23.game.questions.RankQuestion;

import java.util.HashMap;
import java.util.List;

public class Turn {
  private int turnNumber;
  private final List<Player> turnPlayers;
  private final RankQuestion rankQuestion;
  private final HashMap<String, Integer> savedGuesses; // <CountryCode, Guess taken>
  private final HashMap<String, PlayerColor> savedColors; // <CountryCode, PlayerColor the guess was taken>

  public Turn(int turnNumber, List<Player> turnPlayers, RankQuestion rankQuestion) {
    this.turnNumber = turnNumber;
    this.turnPlayers = turnPlayers;
    this.rankQuestion = rankQuestion;

    // upon creation, create empty HashMap with the savedGuesses and colors
    this.savedGuesses = new HashMap<>();
    this.savedColors = new HashMap<>();
  }

  public HashMap<String, Integer> getSavedGuesses() {
    return savedGuesses;
  }
  public HashMap<String, PlayerColor> getSavedColors() {
    return savedColors;
  }

  public int evaluateGuess(String countryCode, int guess) {
    return rankQuestion.getScore(countryCode, guess);
  }

  public void saveGuess(String countryCode, int guess, PlayerColor guessColor) {
    savedGuesses.put(countryCode, guess);
    savedColors.put(countryCode, guessColor);
  }

}
