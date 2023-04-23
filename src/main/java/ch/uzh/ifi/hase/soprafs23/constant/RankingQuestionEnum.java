package ch.uzh.ifi.hase.soprafs23.constant;

import java.util.Random;

public enum RankingQuestionEnum {
    AREA("Which of these countries has the largest area?", "Area [km^2]"),
    POPULATION("Which of these countries has the largest population?", "Population"),
    GINI("Which of these countries has the largest gini-coefficient?", "Gini-coefficient"),
    POPULATION_DENSITY("Which of these countries has the highest population density?", "Population density [people/km^2]"),
    CAPITAL_LATITUDE("Which of these countries capital is located the furthest north?", "Capital latitude [°]");

    private final String value;
    private final String value_short;

    RankingQuestionEnum(String value, String value_short) {
        this.value = value;
        this.value_short = value_short;
    }

    public String getQuestion() {
        return value;
    }

    public String getQuestionShort() {
        return value_short;
    }

    private static final Random random = new Random();

    public static RankingQuestionEnum getRandom() {
        return values()[random.nextInt(values().length)];
    }
}
