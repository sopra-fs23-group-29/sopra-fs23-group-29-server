package ch.uzh.ifi.hase.soprafs23.game.entity;

import ch.uzh.ifi.hase.soprafs23.constant.GameMode;
import ch.uzh.ifi.hase.soprafs23.constant.GameStatus;
import ch.uzh.ifi.hase.soprafs23.game.repository.PlayerRepository;
import ch.uzh.ifi.hase.soprafs23.game.repository.UserRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.*;


/**
 * Internal Game Representation
 * This class represents a game
 */

public class Game {

  private UserRepository userRepository;
  private List<Player> players;
  private String gamename;
  private GameStatus gamestatus;
  private GameMode gamemode;
  private int boardsize;
  private int maxduration;
  private int maxturns;

  /**
   * The constructor always needs an owner
   * @param gamename name of the game
   * @param gamemode Which mode to play
   */
  public Game(String gamename, GameMode gamemode, UserRepository userRepository) {
    this.gamename = gamename;
    this.gamemode = gamemode;
    this.userRepository = userRepository;
  }

  // default no args constructor - needed for test
  public Game() {}


  public String getGamename() {
    return gamename;
  }

  public void setGamename(String gamename) {
    this.gamename = gamename;
  }

  public GameStatus getGamestatus() {
    return gamestatus;
  }

  public void setGamestatus(GameStatus gamestatus) {
    this.gamestatus = gamestatus;
  }

  public GameMode getGamemode() {
    return gamemode;
  }

  public void setGamemode(GameMode gamemode) {
    this.gamemode = gamemode;
  }

  public int getBoardsize() {
    return boardsize;
  }

  public void setBoardsize(int boardsize) {
    this.boardsize = boardsize;
  }

  public int getMaxduration() {
    return maxduration;
  }

  public void setMaxduration(int maxduration) {
    this.maxduration = maxduration;
  }

  public int getMaxturns() {
    return maxturns;
  }

  public void setMaxturns(int maxturns) {
    this.maxturns = maxturns;
  }



  /**
   * Returns the list of players as an unmodifiable list of the current players in the game.
   * Modifications to the list of players should only be done through addPlayer and removePlayer
   *
   * @return  An unmodifiable list object containing all current players of the game
   */
  public List<Player> getPlayersId() {return Collections.unmodifiableList(this.players);}

  /**
   * Add a Player to the list of players of the game
   * Do nothing if the Player instance is already contained
   * @param player Player to add
   */
  public void addPlayer(Player player) {
    if (!this.players.contains(player)) {
      this.players.add(player);
    }
  }

  /**
   * Remove the given player from the list of players
   * @param player Player object to remove
   * @return True if a matching instance has been found, False otherwise
   */
  public boolean removePlayer(Player player) {return this.players.remove(player);}



  public String toString() {
    return "Game: " + this.getGamename();
  }


}
