package ch.uzh.ifi.hase.soprafs23.game.entity;

import ch.uzh.ifi.hase.soprafs23.constant.GameMode;
import ch.uzh.ifi.hase.soprafs23.constant.GameStatus;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.*;


/**
 * Internal Game Representation
 * This class represents a game
 * Every variable will be mapped into a database field with the @Column
 * annotation
 * - nullable = false -> this cannot be left empty
 * - unique = true -> this value must be unqiue across the database -> composes
 * the primary key
 */
@Entity
@Table(name = "GAME")
public class Game implements Serializable {

  private static final long serialVersionUID = 1L;
  @Id
  @GeneratedValue
  private Long id;

  @Column(nullable = false)
  private String gamename;

  @Column(nullable = false, unique = true)
  private String token;

  @Column(nullable = false)
  private GameStatus gamestatus;

  @Column(nullable = false)
  private GameMode gamemode;

  @Column(nullable = false)
  private int boardsize;

  @Column(nullable = false)
  private int maxduration;

  @Column(nullable = false)
  private int maxturns;

  // no cascade -> default nothing will be cascaded
  // cascade = CascadeType.PERSIS -> Player will be created when game is created with players
  @OneToMany(mappedBy = "game")
  private final List<Player> players = new ArrayList<Player>();

  // private Turn currentTurn;

  /**
   * The constructor always needs a list of players
   * @return
   */



  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getGamename() {
    return gamename;
  }

  public void setGamename(String gamename) {
    this.gamename = gamename;
  }

  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
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
  public List<Player> getPlayers() {return Collections.unmodifiableList(this.players);}

  public void addPlayer(Player player) {this.players.add(player);}

  public void removePlayer(Player player) {this.players.remove(player);}


}
