package ch.uzh.ifi.hase.soprafs23.game.questions.restCountry;
import java.util.ArrayList;
import java.util.List;

import ch.uzh.ifi.hase.soprafs23.constant.QuestionType;
import ch.uzh.ifi.hase.soprafs23.constant.RankingQuestionEnum;
import ch.uzh.ifi.hase.soprafs23.game.entity.Country;
import ch.uzh.ifi.hase.soprafs23.game.questions.Question;

public class RankingQuestion extends Question {
    private final List<Country> countryList;
    private final RankingQuestionEnum rankQuestionCategory;

    public RankingQuestion(RankingQuestionEnum rankingQuestionEnum, List<Country> countryList) {
        this.questionType = QuestionType.RANK;
        this.countryList = countryList;
        this.rankQuestionCategory = rankingQuestionEnum;
        sortCountryList();
    }

    // The Guessed Rank is given like 1,2,3... with 1 being the largest value, the country should be given in the cioc code
    @Override
    public int getScore(String cioc, int guessedRank) {
        for (int i = 0; i < this.countryList.size(); i++) {
            if (this.countryList.get(i).getCioc().equals(cioc)) {
                return Math.abs(i - guessedRank + 1);
            }
        }
        return 0;
    }

    @Override
    protected String getQuestionText() {
        return this.rankQuestionCategory.getQuestion();
    }
    @Override
    protected List<Country> getCountries() {
        return this.countryList;
    }
    @Override
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
