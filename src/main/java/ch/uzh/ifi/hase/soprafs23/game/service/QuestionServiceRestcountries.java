package ch.uzh.ifi.hase.soprafs23.game.service;

import ch.uzh.ifi.hase.soprafs23.constant.QuestionServiceType;
import ch.uzh.ifi.hase.soprafs23.constant.RankingQuestionEnum;
import ch.uzh.ifi.hase.soprafs23.game.entity.Country;
import ch.uzh.ifi.hase.soprafs23.game.questions.IQuestionService;
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

  private static final String[] CIOC_CODES = {
          "AFG", "ALB", "ALG", "AND", "ANG", "ANT", "ARG", "ARM", "ARU", "ASA", "AUS", "AUT", "AZE", "BAH", "BAN", "BAR", "BDI", "BEL", "BEN", "BER", "BHU", "BIH",
          "BIZ", "BLR", "BOL", "BOT", "BRA", "BRN", "BRU", "BUL", "BUR", "CAF", "CAM", "CAN", "CAY", "CGO", "CHA", "CHI", "CHN", "CIV", "CMR", "COD", "COK", "COL",
          "COM", "CPV", "CRC", "CRO", "CUB", "CYP", "CZE", "DEN", "DJI", "DMA", "DOM", "ECU", "EGY", "ERI", "ESA", "ESP", "EST", "ETH", "FIJ", "FIN", "FRA", "FSM",
          "GAB", "GAM", "GBR", "GBS", "GEO", "GEQ", "GER", "GHA", "GRE", "GRN", "GUA", "GUI", "GUM", "GUY", "HAI", "HKG", "HON", "HUN", "INA", "IND", "IRI", "IRL",
          "ISR", "ISV", "ITA", "IVB", "JAM", "JOR", "JPN", "KAZ", "KEN", "KGZ", "KIR", "KOR", "KOS", "KSA", "KUW", "LAO", "LAT", "LBA", "LBR", "LCA", "LES", "LIB",
          "LIE", "LTU", "LUX", "MAD", "MAR", "MAS", "MAW", "MDA", "MDV", "MEX", "MHL", "MKD", "MLI", "MLT", "MNE", "MON", "MOZ", "MRI", "MTN", "MYA", "NAM", "NCA",
          "NED", "NEP", "NGR", "NIG", "NOR", "NRU", "NZL", "OMA", "PAK", "PAN", "PAR", "PER", "PHI", "PLE", "PLW", "PNG", "POL", "POR", "PRK", "PUR", "QAT", "ROU",
          "RSA", "RUS", "RWA", "SAM", "SEN", "SEY", "SIN", "SKN", "SLE", "SLO", "SMR", "SOL", "SOM", "SRB", "SRI", "STP", "SUD", "SUI", "SUR", "SVK", "SWE", "SWZ",
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

          if (tempCountry.getCioc() == null || tempCountry.getName() == null || tempCountry.getFlagUrl() == null) {
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

//  @Override
//  public BarrierQuestion generateBarrierQuestion() {
//    // todo: Implement barrierQuestion
//  }

}
