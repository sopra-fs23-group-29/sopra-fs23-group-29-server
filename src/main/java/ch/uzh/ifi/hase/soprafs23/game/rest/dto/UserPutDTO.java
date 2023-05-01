package ch.uzh.ifi.hase.soprafs23.game.rest.dto;

public class UserPutDTO {

  private String username;
  private String birthday;
  private String password;
  private String cioc;


  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getBirthday() {return birthday;}

  public void setBirthday(String birthday) {this.birthday = birthday;}

  public String getPassword() {return password;}

  public void setPassword(String password) {this.password = password;}

  public String getCioc() {return cioc;}

  public void setCioc(String cioc) {this.cioc = cioc;}
}
