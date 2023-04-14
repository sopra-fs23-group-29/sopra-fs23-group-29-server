package ch.uzh.ifi.hase.soprafs23.constant;

import java.util.Random;

public enum RankingQuestionEnum {
    AREA("Which of these countries has the largest area?"),
    POPULATION("Which of these countries has the largest population?"),
    GINI("Which of these countries has the largest gini-coefficient?"),
    POPULATION_DENSITY("Which of these countries has the highest population density?"),
    CAPITAL_LATITUDE("Which of these countries capital is located the furthest north?");

    private final String value;

    RankingQuestionEnum(String value) {
        this.value = value;
    }

    public String getQuestion() {
        return value;
    }

    private static final Random random = new Random();

    public static RankingQuestionEnum getRandom() {
        return values()[random.nextInt(values().length)];
    }
}
