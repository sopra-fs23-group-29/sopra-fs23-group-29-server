package ch.uzh.ifi.hase.soprafs23.game.websockets.dto.outgoing;

public class PlayerJoinedDTO {
    private String playerName;

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }
}
