package ch.uzh.ifi.hase.soprafs23.game.entity;

import ch.uzh.ifi.hase.soprafs23.constant.GameMode;
import ch.uzh.ifi.hase.soprafs23.constant.GameStatus;
import ch.uzh.ifi.hase.soprafs23.constant.PlayerColor;
import ch.uzh.ifi.hase.soprafs23.game.questions.IQuestionService;
import ch.uzh.ifi.hase.soprafs23.game.questions.restCountry.CountryService;
import ch.uzh.ifi.hase.soprafs23.game.questions.restCountry.RankingQuestion;
import ch.uzh.ifi.hase.soprafs23.game.service.PlayerService;
import ch.uzh.ifi.hase.soprafs23.game.websockets.dto.incoming.Answer;

import java.util.*;


/**
 * Internal Game Representation
 * This class represents a game
 */

public class Game {

  public static final int MAXPLAYERS = 6;

  private List<Player> players;
  private Turn turn;
  private PlayerService playerService;
  private IQuestionService questionService;
  private Long gameId;
  private String gameName;
  private GameStatus gameStatus;
  private GameMode gameMode;
  private Leaderboard leaderboard;
  private Leaderboard barrierLeaderboard;
  private int turnNumber;
  private int boardSize;
  private int maxDuration;
  private int maxTurns;

  /**
   * The constructor always needs a playerRepository to fetch its current players
   * @param gameId id of the game
   * @param gameName name of the game
   * @param gameMode Which mode to play
   * @param playerService PlayerService instance
   */
  public Game(
    Long gameId,String gameName,GameMode gameMode
    ,PlayerService playerService
    ,IQuestionService questionService
    ) {
    this.gameId = gameId;
    this.gameName = gameName;
    this.gameMode = gameMode;
    this.playerService = playerService;
    this.questionService = questionService;

    // upon creation, set gameStatus to INLOBBY
    this.gameStatus = GameStatus.INLOBBY;

    // upon creation, set turnNumber to 0
    this.turnNumber = 0;

    // upon creation, create empty leaderboard and barrierLeaderboard, both Leaderboard class
    this.leaderboard = new Leaderboard();
    this.barrierLeaderboard = new Leaderboard();
  }

  // default no args constructor - needed for test
  public Game() {}

  public void setGameId(Long gameId) {this.gameId = gameId;}
  public Long getGameId() {return gameId;}
  public String getGameName() {
    return gameName;
  }
  public void setGameName(String gameName) {
    this.gameName = gameName;
  }
  public GameStatus getGameStatus() {
    return gameStatus;
  }
  public void setGameStatus(GameStatus gameStatus) {
    this.gameStatus = gameStatus;
  }
  public GameMode getGameMode() {
    return gameMode;
  }
  public void setGameMode(GameMode gameMode) {
    this.gameMode = gameMode;
  }
  public int getTurnNumber() {
    return turnNumber;
  }
  public void setTurnNumber(int turnNumber) {
    this.turnNumber = turnNumber;
  }
  public Leaderboard getLeaderboard() {
    return leaderboard;
  }
  public void setLeaderboard(Leaderboard leaderboard) {
    this.leaderboard = leaderboard;
  }
  public Leaderboard getBarrierLeaderboard() {
    return barrierLeaderboard;
  }
  public void setBarrierLeaderboard(Leaderboard barrierLeaderboard) {
    this.barrierLeaderboard = barrierLeaderboard;
  }
  public int getBoardSize() {
    return boardSize;
  }
  public void setBoardSize(int boardSize) {
    this.boardSize = boardSize;
  }
  public int getMaxDuration() {
    return maxDuration;
  }
  public void setMaxDuration(int maxDuration) {
    this.maxDuration = maxDuration;
  }
  public int getMaxTurns() {
    return maxTurns;
  }
  public void setMaxTurns(int maxTurns) {
    this.maxTurns = maxTurns;
  }
  public Turn getTurn() {
    return turn;
  }


  /**
   * Fetch all current players from the playerRepository via playerService and update the internal players list
   */
  public void updatePlayers() {
    // Fetch all Players for the gameId
    players = playerService.getPlayersByGameId(this.gameId);
  }

