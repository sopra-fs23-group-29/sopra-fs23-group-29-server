package ch.uzh.ifi.hase.soprafs23.game.questions.restCountry;

import ch.uzh.ifi.hase.soprafs23.constant.BarrierQuestionEnum;
import ch.uzh.ifi.hase.soprafs23.constant.QuestionType;
import ch.uzh.ifi.hase.soprafs23.game.entity.Country;

import java.util.List;

public class BarrierQuestion {

    public static final int NOPTIONS = 5; // always give 5 DIFFERENT options, one of the the correct one

    private final QuestionType questionType;
    private final BarrierQuestionEnum barrierQuestionEnum;
    private final String questionText;
    private final Country country;
    private List<Integer> answerOptions;
    private List<Country> countryOptions;
    private int correctResult;

    public BarrierQuestion(BarrierQuestionEnum barrierQuestionEnum, Country country, List<Country> countryOptions) {
        this.questionType = QuestionType.BARRIER;
        this.country = country;
        this.countryOptions = countryOptions;
        this.barrierQuestionEnum = barrierQuestionEnum;
        this.questionText = this.barrierQuestionEnum.getQuestion();
        setCorrectResultAndOptions();
    }

    // The Guessed Rank is given like 1,2,3... with 1 being the largest value, the country should be given in the cioc code
    /**
     * Evaluate a guess for a Barrier Question
     * @param guess The guess to evaluate against the question
     * @return True if guess is correct, False otherwise
     */
    public boolean evaluateGuess(int guess) {
        return correctResult == guess;
    }
    public Country getCountry() {
        return country;
    }

    private void setCorrectResultAndOptions() {
        switch (this.barrierQuestionEnum) {
            case NBORDERS:
                correctResult = country.getNBorders();
                answerOptions = countryOptions.stream().map(Country::getNBorders).toList();
        }
    }
}
