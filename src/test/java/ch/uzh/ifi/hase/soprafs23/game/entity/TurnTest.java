package ch.uzh.ifi.hase.soprafs23.game.entity;

import ch.uzh.ifi.hase.soprafs23.game.RestCountries.CountryService;
import ch.uzh.ifi.hase.soprafs23.game.RestCountries.RankingQuestion;
import ch.uzh.ifi.hase.soprafs23.game.questions.RankQuestion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.client.RestTemplate;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@WebAppConfiguration
@SpringBootTest
class TurnTest {

  private Player p1, p2;
  private RankQuestion dummyRankQuestion;
  private RankingQuestion rankQuestion;

  @BeforeEach
  void setup() {
    p1 = new Player();
    p2 = new Player();
    RankingQuestion rankQuestion = new RankingQuestion(6,new CountryService());
//    dummyRankQuestion = new RankQuestion();
//    dummyRankQuestion.buildDummyRankQuestion(6);
  }

  @Test
  void createTurn() {

    List<Player> playerList = Arrays.asList(p1,p2);

    Turn t1 = new Turn(1, playerList, rankQuestion);

    System.out.println("t1");

  }

}