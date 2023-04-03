package ch.uzh.ifi.hase.soprafs23.game.websockets.dto.outgoing;

import ch.uzh.ifi.hase.soprafs23.constant.GameMode;

public class GameJoinedDTO {
    private String gameName;
    private GameMode gameMode;

    public void setGameName(String gameName) {
        this.gameName = gameName;
    }

    public String getGameName() {
        return gameName;
    }

    public void setGameMode(GameMode gameMode) {
        this.gameMode = gameMode;
    }

    public GameMode getGameMode() {
        return gameMode;
    }
}

