package ch.uzh.ifi.hase.soprafs23.game.websockets.dto.outgoing;

import ch.uzh.ifi.hase.soprafs23.game.entity.LeaderboardEntry;

import java.util.List;

public class MovePlayerDTO {
  private Long playerId;
  private int currentField;

  public MovePlayerDTO(Long playerId, int currentField) {
    this.playerId = playerId;
    this.currentField = currentField;
  }

  public Long getPlayerId() {
    return playerId;
  }

  public int getCurrentField() {
    return currentField;
  }
}
