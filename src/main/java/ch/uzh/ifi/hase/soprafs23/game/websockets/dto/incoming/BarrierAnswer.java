package ch.uzh.ifi.hase.soprafs23.game.websockets.dto.incoming;

public class BarrierAnswer {

  private String userToken;
  private int guess; // answer to the barrier question

  public String getUserToken() {
    return userToken;
  }
  public void setUserToken(String userToken) {
    this.userToken = userToken;
  }
  public int getGuess() {
    return guess;
  }
  public void setGuess(int guess) {
    this.guess = guess;
  }

}
