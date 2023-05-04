package ch.uzh.ifi.hase.soprafs23.game.websockets.dto.incoming;

public class BarrierAnswer {

  private String userToken;
  private String guess; // answer to the barrier question

  public String getUserToken() {
    return userToken;
  }
  public void setUserToken(String userToken) {
    this.userToken = userToken;
  }
  public String getGuess() {
    return guess;
  }
  public void setGuess(String guess) {
    this.guess = guess;
  }

}
