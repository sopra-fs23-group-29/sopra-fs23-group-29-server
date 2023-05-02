package ch.uzh.ifi.hase.soprafs23.game.entity;

import ch.uzh.ifi.hase.soprafs23.constant.PlayerColor;

/**
 * This class summarizes one entry in the leaderboard with Player - current score etc.
 */
public class LeaderboardEntry {

  private final Long playerId;
  private int currentScore;
  private final String playerName;
  private final PlayerColor playerColor;
  private String guessCountryCode;
  private int guess;

  public LeaderboardEntry(Long playerId, int currentScore, String playerName, PlayerColor playerColor) {
    this.playerId = playerId;
    this.currentScore = currentScore;
    this.playerName = playerName;
    this.playerColor = playerColor;
  }

  public Long getPlayerId() {
    return playerId;
  }
  public String getPlayerName() {return playerName;}
  public PlayerColor getPlayerColor() {return playerColor;}
  public int getCurrentScore() {
    return currentScore;
  }
  public String getGuessCountryCode() {return this.guessCountryCode;}
  public void setGuessCountryCode(String guessCountryCode) {this.guessCountryCode = guessCountryCode;}
  public int getGuess() {return this.guess;}
  public void setGuess(int guess) {this.guess = guess;}



  public void addScore(int scoreToAdd) {
    this.currentScore+=scoreToAdd;
  }
  public void replaceScore(int scoreToReplace){this.currentScore=scoreToReplace;}

}
