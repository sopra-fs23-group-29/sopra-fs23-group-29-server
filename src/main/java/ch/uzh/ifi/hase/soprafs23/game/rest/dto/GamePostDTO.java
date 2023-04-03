package ch.uzh.ifi.hase.soprafs23.game.rest.dto;

import ch.uzh.ifi.hase.soprafs23.constant.GameMode;

public class GamePostDTO {
    private String gameName;
    private GameMode gameMode;

    public void setGameName(String gameName) {
        this.gameName = gameName;
    }

    public String getGameName() {
        return gameName;
    }

    public GameMode getGameMode() {
        return gameMode;
    }

    public void setGameMode(GameMode gameMode) {
        this.gameMode = gameMode;
    }
}
