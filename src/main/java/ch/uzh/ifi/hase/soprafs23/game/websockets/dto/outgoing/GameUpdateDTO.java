package ch.uzh.ifi.hase.soprafs23.game.websockets.dto.outgoing;

import ch.uzh.ifi.hase.soprafs23.constant.GameMode;
import ch.uzh.ifi.hase.soprafs23.constant.GameStatus;
import ch.uzh.ifi.hase.soprafs23.game.entity.Game;
import ch.uzh.ifi.hase.soprafs23.game.entity.Player;
import ch.uzh.ifi.hase.soprafs23.game.repository.PlayerRepository;

import java.util.List;

/**
 * Create from a Game instance, copy all fields necessary without the repository.
 * Because the repository cannot be serialized to JSON
 */
public class GameUpdateDTO {

    private List<Player> players;
    private Long gameId;
    private String gameName;
    private GameStatus gameStatus;
    private GameMode gameMode;
    private int boardSize;
    private int maxDuration;
    private int maxTurns;

    public GameUpdateDTO(Game game) {
        this.players = game.getPlayersView();
        this.gameId = game.getGameId();
        this.gameName = game.getGameName();
        this.gameStatus = game.getGameStatus();
        this.gameMode = game.getGameMode();
        this.boardSize = game.getBoardSize();
        this.maxDuration = game.getMaxDuration();
        this.maxTurns = game.getMaxTurns();
    }

    public void setGameId(Long gameId) {this.gameId = gameId;}
    public Long getGameId() {return gameId;}
    public String getGameName() {
        return gameName;
    }
    public void setGameName(String gameName) {
        this.gameName = gameName;
    }
    public GameStatus getGameStatus() {
        return gameStatus;
    }
    public void setGameStatus(GameStatus gameStatus) {
        this.gameStatus = gameStatus;
    }
    public GameMode getGameMode() {
        return gameMode;
    }
    public void setGameMode(GameMode gameMode) {
        this.gameMode = gameMode;
    }
    public int getBoardSize() {
        return boardSize;
    }
    public void setBoardSize(int boardSize) {
        this.boardSize = boardSize;
    }
    public int getMaxDuration() {
        return maxDuration;
    }
    public void setMaxDuration(int maxDuration) {
        this.maxDuration = maxDuration;
    }
    public int getMaxTurns() {
        return maxTurns;
    }
    public void setMaxTurns(int maxTurns) {
        this.maxTurns = maxTurns;
    }
}
