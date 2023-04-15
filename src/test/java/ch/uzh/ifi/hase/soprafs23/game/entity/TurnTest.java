package ch.uzh.ifi.hase.soprafs23.game.entity;

import ch.uzh.ifi.hase.soprafs23.constant.PlayerColor;
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
    p1.setId(1L);
    p1.setToken("p1Token");
    p1.setUserToken("p1UserToken");
    p1.setPlayerName("p1");
    p1.setGameId(1L);
    p1.setPlayerColor(PlayerColor.BLUE);
    p2 = new Player();
    p2.setId(2L);
    p2.setToken("p2Token");
    p2.setUserToken("p2UserToken");
    p2.setPlayerName("p2");
    p2.setGameId(2L);
    p2.setPlayerColor(PlayerColor.RED);
    RankingQuestion rankQuestion = questionService.generateRankQuestion(6);
  }

  @Test
  void createTurn() {

    List<Player> playerList = Arrays.asList(p1,p2);

    Turn t1 = new Turn(1, playerList, rankQuestion);

    System.out.println("t1");

  }

}