package ch.uzh.ifi.hase.soprafs23.game.entity;

import ch.uzh.ifi.hase.soprafs23.constant.GameMode;
import ch.uzh.ifi.hase.soprafs23.constant.GameStatus;
import ch.uzh.ifi.hase.soprafs23.game.repository.UserRepository;

import java.util.Collections;
import java.util.*;


/**
 * Internal Game Representation
 * This class represents a game
 */

public class Game {

  public static final int MAXPLAYERS = 6;

  // todo: players should always update from playerRepository, never keep that internal!
  // getPlayers should be a method calling playerRepository
  // add/remove players should not exist, only go through playerRepository

  private List<Player> players;
  private Long gameId;
  private String gameName;
  private GameStatus gameStatus;
  private GameMode gameMode;
  private int boardSize;
  private int maxDuration;
  private int maxTurns;

  /**
   * The constructor always needs an owner
   * @param gameName name of the game
   * @param gameMode Which mode to play
   */
  public Game(Long gameId, String gameName, GameMode gameMode) {
    this.gameId = gameId;
    this.gameName = gameName;
    this.gameMode = gameMode;
  }

  // default no args constructor - needed for test
  public Game() {}

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


  public void updatePlayers() {
    
  }





  public String toString() {
    return "Game: " + this.getGameName();
  }


}
