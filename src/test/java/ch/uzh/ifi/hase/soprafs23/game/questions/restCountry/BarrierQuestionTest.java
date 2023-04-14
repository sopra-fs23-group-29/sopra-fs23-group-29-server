package ch.uzh.ifi.hase.soprafs23.game.questions.restCountry;

import ch.uzh.ifi.hase.soprafs23.constant.BarrierQuestionEnum;
import ch.uzh.ifi.hase.soprafs23.constant.RankingQuestionEnum;
import ch.uzh.ifi.hase.soprafs23.game.entity.Country;
import ch.uzh.ifi.hase.soprafs23.game.questions.IQuestionService;
import ch.uzh.ifi.hase.soprafs23.game.service.GameService;
import ch.uzh.ifi.hase.soprafs23.game.service.PlayerService;
import ch.uzh.ifi.hase.soprafs23.game.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@WebAppConfiguration
@SpringBootTest
class BarrierQuestionTest {

  @Autowired
  private PlayerService playerService;
  @Autowired
  private UserService userService;
  @Autowired
  private GameService gameService;
  @Autowired
  private IQuestionService questionService;
  @Autowired
  private CountryService countryService;
  private DummyQuestionService dummyQuestionService;

  private BarrierQuestion barrierQuestion;
  private final BarrierQuestionEnum dummyBarrierCategory = BarrierQuestionEnum.NBORDERS;

  class DummyQuestionService implements IQuestionService {
    public BarrierQuestion generateBarrierQuestion() {// always return rankingQuestion of size 3 with same countryCodes
      Country dummyCountry = countryService.getCountryData("SUI");
      return new BarrierQuestion(dummyBarrierCategory, dummyCountry);
    }
    @Override
    public RankingQuestion generateRankQuestion(int size) {
      return null;
    }
  }

  @BeforeEach
  void setUp() {
    // generate a dummy rankingQuestion
    this.dummyQuestionService = new DummyQuestionService();
    this.barrierQuestion = dummyQuestionService.generateBarrierQuestion();
  }

  @Test
  void testSetup() {
    assertEquals(barrierQuestion.getCountry().getCioc(), "SUI");
    assertEquals(barrierQuestion.getCountry().getNBorders(), 5);
  }

  @Test
  void evaluateGuess() {
    assertFalse(barrierQuestion.evaluateGuess(4));
    assertTrue(barrierQuestion.evaluateGuess(5));
  }
}