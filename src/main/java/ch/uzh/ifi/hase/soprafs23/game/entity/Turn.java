package ch.uzh.ifi.hase.soprafs23.game.entity;

import ch.uzh.ifi.hase.soprafs23.game.questions.restCountry.RankingQuestion;

import java.util.ArrayList;
import java.util.List;

public class Turn {
  private final int turnNumber;
  private final List<Player> turnPlayers;
  private final RankingQuestion rankQuestion;
  private final List<Guess> takenGuesses; // Whenever an answer is saved, the guess is recorded as a Guess object
  private final Leaderboard turnResult; // Turn result saving which player scored how many points. Is implemented as a leaderboard

  public Turn(int turnNumber, List<Player> turnPlayers, RankingQuestion rankQuestion) {
    this.turnNumber = turnNumber;
    this.turnPlayers = turnPlayers;
    this.rankQuestion = rankQuestion;

    // upon creation, create an empty list which holds the guesses
    this.takenGuesses = new ArrayList<>();
    // upon creation, create an empty leaderboard with the players from turnPlayers
    this.turnResult = new Leaderboard();
    this.turnPlayers.forEach((p) -> this.turnResult.putNewPlayer(p.getId()));
  }

  public int getTurnNumber() {
    return turnNumber;
  }
  public List<Player> getTurnPlayers() {
    return turnPlayers;
  }
  public RankingQuestion getRankQuestion() {
    return rankQuestion;
  }
  public List<Guess> getTakenGuesses() {return takenGuesses;}
  public Leaderboard getTurnResult() {return turnResult;}

  /**
   * For convenience, return a list with all IDs of players participating in the turn / have answered
   * @return List with all playerIds from turnPlayers
   */
  public List<Long> getTurnPlayersID() {
    return turnPlayers.stream().map(Player::getId).toList();
  }
  public List<Long> getTurnPlayersDoneID() {
    // Loop through takenGuesses and get playerIds
    return takenGuesses.stream().map(Guess::guessPlayerId).toList();
  }

  public int evaluateGuess(String countryCode, int guess) {
    return rankQuestion.getScore(countryCode, guess);
  }

  public void saveGuess(Player player, String countryCode, int guess) {
    takenGuesses.add(new Guess(player.getId(), player.getPlayerColor(), countryCode, guess));
  }

}
