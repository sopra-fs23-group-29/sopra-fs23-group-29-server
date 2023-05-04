package ch.uzh.ifi.hase.soprafs23.game.websockets.dto.outgoing;

import ch.uzh.ifi.hase.soprafs23.constant.BoardSize;
import ch.uzh.ifi.hase.soprafs23.constant.GameMode;
import ch.uzh.ifi.hase.soprafs23.constant.GameStatus;
import ch.uzh.ifi.hase.soprafs23.constant.MaxDuration;
import ch.uzh.ifi.hase.soprafs23.game.entity.Game;
import ch.uzh.ifi.hase.soprafs23.game.entity.Leaderboard;
import ch.uzh.ifi.hase.soprafs23.game.entity.Player;
import ch.uzh.ifi.hase.soprafs23.game.questions.restCountry.BarrierQuestion;

import java.util.List;

/**
 * Create from a Game instance, copy all fields necessary without the repository.
 * Because the repository cannot be serialized to JSON
 */
public class GameUpdateDTO {

    private final List<Player> players;
    private int turnNumber;
    private BarrierQuestion currentBarrierQuestion;
    private List<Integer> resolvedBarriers; // keep track of which barriers have been resolved already
    private Long gameId;
    private String gameName;
    private GameStatus gameStatus;
    private GameMode gameMode;
    private Leaderboard leaderboard;
    private Leaderboard barrierLeaderboard;
    private boolean joinable;
    private BoardSize boardSize;
    private int boardSizeInt;
    private MaxDuration maxDuration;
    private int maxDurationInt;
    private int maxTurns;

    public GameUpdateDTO(Game game) {
        this.players = game.getPlayersView();
        this.turnNumber = game.getTurnNumber();
        this.currentBarrierQuestion = game.getCurrentBarrierQuestion();
        this.resolvedBarriers = game.getResolvedBarriers();
        this.gameId = game.getGameId();
        this.gameName = game.getGameName();
        this.gameStatus = game.getGameStatus();
        this.gameMode = game.getGameMode();
        this.leaderboard = game.getLeaderboard();
        this.barrierLeaderboard = game.getBarrierLeaderboard();
        this.boardSize = game.getBoardSize();
        this.boardSizeInt = game.getBoardSize().getBoardSize();
        this.maxDuration = game.getMaxDuration();
        this.maxDurationInt = game.getMaxDuration().getMaxDuration();
        this.maxTurns = game.getMaxTurns();
        this.joinable = game.getJoinable();
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
    public int getTurnNumber() {
        return turnNumber;
    }
    public void setTurnNumber(int turnNumber) {
        this.turnNumber = turnNumber;
    }
    public Leaderboard getLeaderboard() {
        return leaderboard;
    }
    public void setLeaderboard(Leaderboard leaderboard) {
        this.leaderboard = leaderboard;
    }
    public Leaderboard getBarrierLeaderboard() {
        return barrierLeaderboard;
    }
    public void setBarrierLeaderboard(Leaderboard barrierLeaderboard) {
        this.barrierLeaderboard = barrierLeaderboard;
    }
    public BarrierQuestion getCurrentBarrierQuestion() {
        return currentBarrierQuestion;
    }
    public void setCurrentBarrierQuestion(BarrierQuestion currentBarrierQuestion) {
        this.currentBarrierQuestion = currentBarrierQuestion;
    }
    public List<Integer> getResolvedBarriers() {
        return resolvedBarriers;
    }
    public void setResolvedBarriers(List<Integer> resolvedBarriers) {
        this.resolvedBarriers = resolvedBarriers;
    }
    public BoardSize getBoardSize() {
        return boardSize;
    }
    public void setBoardSize(BoardSize boardSize) {
        this.boardSize = boardSize;
    }
    public int getBoardSizeInt() {
        return boardSizeInt;
    }
    public void setBoardSizeInt(int boardSizeInt) {
        this.boardSizeInt = boardSizeInt;
    }
    public MaxDuration getMaxDuration() {
        return maxDuration;
    }
    public void setMaxDuration(MaxDuration maxDuration) {
        this.maxDuration = maxDuration;
    }
    public int getMaxDurationInt() {
        return maxDurationInt;
    }
    public void setMaxDurationInt(int maxDurationInt) {
        this.maxDurationInt = maxDurationInt;
    }
    public int getMaxTurns() {
        return maxTurns;
    }
    public void setMaxTurns(int maxTurns) {
        this.maxTurns = maxTurns;
    }
    public boolean getJoinable() {return this.joinable;}
    public void setJoinable(boolean joinable) {this.joinable = joinable;}

}

