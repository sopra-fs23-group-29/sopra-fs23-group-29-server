package ch.uzh.ifi.hase.soprafs23.game.rest.dto;

public class UserPostDTO {

  private String username;
  private String password;
  private String token;


  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {return password;}

  public void setPassword(String password) {this.password = password;}

  public String getToken() {return token;}
  public void setToken(String token) {this.token = token;}

}
