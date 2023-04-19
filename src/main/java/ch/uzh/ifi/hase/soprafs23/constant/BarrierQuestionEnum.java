package ch.uzh.ifi.hase.soprafs23.constant;

import java.util.Random;

public enum BarrierQuestionEnum {

  // todo: More categories need corresponding methods in Country!!

  NBORDERS("How many neighbouring countries does this country have?");

  private final String question;

  BarrierQuestionEnum(String question) {
    this.question = question;
  }

  public String getQuestion() {
    return question;
  }

  private static final Random random = new Random();

  public static BarrierQuestionEnum getRandom() {
    return values()[random.nextInt(values().length)];
  }
}
