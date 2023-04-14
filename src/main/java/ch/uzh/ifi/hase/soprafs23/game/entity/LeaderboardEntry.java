package ch.uzh.ifi.hase.soprafs23.game.entity;

/**
 * This class summarizes one entry in the leaderboard with Player - current score etc.
 */
public class LeaderboardEntry {

  private Long playerId;
  private int currentScore;

  public LeaderboardEntry(Long playerId, int currentScore) {
    this.playerId = playerId;
    this.currentScore = currentScore;
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

}