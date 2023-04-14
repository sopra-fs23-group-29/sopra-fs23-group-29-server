package ch.uzh.ifi.hase.soprafs23.game.questions.restCountry;

import ch.uzh.ifi.hase.soprafs23.constant.BarrierQuestionEnum;
import ch.uzh.ifi.hase.soprafs23.constant.QuestionType;
import ch.uzh.ifi.hase.soprafs23.game.entity.Country;
import ch.uzh.ifi.hase.soprafs23.game.entity.Game;

import java.util.List;

public class BarrierQuestion {

    private final QuestionType questionType;
    private final Country country;
    private int correctResult;
    private final BarrierQuestionEnum barrierQuestionEnum;

    public BarrierQuestion(BarrierQuestionEnum barrierQuestionEnum, Country country) {
        this.questionType = QuestionType.BARRIER;
        this.country = country;
        this.barrierQuestionEnum = barrierQuestionEnum;
        setCorrectResult();
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

    public String getQuestionText() {
        return barrierQuestionEnum.getQuestion();
    }
    public Country getCountry() {
        return country;
    }

    private void setCorrectResult() {
        switch (this.barrierQuestionEnum) {
            case NBORDERS -> correctResult = country.getNBorders();
        }
    }



}
