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
import java.util.Arrays;
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



  class DummyQuestionService implements IQuestionService {

    public BarrierQuestionEnum barrierQuestionEnum;

    public DummyQuestionService(BarrierQuestionEnum barrierQuestionEnum) {
      this.barrierQuestionEnum = barrierQuestionEnum;
    }

    @Override
    public BarrierQuestion generateBarrierQuestion() {// always return barrierQuestion with the same 5 countries
      Country dummyCountry = countryService.getCountryData("SUI");
      Country dummyCountry2 = countryService.getCountryData("GER");
      Country dummyCountry3 = countryService.getCountryData("USA");
      Country dummyCountry4 = countryService.getCountryData("CAN");
      Country dummyCountry5 = countryService.getCountryData("FRA");
      return new BarrierQuestion(
        this.barrierQuestionEnum,
        dummyCountry,
        Arrays.asList(dummyCountry, dummyCountry2, dummyCountry3, dummyCountry4, dummyCountry5));
    }

    // for LANDLOCKED barrierQuestionEnum
    public BarrierQuestion generateBarrierQuestion_landlocked() {
      Country dummyCountry = countryService.getCountryData("SUI");
      return new BarrierQuestion(
        BarrierQuestionEnum.LANDLOCKED,
        dummyCountry,
        new ArrayList<>()
      );
    }

    @Override
    public RankingQuestion generateRankQuestion(int size) {
      return null;
    }
  }



  @BeforeEach
  void setUp() {
    // generate a dummy rankingQuestion NBORDERS
    this.dummyQuestionService = new DummyQuestionService(BarrierQuestionEnum.NBORDERS);
    this.barrierQuestion = dummyQuestionService.generateBarrierQuestion();
  }

  @Test
  void testSetup_nBorders() {
    assertEquals(barrierQuestion.getCountry().getCioc(), "SUI");
    assertEquals(barrierQuestion.getCountry().getNBorders(), 5);
  }

  @Test
  void evaluateGuess_nBorders() {
    assertFalse(barrierQuestion.evaluateGuess("4"));
    assertTrue(barrierQuestion.evaluateGuess("5"));
  }

  @Test
  void landlocked() {
    BarrierQuestion barrierQuestionLandlocked = this.dummyQuestionService.generateBarrierQuestion_landlocked();
    // only accept 1 as true, everything else as false
    assertTrue(barrierQuestionLandlocked.evaluateGuess("yes"));
    assertFalse(barrierQuestionLandlocked.evaluateGuess("no"));
    assertFalse(barrierQuestionLandlocked.evaluateGuess("asdflkajsdf"));
  }
}