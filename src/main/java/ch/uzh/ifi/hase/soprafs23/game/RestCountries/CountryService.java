package ch.uzh.ifi.hase.soprafs23.game.RestCountries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.web.server.ResponseStatusException;


@Service
public class CountryService {

    private final Logger log = LoggerFactory.getLogger(CountryService.class);
    @Autowired
    private RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public Country getCountryData(String COICode) throws ResponseStatusException {
        String url = "https://restcountries.com/v3.1/alpha/" + COICode;
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        if (response.getStatusCode() == HttpStatus.OK) {
            String jsonData = response.getBody();

            if (jsonData == null) {
                log.error("jsonData from COICode {} is null, return null", COICode);
                return null;
            }

            if (jsonData.startsWith("[") && jsonData.endsWith("]")) {
                jsonData = jsonData.substring(1, jsonData.length()-1);
            }

            // Catch JsonProcessingException, return null if it fails
            try {
                Country country = objectMapper.readValue(jsonData, Country.class);
                return country;
            } catch (JsonProcessingException e) {
                log.warn("jsonDat from COICode {} could not be parsed, return null", COICode);
                return null;
            }
        }
        return null;
    }

}
