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
  @GeneratedValue
  private Long id;

  @Column(nullable = false, unique = true)
  private String playername;

  @Column(nullable = false, unique = true)
  private String token;

  @Column(nullable = false)
  private PlayerColor playercolor;

//  @ManyToOne
//  private Game game;
//
//  @ManyToOne
//  private Lobby lobby;

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

//  public Game getGame() {
//    return game;
//  }
//
//  public void setGame(Game game) {
//    this.game = game;
//  }
//
//  public Lobby getLobby() {
//    return lobby;
//  }
//
//  public void setLobby(Lobby lobby) {
//    this.lobby = lobby;
//  }



  public String toString() {
    return "Player: " + this.getPlayername() + " ID: " + this.getId();
  }

}
