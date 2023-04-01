package ch.uzh.ifi.hase.soprafs23.game.entity;

import ch.uzh.ifi.hase.soprafs23.constant.GameMode;
import ch.uzh.ifi.hase.soprafs23.constant.GameStatus;
import ch.uzh.ifi.hase.soprafs23.constant.PlayerColor;

import javax.persistence.*;
import java.io.Serializable;


/**
 * Internal Player Representation
 * This class represents a player in a game/lobby
 * Every variable will be mapped into a database field with the @Column
 * annotation
 * - nullable = false -> this cannot be left empty
 * - unique = true -> this value must be unqiue across the database -> composes
 * the primary key
 */
@Entity
@Table(name = "PLAYER")
public class Player implements Serializable {

  private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private String playername;

  @Column(nullable = false, unique = true)
  private String token;

  @Column(nullable = false)
  private PlayerColor playercolor;

  @Column(nullable = false, unique = true)
  private String userToken;

  /**
   * Cascade: Player has CascadeType.PERSIST.
   * When the Player is persisted, the Game is persisted if not exist
   * However, the game lives on if all Players are deleted
   */
  @ManyToOne(cascade = CascadeType.PERSIST)
  private Game game;



  /**
   * The constructor always needs a playername, a token and a user reference
   * A game reference is not needed
   * @param playername Name of the player
   * @param token Token of the player
   * @param userToken The userToken this player belongs to
   */
  public Player(String playername, String token, String userToken) {
    this.playername = playername;
    this.token = token;
    this.userToken = userToken;
  }






  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getPlayername() {
    return playername;
  }

  public void setPlayername(String playername) {
    this.playername = playername;
  }

  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }

  public PlayerColor getPlayercolor() {
    return playercolor;
  }

  public void setPlayercolor(PlayerColor playercolor) {
    this.playercolor = playercolor;
  }

  public Game getGame() {
    return game;
  }

  public void setGame(Game game) {
    this.game = game;
    // to keep it consistent, also add the player to the game
    game.addPlayer(this);
  }

  public String getUserToken() {return userToken;}

  public void setUserToken(String userToken) {
    this.userToken = userToken;
  }


  public String toString() {
    return "Player: " + this.getPlayername() + " ID: " + this.getId() + " Token:" + this.getToken() + " GameID:" + this.getGame();
  }

}
