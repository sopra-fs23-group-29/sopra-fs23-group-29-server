package ch.uzh.ifi.hase.soprafs23.game.questions.restCountry;

import ch.uzh.ifi.hase.soprafs23.game.entity.Country;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.*;

import java.io.IOException;
import java.nio.file.Paths;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime; // import the LocalDateTime class
import java.time.temporal.ChronoUnit; // for time differences between two datetimes


@Service
public class CountryService {

    private int WAIT = 60;

    private final Logger log = LoggerFactory.getLogger(CountryService.class);
    @Autowired
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private String url;
    private boolean useLocalData = false; // default is false, assume url works

    private LocalDateTime lastCheckUrl = LocalDateTime.now();

    public CountryService() {
        this.url = "https://restcountries.com";
        boolean urlValid = testURL();
        if (!urlValid) {
            log.warn("URL not valid, try to use local data");
            useLocalData = true;
        }
    }

    /**
     * For testing purposes only
     * Inject an invalid server response to mock server failure
     * Inject a custom waiting time for the testURL in getCountryData to recheck
     * @param url The url to use for the restTemplate
     */
    public CountryService(String url, int waitSeconds) {
        this.url = url;
        this.WAIT = waitSeconds;
        boolean urlValid = testURL();
        if (!urlValid) {
            log.warn("URL not valid, try to use local data");
            useLocalData = true;
        }
    }

    public boolean isLocal() {return useLocalData;}

    /**
     * Set and get the url of the CountryService. For testing purposes only
     * @param url New url string to set
     */
    public void setUrl(String url) {this.url = url;}
    public String getUrl() {return this.url;}

    /**
     * Run this function in each constructor to test the validity of the supplied url
     * If a response is not 200 switch to local data mode
     *
     * restTemplate.getForEntity(String url, Class <String>) throws a RestClientException in case something goes wrong
     * IllegalArgument exception is thrown if URL is not in a valid url schema
     * HttpServerErrorException is a subclass of RestClientException specifically catches server errors with response type 5xx
     * The last expression catches all the remaining exceptions
     *
     * @return true if url responds with OK, false otherwise
     */
    public boolean testURL () {
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(this.url, String.class);
            if (response.getStatusCode() != HttpStatus.OK) {
                log.warn("URL response status not 200, returned {}", response.getStatusCode());
                return false;
            }
        } catch (IllegalArgumentException e) {
            log.warn("URL {} is not a valid URL", this.url);
            return false;
        } catch (HttpServerErrorException e) {
            log.warn("URL {} reports server error with code {}", this.url, e.getStatusCode());
            return false;
        } catch (RestClientException e) {
            log.warn("URL {} general RestClientException {}", this.url, e.getClass());
            return false;
        }
        return true;
    }

    public Country getCountryData(String COICode) {

        // If we currently have online data, always check
        if (!this.useLocalData) {
            boolean serverResponsive = testURL();
            if (!serverResponsive) {
                this.useLocalData = true;
            }
        } else {
            // If the current status uses offline data, check every 60 seconds reevaluate the server and set useLocalData
            LocalDateTime now = LocalDateTime.now();
            if (ChronoUnit.SECONDS.between(this.lastCheckUrl, now) > this.WAIT) {
                log.info("Checking status of server ...");
                boolean serverResponsive = testURL();
                if (serverResponsive) {
                    log.info("Set status from offline to online");
                    this.useLocalData = false;
                }
                this.lastCheckUrl = now;
            }
        }

        if (this.useLocalData) {

            // use local data
            try {
                Country[] allCountries = objectMapper.readValue(Paths.get("src/main/resources/countriesV31.json").toFile(), Country[].class);

                for (Country c : allCountries) {
                    c.refreshDataFromNestedObjects();
                    if (c.getCioc().equals(COICode)) {
                        return c;
                    }
                }

                return null;

            } catch (IOException ioException) {
                log.warn("Reading JSON data threw IOException");
                return null;
            }

        } else {

            // do the normal data fetching
            // construct the valid URL
            String fullUrl = this.url + "/v3.1/alpha/" + COICode;
            // catch any HTTP errors from the request
            try {
                ResponseEntity<String> response = restTemplate.getForEntity(fullUrl, String.class);

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
                        country.refreshDataFromNestedObjects();
                        return country;
                    } catch (JsonProcessingException e) {
                        log.warn("jsonDat from COICode {} could not be parsed, return null", COICode);
                        return null;
                    } catch (Exception other_exception) {
                        log.error("objectMapper.readValue(jsonData) failed for unknown reasons");
                        throw other_exception;
                    }
                }
                return null;

            } catch (HttpClientErrorException e) {
                log.warn("COICode {} returned {}", COICode, e.getStatusCode());
                return null;
            } catch (HttpServerErrorException e) {
                log.error("Server is not available");
                throw e;
            } catch (IllegalArgumentException e) {
                log.error("URL {} is not valid", fullUrl);
                throw e;
            }
        }
    }

}
