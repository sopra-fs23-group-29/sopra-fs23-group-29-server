package ch.uzh.ifi.hase.soprafs23.game.service;

import ch.uzh.ifi.hase.soprafs23.constant.BarrierQuestionEnum;
import ch.uzh.ifi.hase.soprafs23.constant.QuestionServiceType;
import ch.uzh.ifi.hase.soprafs23.constant.RankingQuestionEnum;
import ch.uzh.ifi.hase.soprafs23.game.entity.Country;
import ch.uzh.ifi.hase.soprafs23.game.questions.IQuestionService;
import ch.uzh.ifi.hase.soprafs23.game.questions.restCountry.BarrierQuestion;
import ch.uzh.ifi.hase.soprafs23.game.questions.restCountry.CountryService;
import ch.uzh.ifi.hase.soprafs23.game.questions.restCountry.RankingQuestion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class QuestionServiceRestcountries implements IQuestionService {

  private final Logger log = LoggerFactory.getLogger(QuestionServiceRestcountries.class);
  private final QuestionServiceType questionServiceType;
  private final CountryService countryService;

  public static final String[] CIOC_CODES = {
          "AFG", "ALB", "ALG", "AND", "ANG", "ANT", "ARG", "ARM", "ARU", "ASA", "AUS", "AUT", "AZE", "BAH", "BAN", "BAR", "BDI", "BEL", "BEN", "BER", "BHU", "BIH",
          "BIZ", "BLR", "BOL", "BOT", "BRA", "BRN", "BRU", "BUL", "BUR", "CAF", "CAM", "CAN", "CAY", "CGO", "CHA", "CHI", "CHN", "CIV", "CMR", "COD", "COK", "COL",
          "COM", "CPV", "CRC", "CRO", "CUB", "CYP", "CZE", "DEN", "DJI", "DMA", "DOM", "ECU", "EGY", "ERI", "ESA", "ESP", "EST", "ETH", "FIJ", "FIN", "FRA", "FSM",
          "GAB", "GAM", "GBR", "GBS", "GEO", "GEQ", "GER", "GHA", "GRE", "GRN", "GUA", "GUI", "GUM", "GUY", "HAI", "HKG", "HON", "HUN", "INA", "IND", "IRI", "IRL",
          "ISR", "ISV", "ITA", "IVB", "JAM", "JOR", "JPN", "KAZ", "KEN", "KGZ", "KIR", "KOR", "KOS", "KSA", "KUW", "LAO", "LAT", "LBA", "LBR", "LCA", "LES",
          "LIE", "LTU", "LUX", "MAD", "MAR", "MAS", "MAW", "MDA", "MDV", "MEX", "MHL", "MKD", "MLI", "MLT", "MNE", "MON", "MOZ", "MRI", "MTN", "MYA", "NAM", "NCA",
          "NED", "NEP", "NGR", "NIG", "NOR", "NRU", "NZL", "OMA", "PAK", "PAN", "PAR", "PER", "PHI", "PLE", "PLW", "PNG", "POL", "POR", "PRK", "PUR", "QAT", "ROU",
          "RSA", "RUS", "RWA", "SAM", "SEN", "SEY", "SKN", "SLE", "SLO", "SMR", "SOL", "SOM", "SRB", "SRI", "STP", "SUD", "SUI", "SUR", "SVK", "SWE", "SWZ",
          "SYR", "TAN", "TGA", "THA", "TJK", "TKM", "TLS", "TOG", "TPE", "TTO", "TUN", "TUR", "TUV", "UAE", "UGA", "UKR", "URU", "USA", "UZB", "VAN", "VEN", "VIE",
          "VIN", "YEM", "ZAM", "ZIM"};

  public QuestionServiceRestcountries() {
    this.questionServiceType = QuestionServiceType.RESTCOUNTRIES;
    this.countryService = new CountryService();
  }

  @Override
  public RankingQuestion generateRankQuestion(int size) {

    // If the desired size is bigger than all available countries, throw error
    if (size > CIOC_CODES.length) {
      throw new IllegalArgumentException("Length of sublist cannot be greater than length of original list");
    }

    // Generate random rankCategory
    RankingQuestionEnum randomRankCategory = RankingQuestionEnum.getRandom();

    // To generate a new rankQuestion, fetch size countries with valid values for the category
    List<String> alreadyChosenCountries = new ArrayList<>();
    List<Country> listCountries = new ArrayList<>();

    for (int i = 0; i < size; i++) {
      while (true) {
        int index = (int) (Math.random() * CIOC_CODES.length);
        String ciocCode = CIOC_CODES[index];

        if (!alreadyChosenCountries.contains(ciocCode)) {

          Country tempCountry = countryService.getCountryData(ciocCode);

          if (tempCountry == null || tempCountry.getCioc() == null || tempCountry.getName() == null || tempCountry.getFlagUrl() == null) {
            continue;
          }

          switch(randomRankCategory) {
            case AREA:
              if (tempCountry.getArea() == null) {continue;}
            case POPULATION:
              if (tempCountry.getPopulation() == null) {continue;}
            case GINI:
              if (tempCountry.getGini() == null) {continue;}
            case POPULATION_DENSITY:
              if (tempCountry.getPopulationDensity() == null) {continue;}
            case CAPITAL_LATITUDE:
              if (tempCountry.getCapitalLatitude() == null) {continue;}
          }

          alreadyChosenCountries.add(ciocCode);
          listCountries.add(tempCountry);
          break;
        }
      }
    }

    return new RankingQuestion(randomRankCategory, listCountries);

  }

  @Override
  public BarrierQuestion generateBarrierQuestion() {

    Country countryChosen;
    // List to hold all countries the answers are chosen from, including the correct one
    List<Country> listOptions = new ArrayList<>();

    // Generate random barrier category
    BarrierQuestionEnum randomBarrierQuestionEnum = BarrierQuestionEnum.getRandom();
    // Check if the fetched random barrier category is a true/false question or not
    boolean randomBarrierIsBoolean = randomBarrierQuestionEnum.getIsBoolean();

    // first find the chosen country with the correct answer
    while (true) {
      int index = (int) (Math.random() * CIOC_CODES.length);
      String ciocCode = CIOC_CODES[index];

      Country tempCountry = countryService.getCountryData(ciocCode);

      if (tempCountry == null || tempCountry.getCioc() == null || tempCountry.getName() == null || tempCountry.getFlagUrl() == null) {
        continue;
      }

      switch(randomBarrierQuestionEnum) {
        case NBORDERS:
          if (tempCountry.getNBorders() == null) {continue;}
        case LANDLOCKED:
          if (tempCountry.getLandlocked() == null) {continue;}
        case LANGUAGES:
          if (tempCountry.getLanguages() == null) {continue;}
        case CAPITAL:
          if (tempCountry.getCapital() == null) {continue;}
      }
      countryChosen = tempCountry;
      listOptions.add(countryChosen);
      break;
    }

    // next, if we need other answer options from random countries, search for countries with different answer options
    // if the question ist true/false, don't fetch other countries

    if (!randomBarrierIsBoolean) {
      for (int i = 0; i < BarrierQuestion.NOPTIONS-1; i++) {
        while (true) {
          int index = (int) (Math.random() * CIOC_CODES.length);
          String ciocCode = CIOC_CODES[index];

          if (ciocCode.equals(countryChosen.getCioc())){continue;}

          Country tempCountry = countryService.getCountryData(ciocCode);

          if (tempCountry == null || tempCountry.getCioc() == null || tempCountry.getName() == null || tempCountry.getFlagUrl() == null) {
            continue;
          }

          switch(randomBarrierQuestionEnum) {
            case NBORDERS:
              // continue if target property is null
              if (tempCountry.getNBorders() == null) {continue;}
              int tempCountryNBorders = tempCountry.getNBorders();
              // don't keep the country if the answer would be the same as one already chosen
              if (listOptions.stream().map(Country::getNBorders).toList().contains(tempCountryNBorders)) {continue;}
            case LANGUAGES:
              if (tempCountry.getLanguages() == null) {continue;}
              String tempCountryLanguages = tempCountry.getLanguages();
              if (listOptions.stream().map(Country::getLanguages).toList().contains(tempCountryLanguages)) {continue;}
            case CAPITAL:
              if (tempCountry.getCapital() == null) {continue;}
              String tempCountryCapital = tempCountry.getCapital();
              if (listOptions.stream().map(Country::getCapital).toList().contains(tempCountryCapital)) {continue;}
          }

          listOptions.add(tempCountry);
          break;
        }
      }
    }

    // finally, create the barrier question and return it
    return new BarrierQuestion(randomBarrierQuestionEnum, countryChosen, listOptions);

  }

}
