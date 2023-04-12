package ch.uzh.ifi.hase.soprafs23.game.service;

import ch.uzh.ifi.hase.soprafs23.constant.QuestionServiceType;
import ch.uzh.ifi.hase.soprafs23.game.questions.BarrierQuestion;
import ch.uzh.ifi.hase.soprafs23.game.questions.IQuestionService;
import ch.uzh.ifi.hase.soprafs23.game.questions.RankQuestion;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class QuestionServiceRestcountries implements IQuestionService {

  private QuestionServiceType questionServiceType;
  private String serviceURL;

  public QuestionServiceRestcountries() {
    this.questionServiceType = QuestionServiceType.RESTCOUNTRIES;
  }

  @Override
  public RankQuestion generateRankQuestion() {
    return new RankQuestion();



  }

  @Override
  public BarrierQuestion generateBarrierQuestion() {
    return new BarrierQuestion();
  }

}
