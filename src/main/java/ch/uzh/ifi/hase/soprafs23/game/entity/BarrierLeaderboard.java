package ch.uzh.ifi.hase.soprafs23.game.entity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Same as the Leaderboard, this class keeps track of which Player (by ID)
 * has how many BarrierQuestion points
 */
public class BarrierLeaderboard {

  private HashMap<Long, Integer> barrierLeaderboard;

  public BarrierLeaderboard() {
    this.barrierLeaderboard = new HashMap<>();
  }

  /**
   * Copy constructor to copy an existing barrierLeaderboard
   * @param barrierLeaderboardToCopy BarrierLeaderboard to copy from
   */
  public BarrierLeaderboard(BarrierLeaderboard barrierLeaderboardToCopy) {
    this.barrierLeaderboard = new HashMap<>();
    this.barrierLeaderboard.putAll(barrierLeaderboardToCopy.barrierLeaderboard);
  }

  /**
   * Put a new player into the barrierLeaderboard with a value of 0
   * Throws exception if the playerId already exists in the barrierLeaderboard
   */
  public void putNewPlayer(Long playerId) throws IllegalArgumentException {
    if (barrierLeaderboard.containsKey(playerId)) {
      throw new IllegalArgumentException("playerId %s already in barrierLeaderboard".formatted(playerId));
    }
    barrierLeaderboard.put(playerId, 0);
  }

  /**
   * Increase the value if an entry in the barrierLeaderboard by 1
   * If the key playerId is not found throw error
   *
   * @param playerId ID of the player in the barrierLeaderboard to update
   */
  public void updateByOne(Long playerId) throws IllegalArgumentException {
    if (!barrierLeaderboard.containsKey(playerId)) {
      throw new IllegalArgumentException("playerId %s not found in barrierLeaderboard".formatted(playerId));
    }
    barrierLeaderboard.put(playerId, barrierLeaderboard.get(playerId)+1);
  }

  /**
   * Sync a barrierLeaderboard, keep only those keys in the argument and delete all others
   */
  public void sync(List<Long> keysToKeep) {
    for (Map.Entry<Long,Integer> entry : barrierLeaderboard.entrySet()) {
      if (!keysToKeep.contains(entry.getKey())) {
        barrierLeaderboard.remove(entry.getKey());
      }
    }
  }

}
