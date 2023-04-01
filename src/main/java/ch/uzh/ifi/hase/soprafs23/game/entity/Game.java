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
  @GeneratedValue(strategy = GenerationType.IDENTITY)
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

  /**
   * Cascade: Game has ALL. Meaning when the Game is persisted, the players are persisted if not exist
   * When the game is deleted, all the players are deleted
   */
  @OneToMany(mappedBy = "game", cascade = CascadeType.ALL)
  private final List<Player> players = new ArrayList<>();

  @OneToOne
  private Player owner;

  // private Turn currentTurn;

  /**
   * The constructor always needs an owner
   * @param gamename name of the game
   * @param token token of the game
   * @param gamemode Which mode to play
   * @param owner The token of the player owning the game
   */
  public Game(String gamename, String token, GameMode gamemode, Player owner) {
    this.gamename = gamename;
    this.token = token;
    this.gamemode = gamemode;
    this.owner = owner;

    // Add the owner to the list of players
    addPlayer(owner);

    // When a game is created, set the GameStatus to INLOBBY
    this.gamestatus = GameStatus.INLOBBY;
  }



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

  public void setOwner(Player owner) {
    this.owner = owner;
  }

  public Player getOwner() {
    return owner;
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

  /**
   * Add a Player to the list of players of the game
   * Do nothing if the Player instance is already contained
   * @param player Player to add
   */
  public void addPlayer(Player player) {
    if (!this.players.contains(player)) {
      this.players.add(player);
      // To keep consistency, automatically set the game for the player
      player.setGame(this);
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
