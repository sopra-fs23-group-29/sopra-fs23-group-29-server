package ch.uzh.ifi.hase.soprafs23.game.questions.restCountry;

import ch.uzh.ifi.hase.soprafs23.constant.BarrierQuestionEnum;
import ch.uzh.ifi.hase.soprafs23.constant.QuestionType;
import ch.uzh.ifi.hase.soprafs23.game.entity.Country;

import java.util.Arrays;
import java.util.List;

public class BarrierQuestion {

    public static final int NOPTIONS = 5; // For not true/false questions, always give 5 options, one of them correct

    private final QuestionType questionType;
    private final BarrierQuestionEnum barrierQuestionEnum;
    private final String questionText;
    private final Country country;
    private String correctResult; // The options and the results are always done in strings
    private List<String> answerOptions;
    private List<Country> countryOptions;

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
    public boolean evaluateGuess(String guess) {
        return correctResult.equals(guess);
    }
    public Country getCountry() {
        return country;
    }

    private void setCorrectResultAndOptions() {
        switch (this.barrierQuestionEnum) {
            case NBORDERS:
                correctResult = Integer.toString(country.getNBorders());
                // Get all the possible answer options as strings. getNBorders comes as Integer
                List<Integer> optionsInt = countryOptions.stream().map(Country::getNBorders).toList();
                answerOptions = optionsInt.stream().map(Object::toString).toList();
                break;
            case LANDLOCKED:
                // LANDLOCKED is a true/false question, just send 1 (yes) or 0 (no)
                correctResult = country.getLandlocked() ? "yes" : "no";
                answerOptions = Arrays.asList("yes","no");
                break;
        }
    }
}
