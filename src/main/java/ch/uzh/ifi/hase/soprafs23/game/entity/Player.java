package ch.uzh.ifi.hase.soprafs23.game.entity;

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
  private String userToken;

  @Column
  private Long gameId;

  @Column(nullable = false)
  private String playerName;

  @Column(nullable = false, unique = true)
  private String token;

  @Column(nullable = false)
  private PlayerColor playerColor;

  @Column(nullable = false)
  private boolean isHost;






  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getUserToken() {return userToken;}

  public void setUserToken(String userToken) {
    this.userToken = userToken;
  }

  public Long getGameId() {
    return gameId;
  }

  public void setGameId(Long gameId) {
    this.gameId = gameId;
  }

  public String getPlayerName() {
    return playerName;
  }

  public void setPlayerName(String playerName) {
    this.playerName = playerName;
  }

  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }

  public PlayerColor getPlayerColor() {
    return playerColor;
  }

  public void setPlayerColor(PlayerColor playerColor) {
    this.playerColor = playerColor;
  }

  public boolean getIsHost() {return this.isHost;}

  public void setIsHost(boolean host) {
    isHost = host;
  }

  public String toString() {
    return "Player: " + this.getPlayerName() + " ID: " + this.getId() + " Token:" + this.getToken();
  }

}
