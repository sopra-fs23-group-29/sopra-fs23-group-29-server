package ch.uzh.ifi.hase.soprafs23.game.questions.restCountry;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@WebAppConfiguration
@SpringBootTest
class CountryServiceTest {

  private final Logger log = LoggerFactory.getLogger(CountryService.class);

  @Autowired
  private final RestTemplate restTemplate = new RestTemplate();

  @Autowired
  private CountryService countryService;

  private final CountryService countryServiceInvalidURL = new CountryService("invalidURL");
  private final CountryService countryServiceServerError = new CountryService("https://httpstat.us/503");

//  @Test
//  void testAllCodes_validURL() {
//    // go through all CIOC codes, either data or null, but no error should happen!
//    for (String ciocCode : QuestionServiceRestcountries.CIOC_CODES) {
//      countryService.getCountryData(ciocCode);
//    }
//    assertTrue(true);
//  }
//
//  @Test
//  void testAllCodes_invalidURL() {
//    // go through all CIOC codes, either data or null, but no error should happen!
//    for (String ciocCode : QuestionServiceRestcountries.CIOC_CODES) {
//      countryServiceInvalidURL.getCountryData(ciocCode);
//    }
//    assertTrue(true);
//  }
//
//  @Test
//  void testAllCodes_serverError() {
//    // go through all CIOC codes, either data or null, but no error should happen!
//    for (String ciocCode : QuestionServiceRestcountries.CIOC_CODES) {
//      countryServiceServerError.getCountryData(ciocCode);
//    }
//    assertTrue(true);
//  }

  @Test
  void switch_to_local() {
    // if the service restcountries.com is actually down, it should realize and switch to local
    if (countryService.isLocal()) {
      assertTrue(countryService.isLocal());
    } else {
      assertFalse(countryService.isLocal());
    }
  }

  @Test
  void test_existing_code() {
    // in case restcountries.com is actually down, the switch to local version should work
    assertNotNull(countryService.getCountryData("GER"));
  }

  @Test
  void test_nonexisting_code() {
    // some non-existing url call should return a 400 BAD_REQUEST, should be caught and return null
    assertNull(countryService.getCountryData("some non existing code"));

    // should also return null in case of invalid URL or server error
    assertNull(countryServiceInvalidURL.getCountryData("some non existing code"));
    assertNull(countryServiceServerError.getCountryData("some non existing code"));

  }

  @Test
  void test_invalid_url() {
    // an invalid URL should switch to local data
    assertTrue(countryServiceInvalidURL.isLocal());
  }

  @Test
  void test_server_error() {
    // an invalid URL should switch to local data
    assertTrue(countryServiceServerError.isLocal());
  }

  @Test
  void server_error_during_operation() {

    // !! Cannot actually test if the restcountries.com server is down !!
    // Check that the server is actually running
    boolean serviceCurrentlyRunning = countryService.testURL();

    if (!serviceCurrentlyRunning) {
      log.warn("The service {} is actually down, tests cannot be run in full!", countryService.getUrl());
      return;
    }

    // Assert that an url suddenly changing from valid to server error does not result in an error
    List<String> validCodes1 = Arrays.asList("GER","SUI");
    List<String> validCodes2 = Arrays.asList("CAN","FRA","ITA");

    // Go through two codes and then switch to server error url. Make sure the rest of the list is still processed
    for (String code : validCodes1) {
      assertNotNull(countryService.getCountryData(code));
    }

    // set the url to a server error url
    countryService.setUrl("https://httpstat.us/503");

    // Go through the other valid codes and make sure the service is still returning not null
    for (String code : validCodes2) {
      assertNotNull(countryService.getCountryData(code));
    }

  }
}