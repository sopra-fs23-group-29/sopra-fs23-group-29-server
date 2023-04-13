package ch.uzh.ifi.hase.soprafs23.game.RestCountries;
import java.util.ArrayList;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

public class RankingQuestion{

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

    private final int playerCount;
    private final CountryService countryService;
    private final ArrayList<Country> countryList = new ArrayList<>();
    private final RankingQuestionEnum randomQuestion;

    public RankingQuestion(int playerCount, CountryService countryService) {
       
        this.countryService = countryService;
        this.playerCount = playerCount;
        this.randomQuestion = RankingQuestionEnum.getRandom();

        getRandomCountryList();
 
        sortCountryList();

        
    }

    //Get a list of the countries of the question. The Countries are ordered by the value of the question. The first country is the largest value
    public ArrayList<Country> getCountryList() {
        return this.countryList;
    }

    //Returns the enum of the question. Call .getQuestion() on the enum to get the question string
    public RankingQuestionEnum getQuestionEnum() {
        return this.randomQuestion;
    }

    //The Guessed Rank is given like 1,2,3... with 1 being the largest value, the country should be given in the cioc code
    public int getScore(String cioc, int guessedRank) {
        for (int i = 0; i < this.countryList.size(); i++) {
            if (this.countryList.get(i).getCioc().equals(cioc)) {
                return Math.abs(i - guessedRank + 1);
            }
        }
        return 0;
    }



    private void getRandomCountryList() {
        if (this.playerCount > CIOC_CODES.length) {
            throw new IllegalArgumentException("Length of sublist cannot be greater than length of original list");
        }
        ArrayList<String> CountryCodeList = new ArrayList<>();
        for (int i = 0; i < this.playerCount; i++) {
            
            while (true) {
                int index = (int) (Math.random() * CIOC_CODES.length);
                String ciocCode = CIOC_CODES[index];


                if (!CountryCodeList.contains(ciocCode)) {

                    Country tempCountry = this.countryService.getCountryData(ciocCode);

                    if (tempCountry.getCioc() == null || tempCountry.getName() == null || tempCountry.getFlagUrl() == null) {
                        continue;
                    }

                    switch(this.randomQuestion) {
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


                    CountryCodeList.add(ciocCode);
                    this.countryList.add(tempCountry);
                    break;
                }

                
            }

            
        }
       
    }
        
    private void sortCountryList() {
        switch(this.randomQuestion) {
            case AREA:
                this.countryList.sort((Country c1, Country c2) -> c2.getArea().compareTo(c1.getArea()));
                break;
            case POPULATION:
                this.countryList.sort((Country c1, Country c2) -> c2.getPopulation().compareTo(c1.getPopulation()));
                break;
            case GINI:
                this.countryList.sort((Country c1, Country c2) -> c2.getGini().compareTo(c1.getGini()));
                break;
            case POPULATION_DENSITY:
                this.countryList.sort((Country c1, Country c2) -> c2.getPopulationDensity().compareTo(c1.getPopulationDensity()));
                break;
            case CAPITAL_LATITUDE:
                this.countryList.sort((Country c1, Country c2) -> c2.getCapitalLatitude().compareTo(c1.getCapitalLatitude()));
                break;
        }
    }
}
