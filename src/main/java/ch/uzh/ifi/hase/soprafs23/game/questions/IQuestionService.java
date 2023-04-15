package ch.uzh.ifi.hase.soprafs23.game.questions;

import ch.uzh.ifi.hase.soprafs23.game.questions.restCountry.BarrierQuestion;
import ch.uzh.ifi.hase.soprafs23.game.questions.restCountry.RankingQuestion;

public interface IQuestionService {

  RankingQuestion generateRankQuestion(int size);

  // todo: Implement barrierQuestion
  BarrierQuestion generateBarrierQuestion();

}
