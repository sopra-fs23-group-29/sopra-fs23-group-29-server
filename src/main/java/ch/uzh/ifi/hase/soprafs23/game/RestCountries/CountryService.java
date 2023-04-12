package ch.uzh.ifi.hase.soprafs23.game.RestCountries;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class CountryService {

    @Autowired
    private RestTemplate restTemplate;
    private ObjectMapper objectMapper = new ObjectMapper();

    public Country getCountryData(String COICode) throws JsonMappingException, JsonProcessingException {
        String url = "https://restcountries.com/v3.1/alpha/" + COICode;
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        if (response.getStatusCode() == HttpStatus.OK) {
            String jsonData = response.getBody();

            if (jsonData.startsWith("[") && jsonData.endsWith("]")) {
                jsonData = jsonData.substring(1, jsonData.length()-1);
            }
            
            Country country = objectMapper.readValue(jsonData, Country.class);
            return country;
            
        }
        return null;
    }

}
