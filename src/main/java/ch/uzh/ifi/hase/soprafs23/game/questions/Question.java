package ch.uzh.ifi.hase.soprafs23.game.questions;

import ch.uzh.ifi.hase.soprafs23.constant.QuestionType;
import ch.uzh.ifi.hase.soprafs23.game.entity.Country;

import java.util.List;

abstract public class Question {

  protected QuestionType questionType;

  protected abstract int getScore(String cioc, int guess);
  protected abstract String getQuestionText();
  protected abstract List<Country> getCountries();
  public abstract List<String> getCountryCodes();

}
