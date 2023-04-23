package ch.uzh.ifi.hase.soprafs23.game.entity;

import java.util.ArrayList;
import java.util.List;

import ch.uzh.ifi.hase.soprafs23.constant.PlayerColor;

/**
 * A leaderboard contains a mapping from PlayerId to number of fields
 */
public class Leaderboard {

  private List<LeaderboardEntry> entries; // Each entry is one player with scores

  public Leaderboard() {
    this.entries = new ArrayList<>();
  }

  /**
   * Copy constructor to copy an existing leaderboard
   * @param leaderboardToCopy Leaderboard to copy from
   */
  public Leaderboard(Leaderboard leaderboardToCopy) {
    this.entries = new ArrayList<>();
    this.entries.addAll(leaderboardToCopy.entries);
  }

  public List<LeaderboardEntry> getEntries() {
    return entries;
  }

  /**
   * Fetch an entry with key playerId
   * Throws IllegalArgumentException if playerId not a key in leaderboard
   * @return LeaderboardEntry
   */
  public LeaderboardEntry getEntry(Long playerId) {
    boolean playerFound = false;
    int i = 0;
    for (LeaderboardEntry entry : entries) {
      if (entry.getPlayerId().equals(playerId)) {
        playerFound = true;
        break;
      }
      i++;
    }
    if (!playerFound) {
      throw new IllegalArgumentException("Player %s not found".formatted(playerId));
    }
    return entries.get(i);
  }

  /**
   * Put a new player into the leaderboard with a value of 0
   * Throws exception if the playerId already exists in the leaderboard
   */
  public void putNewPlayer(Long newPlayerId, String playerUsername, PlayerColor playerColor) throws IllegalArgumentException {
    for (LeaderboardEntry entry : entries) {
      if (entry.getPlayerId().equals(newPlayerId)) {
        throw new IllegalArgumentException("Player ID %s already exists".formatted(newPlayerId));
      }
    }
    LeaderboardEntry newEntry = new LeaderboardEntry(newPlayerId, 0, playerUsername, playerColor);
    entries.add(newEntry);
  }

  /**
   * Increase the value of an entry in the leaderboard by the given value
   * If the key playerId is not found throw error
   *
   * @param playerId ID of the player in the leaderboard to update
   */
  public void addToEntry(Long playerId, int addScore) throws IllegalArgumentException {
    boolean playerFound = false;
    int i = 0;
    for (LeaderboardEntry entry : entries) {
      if (entry.getPlayerId().equals(playerId)) {
        playerFound = true;
        break;
      }
      i++;
    }
    if (!playerFound) {
      throw new IllegalArgumentException("Player %s not found".formatted(playerId));
    }

    // Fetch the leaderboard entry and add the score
    entries.get(i).addScore(addScore);
  }

  /**
   * Replace the value of an entry in the leaderboard by the given value
   * If the key playerId is not found throw error
   *
   * @param playerId ID of the player in the leaderboard to update
   */
  public void replaceEntry(Long playerId, int replaceScore, String guessCountyCode, int guess) throws IllegalArgumentException {
    boolean playerFound = false;
    int i = 0;
    for (LeaderboardEntry entry : entries) {
      if (entry.getPlayerId().equals(playerId)) {
        playerFound = true;
        break;
      }
      i++;
    }
    if (!playerFound) {
      throw new IllegalArgumentException("Player %s not found".formatted(playerId));
    }

    // Fetch the leaderboard entry and add the score
    entries.get(i).replaceScore(replaceScore);
    entries.get(i).setGuessCountryCode(guessCountyCode);
    entries.get(i).setGuess(guess);
  }

  /**
   * Sync a leaderboard, keep only those playerIds in the argument and delete all others
   */
  public void sync(List<Long> playerIdsToKeep) {

    List<LeaderboardEntry> newPlayers = new ArrayList<>();

    for (LeaderboardEntry entry : entries) {
      if (playerIdsToKeep.contains(entry.getPlayerId())) {
        newPlayers.add(entry);
      }
    }

    // set the new player
    this.entries = newPlayers;
  }


}
