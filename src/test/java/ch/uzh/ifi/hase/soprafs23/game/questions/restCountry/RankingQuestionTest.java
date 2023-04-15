package ch.uzh.ifi.hase.soprafs23.game.questions.restCountry;

import ch.uzh.ifi.hase.soprafs23.constant.RankingQuestionEnum;
import ch.uzh.ifi.hase.soprafs23.game.entity.Country;
import ch.uzh.ifi.hase.soprafs23.game.questions.IQuestionService;
import ch.uzh.ifi.hase.soprafs23.game.repository.PlayerRepository;
import ch.uzh.ifi.hase.soprafs23.game.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs23.game.service.GameService;
import ch.uzh.ifi.hase.soprafs23.game.service.PlayerService;
import ch.uzh.ifi.hase.soprafs23.game.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@WebAppConfiguration
@SpringBootTest
class RankingQuestionTest {

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

  private RankingQuestion rankingQuestion;
  private final RankingQuestionEnum dummyRankingCategory = RankingQuestionEnum.AREA;

  class DummyQuestionService implements IQuestionService {
    @Override
    public RankingQuestion generateRankQuestion(int size) {
      // always return rankingQuestion of size 3 with same countryCodes
      List<Country> dummyList = new ArrayList<>();
      dummyList.add(countryService.getCountryData("USA"));
      dummyList.add(countryService.getCountryData("GER"));
      dummyList.add(countryService.getCountryData("SWZ")); // SWZ = Swaziland
      return new RankingQuestion(dummyRankingCategory, dummyList);
    }

    @Override
    public BarrierQuestion generateBarrierQuestion() {
      return null;
    }


  }

  @BeforeEach
  void setUp() {
    // generate a dummy rankingQuestion
    this.dummyQuestionService = new DummyQuestionService();
    this.rankingQuestion = dummyQuestionService.generateRankQuestion(99);
  }

  @Test
  void size() {
    // assert size = 3
    assertEquals(rankingQuestion.getCountries().size(), 3);
  }

  @Test
  void rankingArea() {
    // assert AREA USA > GER > SWZ
    // get country list
    List<Country> currentCountryList = rankingQuestion.getCountries();
    assertEquals(currentCountryList.get(0).getCioc(), "USA");
    assertEquals(currentCountryList.get(1).getCioc(), "GER");
    assertEquals(currentCountryList.get(2).getCioc(), "SWZ");
  }

  @Test
  void getScore() {
    assertEquals(rankingQuestion.getScore("USA", 1), 3);
    assertEquals(rankingQuestion.getScore("USA", 2), 2);
    assertEquals(rankingQuestion.getScore("USA", 3), 1);
    assertEquals(rankingQuestion.getScore("USA", 4), 0);
    assertEquals(rankingQuestion.getScore("USA", 5), 0);
  }

}