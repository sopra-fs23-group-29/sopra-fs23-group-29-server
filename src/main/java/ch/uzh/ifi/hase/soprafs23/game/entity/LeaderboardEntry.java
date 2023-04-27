package ch.uzh.ifi.hase.soprafs23.game.entity;

import ch.uzh.ifi.hase.soprafs23.constant.PlayerColor;

/**
 * This class summarizes one entry in the leaderboard with Player - current score etc.
 */
public class LeaderboardEntry {

  private final Long playerId;
  private int currentScore;
  private String playerName;
  private String guessCountryCode;
  private int guess;
  private PlayerColor playerColor;

  public LeaderboardEntry(Long playerId, int currentScore, String playerName, PlayerColor playerColor) {
    this.playerId = playerId;
    this.currentScore = currentScore;
    this.playerName = playerName;
    this.playerColor = playerColor;
  }

  public Long getPlayerId() {
    return playerId;
  }
  public int getCurrentScore() {
    return currentScore;
  }



  public void addScore(int scoreToAdd) {
    this.currentScore+=scoreToAdd;
  }
  public void replaceScore(int scoreToReplace){this.currentScore=scoreToReplace;}

  public void setGuessCountryCode(String guessCountryCode) {this.guessCountryCode = guessCountryCode;}

  public void setGuess(int guess) {this.guess = guess;}

}
