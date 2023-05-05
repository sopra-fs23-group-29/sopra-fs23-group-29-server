package ch.uzh.ifi.hase.soprafs23.game.service;

import ch.uzh.ifi.hase.soprafs23.game.entity.Country;
import ch.uzh.ifi.hase.soprafs23.game.questions.IQuestionService;
import ch.uzh.ifi.hase.soprafs23.game.questions.restCountry.CountryService;
import ch.uzh.ifi.hase.soprafs23.game.questions.restCountry.RankingQuestion;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.List;
import java.util.HashSet; // Import the HashSet class

import static org.junit.jupiter.api.Assertions.*;

@WebAppConfiguration
@SpringBootTest
class QuestionServiceRestcountriesTest {

    @Autowired
    private CountryService countryService;

    @Autowired
    private IQuestionService questionService;

    @Test
    void testSetup() {
        System.out.println("works.");
    }

    @Test
    void generateRankingQuestion() {
        // given - generate a question of size 6
        RankingQuestion q = questionService.generateRankQuestion(6);

        // assert size 6
        assertEquals(q.getCountries().size(), 6);

        // assert all different
        List<String> q_codes = q.getCountryCodes();
        // List and HashSet should have same size
        assertEquals(q_codes.size(), new HashSet<>(q_codes).size());
    }

    // todo: tests for generateBarrierQuestion


}