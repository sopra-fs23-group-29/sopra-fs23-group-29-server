package ch.uzh.ifi.hase.soprafs23.game.websockets.dto.outgoing;

import ch.uzh.ifi.hase.soprafs23.constant.PlayerColor;
import ch.uzh.ifi.hase.soprafs23.game.entity.LeaderboardEntry;

import java.util.List;

public class MovePlayerDTO {
  private Long playerId;
  private PlayerColor playerColor;
  private int currentField;

  public MovePlayerDTO(Long playerId, PlayerColor playerColor, int currentField) {
    this.playerId = playerId;
    this.currentField = currentField;
    this.playerColor = playerColor;
  }

  public Long getPlayerId() {
    return playerId;
  }

  public PlayerColor getPlayerColor() {return playerColor;}

  public int getCurrentField() {
    return currentField;
  }
}
