package ch.uzh.ifi.hase.soprafs23.game.websockets.dto.incoming;

public class Answer {

  private String userToken;
  private String countryCode; // which country is the guess for?
  private int guess; // which rank was guessed for countryCode?

  public String getUserToken() {
    return userToken;
  }
  public void setUserToken(String userToken) {
    this.userToken = userToken;
  }
  public String getCountryCode() {
    return countryCode;
  }
  public void setCountryCode(String countryCode) {
    this.countryCode = countryCode;
  }
  public int getGuess() {
    return guess;
  }
  public void setGuess(int guess) {
    this.guess = guess;
  }

}