  /**
   * Determine if the game can be joined by a player
   */
  public boolean isJoinable() {
      updatePlayers();
      return players.size() < MAXPLAYERS && gameStatus == GameStatus.INLOBBY;
   }

  /**
   * Returns the list of players as an unmodifiable list of the current players in the game.
   * Modifications to the list of players should only be done through the playerService/Repository
   * If no players are added jet, returns an empty list
   * @return  An unmodifiable list object containing all current players of the game
   */
  public List<Player> getPlayersView() {

    if (this.players == null) {
      return List.of();
    }
    return Collections.unmodifiableList(this.players);
  }

  /**
   * Assign PlayerColor for a given list of players
   * Persist into the PLAYER repository
   */
  private void assignColors() {

    Set<PlayerColor> usedPlayerColors = new HashSet<>();

    for (Player p : players) {
      Long pId = p.getId();
      for (PlayerColor pc : PlayerColor.values()) {
        if (pc != PlayerColor.NOTSET && !usedPlayerColors.contains(pc)) {
          usedPlayerColors.add(pc);
          playerService.updatePlayerColor(pId, pc);
          break;
        }
      }
    }
  }

  /**
   * Given the players in the playerRepository, create a turn order
   * Returns a NEW LIST WITH NEW PLAYER OBJECTS
   */
  public List<Player> createTurnOrder() {
    updatePlayers();

    // shuffle randomly
    List<Player> turnOrder = new ArrayList<>(players);
    Collections.shuffle(turnOrder);
    return turnOrder;

    // todo: Keep track of who did how many turns, so that everybody can go first equally
  }

  /**
   * Start the game
   * Set gameStatus = GameStatus.INPROGRESS
   * Assign colours to all Players, based on the moment this method runs
   */
  public void initGame() {
    // Update player list
    updatePlayers();

    // todo: Check if enough number of players?

    // Set gameStatus
    setGameStatus(GameStatus.INPROGRESS);

    // Assign PlayerColor
    assignColors();

    // update players
    updatePlayers();

    // populate leaderboard and barrierLeaderboard
    players.forEach((p) -> leaderboard.putNewPlayer(p.getId()));
    players.forEach((p) -> barrierLeaderboard.putNewPlayer(p.getId()));
  }

  /**
   * Start a new turn, returning a Turn object
   */
  public void nextTurn() {

    turnNumber++;

    // create ordered list of players, determining who's first
    List<Player> turnOrder = createTurnOrder();

    // fetch a question object
    RankingQuestion turnQuestion = questionService.generateRankQuestion(players.size());

    // Dummy RankQuestion
//    RankQuestion turnQuestion = new RankQuestion();
//    turnQuestion.buildDummyRankQuestion(6);

    // New Turn object is saved to game instance
    this.turn = new Turn(turnNumber, turnOrder, turnQuestion);

  }

  /**
   * Update the turn object of the game
   * @param answer The answer given from the client
   */
  public void updateTurn(Answer answer) {
    // Extract answer and save in the current turn object
    Player playerGuessed = playerService.getPlayerByUserToken(answer.getUserToken());
    String countryCodeGuessed = answer.getCountryCode();
    int guess = answer.getGuess();
    PlayerColor colorGuessed = playerGuessed.getPlayerColor();

    this.turn.saveGuess(playerGuessed, countryCodeGuessed, guess, colorGuessed);
  }

  /**
   * Update the leaderboard object from the turn object
   */
  public void updateLeaderboard() {

    // todo: Check that turn.turnPlayersDone is equal to turn.turnPlayers? Otherwise you shouldn't end the turn

    // For each player in turn.turnPlayersDone, get his guess, evaluate and update the leaderboard

    // For each player in turn.turnPlayersDone
    for (Map.Entry<Player, String> entry : turn.getTurnPlayersDone().entrySet()) {
      Player playerGuess = entry.getKey();
      String countryCodeGuess = entry.getValue();

      // Get the guess the player made for country countryCodeGuess
      int guessMade = turn.getSavedGuesses().get(countryCodeGuess);

      // Evaluate the score that guess scored the player
      int playerScoreAdd = turn.evaluateGuess(countryCodeGuess, guessMade);

      // Update the leaderboard
      leaderboard.updateEntry(playerGuess.getId(), playerScoreAdd);
    }
  }

}
