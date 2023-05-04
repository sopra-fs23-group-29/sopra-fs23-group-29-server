package ch.uzh.ifi.hase.soprafs23.game.questions.restCountry;

import ch.uzh.ifi.hase.soprafs23.game.service.GameService;
import ch.uzh.ifi.hase.soprafs23.game.service.QuestionServiceRestcountries;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;

@WebAppConfiguration
@SpringBootTest
class CountryServiceTest {

  @Autowired
  private final RestTemplate restTemplate = new RestTemplate();

  @Autowired
  private CountryService countryService;

//  @Test
//  void testAllCodes() {
//
//    // go through all COIC codes, either data or null, but no error should happen!
//    for (String ciocCode : QuestionServiceRestcountries.CIOC_CODES) {
//      countryService.getCountryData(ciocCode);
//    }
//  }

  @Test
  void test_LIB() {
    // LIB returns 404 NOT_FOUND, should be caught and return null
    assertNull(countryService.getCountryData("LIB"));
  }

  @Test
  void test_nonexisting_coiccode() {
    // some non-existing url call should return a 400 BAD_REQUEST, should be caught and return null
    assertNull(countryService.getCountryData("some non existing code"));
  }
}