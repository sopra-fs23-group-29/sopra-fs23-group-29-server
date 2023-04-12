package ch.uzh.ifi.hase.soprafs23.game.questions;

import ch.uzh.ifi.hase.soprafs23.constant.QuestionType;
import ch.uzh.ifi.hase.soprafs23.constant.RankCategory;
import ch.uzh.ifi.hase.soprafs23.game.entity.CountryCard;

import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;

public class RankQuestion extends Question {

  private RankCategory rankCategory;
  private List<CountryCard> options;

  public void buildDummyRankQuestion(int size) {
    this.questionText = "dummyQuestion";
    this.rankCategory = RankCategory.AREA;
    this.questionType = QuestionType.RANK;

    IntStream.range(0, size-1).forEachOrdered(n -> {
      options.add(new CountryCard(String.valueOf(n), String.valueOf(n), n, n, n, n, true));
    });
  }

  public String getQuestion() {return this.questionText;}

  public List<CountryCard> getOptions() {return this.options;}

  public int calcScore(String countryCode, int guessedRank) {
    // dummy return value
    return 1;
  }

}
