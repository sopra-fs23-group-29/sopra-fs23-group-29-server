package ch.uzh.ifi.hase.soprafs23.game.rest.dto;

import ch.uzh.ifi.hase.soprafs23.constant.UserStatus;

public class UserGetDTO {

  private Long id;
  private String password;
  private String username;
  private String token;
  private UserStatus status;
  private String creationDate;
  private String birthday;

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
}
