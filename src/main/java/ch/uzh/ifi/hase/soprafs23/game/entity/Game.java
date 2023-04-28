package ch.uzh.ifi.hase.soprafs23.game.entity;

import ch.uzh.ifi.hase.soprafs23.constant.GameMode;
import ch.uzh.ifi.hase.soprafs23.constant.GameStatus;
import ch.uzh.ifi.hase.soprafs23.constant.PlayerColor;
import ch.uzh.ifi.hase.soprafs23.game.questions.IQuestionService;
import ch.uzh.ifi.hase.soprafs23.game.questions.restCountry.BarrierQuestion;
import ch.uzh.ifi.hase.soprafs23.game.questions.restCountry.RankingQuestion;
import ch.uzh.ifi.hase.soprafs23.game.service.PlayerService;
import ch.uzh.ifi.hase.soprafs23.game.websockets.dto.incoming.Answer;
import ch.uzh.ifi.hase.soprafs23.game.websockets.dto.incoming.BarrierAnswer;

import java.util.*;


/**
 * Internal Game Representation
 * This class represents a game
 */

public class Game {

  public static final int MAXPLAYERS = 6;
  // every 3rd field is a barrier question
  public static final int BARRIERPOSITION = 3; // todo: could be dynamic
  public static final int BOARDSIZE = 48; // todo: could be dynamic, must match Board.js

  private List<Player> players;
  // Which players have agreed to hide the scoreboard and move on to moving the players? Is reset with each new turn
  private List<Long> playerIdReadyToMove;
  private Turn turn;
  private int turnNumber;
  private PlayerService playerService;
  private IQuestionService questionService;
  private BarrierQuestion currentBarrierQuestion;
  private Long gameId;
  private String gameName;
  private GameStatus gameStatus;
  private GameMode gameMode;
  private Leaderboard leaderboard;
  private Leaderboard barrierLeaderboard;
  private List<Integer> resolvedBarriers; // keep track of which barriers have been resolved already
  private boolean joinable;
  private int boardSize;
  private int maxDuration;
  private int maxTurns;

  /**
   * The constructor always needs a playerRepository to fetch its current players
   * Also a questionService to generate Rank questions
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

    // todo: boardSize/maxTurns/maxDuration depending on constructor?
    // currently static
    this.boardSize = BOARDSIZE;

    // upon creation, set gameStatus to INLOBBY
    this.gameStatus = GameStatus.INLOBBY;

    // upon creation, set turnNumber to 0
    this.turnNumber = 0;

    // upon creation, create empty leaderboard and barrierLeaderboard, both Leaderboard class
    // create empty list of resovled BarrierQuestions
    // create empty list of players ready to move
    // set currentBarrierQuestion to null
    // set joinable to true if PVP, false otherwise
    this.currentBarrierQuestion = null;
    this.leaderboard = new Leaderboard();
    this.barrierLeaderboard = new Leaderboard();
    this.resolvedBarriers = new ArrayList<>();
    this.playerIdReadyToMove = new ArrayList<>();
    this.joinable = gameMode.equals(GameMode.PVP);
  }

  // default no args constructor - needed for mapper
  public Game() {}

  public void setCurrentBarrierQuestion(BarrierQuestion currentBarrierQuestion) {
    this.currentBarrierQuestion = currentBarrierQuestion;
  }
  public BarrierQuestion getCurrentBarrierQuestion() {
    return currentBarrierQuestion;
  }
  public void setResolvedBarriers(List<Integer> resolvedBarriers) {
    this.resolvedBarriers = resolvedBarriers;
  }
  public List<Integer> getResolvedBarriers() {
    return resolvedBarriers;
  }
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
    // when the GameStatus is changed, update joinable as well
    this.gameStatus = gameStatus;
    this.joinable = isJoinable();
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
  public boolean getJoinable() {return this.joinable;}


  /**
   * Check if moving playerId by one field hits a barrier on the board
   * @param playerId Player ID
   * @return True if barrier is hit, false otherwise
   */
  public boolean hitsBarrier(Long playerId) {
    // Get the playerId leaderboard entry
    LeaderboardEntry playerLeaderboardEntry = leaderboard.getEntry(playerId);
    // Get the current score/position of the player
    int currentPosition = playerLeaderboardEntry.getCurrentScore();
    int newPosition = currentPosition+1;

    // Check if by adding one, player hits a barrier which is not resolved yet
    // Ever BARRIERPOSITION field is a barrier, if modulo equals 0, we hit one of those
    if (newPosition%BARRIERPOSITION == 0) {
      return !resolvedBarriers.contains(newPosition);
    }

    return false;

  }

  /**
   * Fetch all current players from the playerRepository via playerService and update the internal players list
   * Upon updating player, also update if joinable or not
   */
  public void updatePlayers() {
    // Fetch all Players for the gameId
    players = playerService.getPlayersByGameId(this.gameId);
    // Update if joinable
    this.joinable = isJoinable();
  }

  /**
   * Add a player to the current list of all players who are ready to move on
   * @param playerIdToAdd Player
   */
  public void addPlayerIdReadyToMove(Long playerIdToAdd) {
    this.playerIdReadyToMove.add(playerIdToAdd);
  }

