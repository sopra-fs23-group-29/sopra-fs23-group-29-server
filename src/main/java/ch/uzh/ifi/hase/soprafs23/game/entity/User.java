package ch.uzh.ifi.hase.soprafs23.game.entity;

import ch.uzh.ifi.hase.soprafs23.constant.UserStatus;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Internal User Representation
 * This class composes the internal representation of the user and defines how
 * the user is stored in the database.
 * Every variable will be mapped into a database field with the @Column
 * annotation
 * - nullable = false -> this cannot be left empty
 * - unique = true -> this value must be unqiue across the database -> composes
 * the primary key
 */
@Entity
@Table(name = "USER")
public class User implements Serializable {

  private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String password;

  @Column(nullable = false, unique = true)
  private String username;

  @Column(nullable = false, unique = true)
  private String token;

  @Column(nullable = false)
  private UserStatus status;

  @Column(nullable = true) // is nullable
  private String creationDate;

  @Column(nullable = true) // is nullable
  private String birthday;

  @Column(nullable = true)
  private String flagURL;

  @Column(nullable = true)
  private String cioc;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getPassword() {return password;}

  public void setPassword(String password) {this.password = password;}

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }

  public UserStatus getStatus() {
    return status;
  }

  public void setStatus(UserStatus status) {
    this.status = status;
  }

  public String getCreationDate() {
    return creationDate;
  }

  public void setCreationDate(String creationDate) {
    this.creationDate = creationDate;
  }

  public String getBirthday() {
    return birthday;
  }

  public void setBirthday(String birthday) {this.birthday = birthday;}

  public String getFlagURL() {
    return flagURL;
  }

  public void setFlagURL(String flagURL) {
    this.flagURL = flagURL;
  }

  public String getCioc() {
    return cioc;
  }

  public void setCioc(String cioc) {
    this.cioc = cioc;
  }
  @Override
  public String toString() {
    return "{\"id\": \"" + id+ "\"" +
            "\"username\": \"" + username + "\"" +
            "\"status\": \"" + status + "\"" +
            "\"creation date\": \"" + creationDate + "\"" +
            "\"birthday\": \"" + birthday + "\"" +
            "\"flagURL\": \"" + flagURL + "\"" +
            "}";
  }
}
