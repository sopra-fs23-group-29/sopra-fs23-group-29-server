package ch.uzh.ifi.hase.soprafs23.game.websockets.dto.outgoing;

public class DummyDTO {

  private String dummyMessage = "this is a dummy message";

  public void setDummyMessage(String dummyMessage) {
    this.dummyMessage = dummyMessage;
  }

  public String getDummyMessage() {
    return dummyMessage;
  }

  @Override
  public String toString() {
    return dummyMessage;
  }
}
