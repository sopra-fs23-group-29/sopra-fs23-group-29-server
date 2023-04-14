package ch.uzh.ifi.hase.soprafs23.game.entity;

import ch.uzh.ifi.hase.soprafs23.game.questions.IQuestionService;
import ch.uzh.ifi.hase.soprafs23.game.questions.restCountry.CountryService;
import ch.uzh.ifi.hase.soprafs23.game.questions.restCountry.RankingQuestion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.*;

@WebAppConfiguration
@SpringBootTest
class TurnTest {

  private Player p1, p2;
  private RankingQuestion rankQuestion;

  @Autowired
  private IQuestionService questionService;

  @BeforeEach
  void setup() {
    p1 = new Player();
    p2 = new Player();
    RankingQuestion rankQuestion = questionService.generateRankQuestion(6);
  }

  @Test
  void createTurn() {

    List<Player> playerList = Arrays.asList(p1,p2);

    Turn t1 = new Turn(1, playerList, rankQuestion);

    System.out.println("t1");

  }

}