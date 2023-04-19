package ch.uzh.ifi.hase.soprafs23.game.questions.restCountry;
import java.util.List;

import ch.uzh.ifi.hase.soprafs23.constant.QuestionType;
import ch.uzh.ifi.hase.soprafs23.constant.RankingQuestionEnum;
import ch.uzh.ifi.hase.soprafs23.game.entity.Country;
import ch.uzh.ifi.hase.soprafs23.game.entity.Game;

public class RankingQuestion {
    private final QuestionType questionType;
    private final RankingQuestionEnum rankQuestionCategory;
    private final String questionText;
    private final List<Country> countryList;

    public RankingQuestion(RankingQuestionEnum rankingQuestionEnum, List<Country> countryList) {
        this.questionType = QuestionType.RANK;
        this.countryList = countryList;
        this.rankQuestionCategory = rankingQuestionEnum;
        this.questionText = this.rankQuestionCategory.getQuestion();
        sortCountryList();
    }

    // The Guessed Rank is given like 1,2,3... with 1 being the largest value, the country should be given in the cioc code

    /**
     * Evaluate a guessedRank for country code cioc. Return
     * 3 if guessed the exact actual rank
     * MAX(0, ABS(actual rank - guessed rank))
     * @param cioc String coutnry code to evaluate
     * @param guessedRank integer between 1 and Game.MAXPLAYER
     * @return Integer with the score
     */
    public int getScore(String cioc, int guessedRank) {

        assert(guessedRank > 0 && guessedRank <= Game.MAXPLAYERS);

        // helper list with cioc Strings
        List<String> actualCodes = this.countryList.stream().map(Country::getCioc).toList();

        int actualRank = actualCodes.indexOf(cioc) + 1; // +1 because indexOf is 0 indexed, we start counting at 1
        int guessDiff = Math.abs(actualRank - guessedRank);

        if (guessDiff == 0) {
            return 3;
        } else if (guessDiff == 1) {
            return 2;
        } else if (guessDiff == 2) {
            return 1;
        } else {
            return 0;
        }
    }

    public List<Country> getCountries() {
        return this.countryList;
    }
    public List<String> getCountryCodes() {
        return this.countryList.stream().map(Country::getCioc).toList();
    }

    private void sortCountryList() {
        switch (this.rankQuestionCategory) {
            case AREA -> this.countryList.sort((Country c1, Country c2) -> c2.getArea().compareTo(c1.getArea()));
            case POPULATION ->
                    this.countryList.sort((Country c1, Country c2) -> c2.getPopulation().compareTo(c1.getPopulation()));
            case GINI -> this.countryList.sort((Country c1, Country c2) -> c2.getGini().compareTo(c1.getGini()));
            case POPULATION_DENSITY ->
                    this.countryList.sort((Country c1, Country c2) -> c2.getPopulationDensity().compareTo(c1.getPopulationDensity()));
            case CAPITAL_LATITUDE ->
                    this.countryList.sort((Country c1, Country c2) -> c2.getCapitalLatitude().compareTo(c1.getCapitalLatitude()));
        }
    }
}
