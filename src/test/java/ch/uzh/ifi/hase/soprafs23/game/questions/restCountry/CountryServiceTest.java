package ch.uzh.ifi.hase.soprafs23.game.questions.restCountry;

import ch.uzh.ifi.hase.soprafs23.game.entity.Country;
import ch.uzh.ifi.hase.soprafs23.game.service.QuestionServiceRestcountries;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@WebAppConfiguration
@SpringBootTest
class CountryServiceTest {

  private final Logger log = LoggerFactory.getLogger(CountryService.class);

  @Autowired
  private final RestTemplate restTemplate = new RestTemplate();

  @Autowired
  private CountryService countryService;

//  @Test
//  void testAllCodes() {
//    // go through all CIOC codes, either data or null, but no error should happen!
//    for (String ciocCode : QuestionServiceRestcountries.CIOC_CODES) {
//      countryService.getCountryData(ciocCode);
//    }
//  }

  @Test
  void test_nonexisting_cicccode() {
    // some non-existing url call should return a 400 BAD_REQUEST, should be caught and return null
    assertNull(countryService.getCountryData("some non existing code"));
  }

  @Test
  void test_valid_and_invalid() throws IOException {
    String validCIOC = "GER";
    String invalidCIOC = "asdf";

    Country gerValid = countryService.getCountryData(validCIOC);
    Country invalid = countryService.getCountryData(invalidCIOC);

    assertEquals(gerValid.getName(), "Germany");
    assertNull(invalid);
  }
}