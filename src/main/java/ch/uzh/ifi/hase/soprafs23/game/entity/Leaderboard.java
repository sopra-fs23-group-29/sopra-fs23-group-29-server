package ch.uzh.ifi.hase.soprafs23.game.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A leaderboard contains a mapping from PlayerId to number of fields
 */
public class Leaderboard {

  private List<LeaderboardEntry> players; // Each entry is one player with scores

  public Leaderboard() {
    this.players = new ArrayList<>();
  }

  /**
   * Copy constructor to copy an existing leaderboard
   * @param leaderboardToCopy Leaderboard to copy from
   */
  public Leaderboard(Leaderboard leaderboardToCopy) {
    this.players = new ArrayList<>();
    this.players.addAll(leaderboardToCopy.players);
  }

  public List<LeaderboardEntry> getPlayers() {
    return players;
  }

  /**
   * Put a new player into the leaderboard with a value of 0
   * Throws exception if the playerId already exists in the leaderboard
   */
  public void putNewPlayer(Long newPlayerId) throws IllegalArgumentException {
    for (LeaderboardEntry entry : players) {
      if (entry.getPlayerId() == newPlayerId) {
        throw new IllegalArgumentException("Player ID %s already exists".formatted(newPlayerId));
      }
    }
    LeaderboardEntry newEntry = new LeaderboardEntry(newPlayerId, 0);
    players.add(newEntry);
  }

  /**
   * Increase the value if an entry in the leaderboard by the given value
   * If the key playerId is not found throw error
   *
   * @param playerId ID of the player in the leaderboard to update
   */
  public void updateEntry(Long playerId, int addScore) throws IllegalArgumentException {
    boolean playerFound = false;
    int i = 0;
    for (LeaderboardEntry entry : players) {
      if (entry.getPlayerId() == playerId) {
        playerFound = true;
        break;
      }
      i++;
    }
    if (!playerFound) {
      throw new IllegalArgumentException("Player %s not found".formatted(playerId));
    }

    // Fetch the leaderboard entry and add the score
    players.get(i).addScore(addScore);
  }

  /**
   * Sync a leaderboard, keep only those playerIds in the argument and delete all others
   */
  public void sync(List<Long> playerIdsToKeep) {

    List<LeaderboardEntry> newPlayers = new ArrayList<>();

    for (LeaderboardEntry entry : players) {
      if (playerIdsToKeep.contains(entry.getPlayerId())) {
        newPlayers.add(entry);
      }
    }

    // set the new player
    this.players = newPlayers;
  }


}
