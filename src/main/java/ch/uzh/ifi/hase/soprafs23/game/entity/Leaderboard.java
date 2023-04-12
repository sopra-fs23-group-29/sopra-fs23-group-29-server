package ch.uzh.ifi.hase.soprafs23.game.entity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A leaderboard contains a mapping from PlayerId to number of fields
 */
public class Leaderboard {

  private HashMap<Long, Integer> leaderboard;

  public Leaderboard() {
    this.leaderboard = new HashMap<>();
  }

  /**
   * Copy constructor to copy an existing leaderboard
   * @param leaderboardToCopy Leaderborad to copy from
   */
  public Leaderboard(Leaderboard leaderboardToCopy) {
    this.leaderboard = new HashMap<>();
    this.leaderboard.putAll(leaderboardToCopy.leaderboard);
  }

  /**
   * Put a new player into the leaderboard with a value of 0
   * Throws exception if the playerId already exists in the leaderboard
   */
  public void putNewPlayer(Long playerId) throws IllegalArgumentException {
    if (leaderboard.containsKey(playerId)) {
      throw new IllegalArgumentException("playerId %s already in leaderboard".formatted(playerId));
    }
    leaderboard.put(playerId, 0);
  }

  /**
   * Increase the value if an entry in the leaderboard by 1
   * If the key playerId is not found throw error
   *
   * @param playerId ID of the player in the leaderboard to update
   */
  public void updateByOne(Long playerId) throws IllegalArgumentException {
    if (!leaderboard.containsKey(playerId)) {
      throw new IllegalArgumentException("playerId %s not found in leaderboard".formatted(playerId));
    }
    leaderboard.put(playerId, leaderboard.get(playerId)+1);
  }

  /**
   * Sync a leaderboard, keep only those keys in the argument and delete all others
   */
  public void sync(List<Long> keysToKeep) {
    for (Map.Entry<Long,Integer> entry : leaderboard.entrySet()) {
      if (!keysToKeep.contains(entry.getKey())) {
        leaderboard.remove(entry.getKey());
      }
    }
  }

}
