package ch.uzh.ifi.hase.soprafs23.game.entity;

import ch.uzh.ifi.hase.soprafs23.game.questions.RankQuestion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class TurnTest {

  private Player p1, p2;
  private RankQuestion dummyRankQuestion;

  @BeforeEach
  void setup() {
    p1 = new Player();
    p2 = new Player();
    dummyRankQuestion = new RankQuestion();
    dummyRankQuestion.buildDummyRankQuestion(6);
  }

  @Test
  void createTurn() {

    List<Player> playerList = Arrays.asList(p1,p2);

    Turn t1 = new Turn(1, playerList, dummyRankQuestion);

    System.out.println("t1");

  }

}