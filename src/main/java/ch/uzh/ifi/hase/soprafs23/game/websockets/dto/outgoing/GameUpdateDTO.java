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
    private boolean barriersEnabled; // are barriers enabled or not? if not hitsBarrier and hitsResolvedBarrier never return true;
    private Leaderboard leaderboard;
    private Leaderboard barrierLeaderboard;
    private boolean joinable;
    private BoardSize boardSize;
    private int boardSizeInt;
    private MaxDuration maxDuration;
    private int maxDurationInt;
    private int playingTimeInSeconds;

    public GameUpdateDTO(Game game) {
        this.players = game.getPlayersView();
        this.turnNumber = game.getTurnNumber();
        this.currentBarrierQuestion = game.getCurrentBarrierQuestion();
        this.resolvedBarriers = game.getResolvedBarriers();
        this.gameId = game.getGameId();
        this.gameName = game.getGameName();
        this.gameStatus = game.getGameStatus();
        this.gameMode = game.getGameMode();
        this.barriersEnabled = game.getBarriersEnabled();
        this.leaderboard = game.getLeaderboard();
        this.barrierLeaderboard = game.getBarrierLeaderboard();
        this.boardSize = game.getBoardSize();
        this.boardSizeInt = game.getBoardSize().getBoardSize();
        this.maxDuration = game.getMaxDuration();
        this.maxDurationInt = game.getMaxDuration().getMaxDuration();
        this.joinable = game.getJoinable();
        this.playingTimeInSeconds = game.getPlayingTimeInSeconds();
    }

    public void setGameId(Long gameId) {this.gameId = gameId;}
    public Long getGameId() {return gameId;}


}