  /**
   * Check if currently all players in the game have answered to be ready to move on
   * @return True if all Player in players are found in playerIdReadyToMove
   */
  public boolean readyToMovePlayers() {

    // if not INPROGRESS never true
    if (!gameStatus.equals(GameStatus.INPROGRESS)) {
      return false;
    }

    // if this.playerIdReadyToMove is empty, never return true
    if (this.playerIdReadyToMove.isEmpty()) {
      return false;
    }

    updatePlayers();
    for (Player p : this.players) {
      if (!this.playerIdReadyToMove.contains(p.getId())) {
        return false;
      }
    }
    return true;
  }

  /**
   * Update the leaderboard and barrierLeaderboard with players existing in this.players, fetched form the PlayerRepository
   */
  public void updateLeaderboards() {
    // Fetch all current players IDs
    List<Long> playerIds = this.players.stream().map(Player::getId).toList();
    leaderboard.sync(playerIds);
    barrierLeaderboard.sync(playerIds);
  }

  /**
   * Determine if the game can be joined by a player
   * Return False if GameStatus is not INLOBBY
   * Then return true if players are null
   */
  private boolean isJoinable() {

    // If not PVP, no game is ever joinable
    if (!(getGameMode().equals(GameMode.PVP))) {
      return false;
    }

    if (getGameStatus() != GameStatus.INLOBBY) {
      return false;
    }
    if (this.players == null) {
      return true;
    }
    return players.size() < MAXPLAYERS && this.getGameStatus() == GameStatus.INLOBBY;
   }

  /**
   * Check if the conditions for a game over are fulfilled
   * @return True if game is over, false otherwise.
   */
  public boolean gameOver() {
    // if FINISHED, its always true, if INLOBBY always false
    if (gameStatus.equals(GameStatus.FINISHED)) {return true;}
    if (gameStatus.equals(GameStatus.INLOBBY)) {return false;}

    // check winning conditions PVP
    if (gameMode.equals(GameMode.PVP)) {
      for (LeaderboardEntry entry : leaderboard.getEntries()) {
        if (entry.getCurrentScore() >= this.boardSize) {
          return true;
        }
      }

    // default for GameMode not covered yet
    } else {
      System.out.println("ONLY PVP gameOver implemented!");
      throw new RuntimeException();
    }

    return false;
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
    players.forEach((p) -> leaderboard.putNewPlayer(p.getId(), p.getPlayerName(), p.getPlayerColor()));
    players.forEach((p) -> barrierLeaderboard.putNewPlayer(p.getId(), p.getPlayerName(), p.getPlayerColor()));
  }

  /**
   * Start a new turn, returning a Turn object
   */
  public void nextTurn() {

    // assert Game is in progress
    if (!gameStatus.equals(GameStatus.INPROGRESS)) {
      throw new IllegalStateException("Game not INPROGRESS, nextTurn cannot be called!");
    }

    turnNumber++;

    // reset the playerIdReadyToMove to an empty list
    this.playerIdReadyToMove = new ArrayList<>();

    // create ordered list of players, determining who's first
    List<Player> turnOrder = createTurnOrder();

    // fetch a question object, depending on the number of players either 5 or 6 countries
    int sizeRankingQuestion = players.size() >= 5 ? 6 : 5;
    RankingQuestion turnQuestion = questionService.generateRankQuestion(sizeRankingQuestion);

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

    this.turn.saveGuess(playerGuessed, countryCodeGuessed, guess);
  }

  /**
   * Process the barrier answer, update the game, leaderboards and the currentBarrierQuestion
   * @param barrierAnswer The answer given
   */
  public void processBarrierAnswer(BarrierAnswer barrierAnswer) {
    // Extract the player that gave the answer
    Player playerGuessed = playerService.getPlayerByUserToken(barrierAnswer.getUserToken());
    Long playerIdGuessed = playerGuessed.getId();
    // Evaluate the guess
    boolean guessCorrect = currentBarrierQuestion.evaluateGuess(barrierAnswer.getGuess());

    // if the guess was correct, update the leaderboards
    if (guessCorrect) {
      // add to the resolved barriers. It should be where the playerGuessed is in the leaderboard, plus 1
      this.resolvedBarriers.add(this.leaderboard.getEntry(playerIdGuessed).getCurrentScore() + 1);

      // update the leaderboards
      this.leaderboard.addToEntry(playerIdGuessed,1);
      this.barrierLeaderboard.addToEntry(playerIdGuessed, 1);
    }

    // set currentBarrierQuestion to null since the question has been answered
    this.currentBarrierQuestion = null;

  }

  /**
   * Update the turnResult object from the turn object
   * DO NOT update the leaderboard, since we dont know yet how many field the player actually can move bc of barriers
   */
  public void endTurn() {

    // todo: Check that turn.turnPlayersDone is equal to turn.turnPlayers? Otherwise you shouldn't end the turn

    // For each player in turn.takenGuesses, get the player, his guess, evaluate and update the turnResult board

    // For each player in turn.getTakenGuesses
    for (Guess g : turn.getTakenGuesses()) {
      // evaluate the guess
      int playerScoreAdd = turn.evaluateGuess(g.guessCountryCode(), g.guess());
      // update the turnResult
      turn.getTurnResult().replaceEntry(g.guessPlayerId(), playerScoreAdd, g.guessCountryCode(), g.guess());
    }
  }

  /**
   * Set GameStatus.FINISHED
   */
  public void endGame() {
    // Check again that GameStatus is INPROGRESS
    assert gameStatus.equals(GameStatus.INPROGRESS);

    setGameStatus(GameStatus.FINISHED);
  }
}
