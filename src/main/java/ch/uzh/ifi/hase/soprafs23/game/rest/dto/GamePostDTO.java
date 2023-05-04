package ch.uzh.ifi.hase.soprafs23.game.rest.dto;

import ch.uzh.ifi.hase.soprafs23.constant.BoardSize;
import ch.uzh.ifi.hase.soprafs23.constant.GameMode;
import ch.uzh.ifi.hase.soprafs23.constant.MaxDuration;

public class GamePostDTO {
    private String gameName;
    private GameMode gameMode;
    private BoardSize boardSize;
    private MaxDuration maxDuration;

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
    public BoardSize getBoardSize() {
        return boardSize;
    }
    public void setBoardSize(BoardSize boardSize) {
        this.boardSize = boardSize;
    }
    public MaxDuration getMaxDuration() {
        return maxDuration;
    }
    public void setMaxDuration(MaxDuration maxDuration) {
        this.maxDuration = maxDuration;
    }
}
