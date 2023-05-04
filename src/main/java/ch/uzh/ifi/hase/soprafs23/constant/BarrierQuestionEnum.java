package ch.uzh.ifi.hase.soprafs23.constant;

import java.util.Random;

public enum BarrierQuestionEnum {

  // todo: More categories need corresponding methods in Country!!

  NBORDERS("How many neighbouring countries does this country have?", false),
  LANDLOCKED("Is this country landlocked?", true);

  private final String question;
  private final boolean isBoolean;

  BarrierQuestionEnum(String question, boolean isBoolean) {
    this.question = question;
    this.isBoolean = isBoolean;
  }

  public String getQuestion() {
    return question;
  }
  public boolean getIsBoolean() {return isBoolean;}

  private static final Random random = new Random();

  public static BarrierQuestionEnum getRandom() {
//    return values()[random.nextInt(values().length)];
    // DEBUGGING: Set a fixed barrier question type
    // TODO: Remove
//     return LANDLOCKED;
    return NBORDERS;
  }
}
