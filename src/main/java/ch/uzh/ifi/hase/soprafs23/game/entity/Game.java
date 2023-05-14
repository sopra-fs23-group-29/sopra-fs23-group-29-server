package ch.uzh.ifi.hase.soprafs23.game.entity;

import ch.uzh.ifi.hase.soprafs23.constant.*;
import ch.uzh.ifi.hase.soprafs23.game.questions.IQuestionService;
import ch.uzh.ifi.hase.soprafs23.game.questions.restCountry.BarrierQuestion;
import ch.uzh.ifi.hase.soprafs23.game.questions.restCountry.RankingQuestion;
import ch.uzh.ifi.hase.soprafs23.game.service.PlayerService;
import ch.uzh.ifi.hase.soprafs23.game.service.UserService;
import ch.uzh.ifi.hase.soprafs23.game.websockets.dto.incoming.Answer;
import ch.uzh.ifi.hase.soprafs23.game.websockets.dto.incoming.BarrierAnswer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import java.time.LocalDateTime; // import the LocalDateTime class
import java.time.temporal.ChronoUnit; // for time differences between two datetimes
import java.time.format.DateTimeFormatter; // formatting dates


/**
 * Internal Game Representation
 * This class represents a game
 */

public class Game {

  private final Logger log = LoggerFactory.getLogger(UserService.class);
  
  public static final int MAXPLAYERS = 6;
  // every 3rd field is a barrier question
  public static final int BARRIERPOSITION = 3; // todo: could be dynamic, static must match Client side!

  private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  private List<Player> players;
  // Which players have agreed to hide the scoreboard and move on to moving the players? Is reset with each new turn
  private List<Long> playerIdReadyToMove;
  private Turn turn;
  private int turnNumber;
  private PlayerService playerService;
  private IQuestionService questionService;
  private BarrierQuestion currentBarrierQuestion;
  private boolean waitingForBarrierAnswer; // this lock determines if we can keep on processing or should wait
  private Long gameId;
  private String gameName;
  private GameStatus gameStatus;
  private GameMode gameMode;
  private boolean barriersEnabled; // are barriers enabled or not? if not hitsBarrier and hitsResolvedBarrier never return true;
  private Leaderboard leaderboard;
  private Leaderboard barrierLeaderboard;
  private List<Integer> resolvedBarriers; // keep track of which barriers have been resolved already
  private boolean joinable;
  private BoardSize boardSize;
  private MaxDuration maxDuration;
  private LocalDateTime startDatetime; // used to time the duration of the game
  private int playingTimeInSeconds; // used to time the duration of the game

  /**
   * The constructor always needs a playerRepository to fetch its current players
   * Also a questionService to generate Rank questions
   * @param gameId id of the game
   * @param gameName name of the game
   * @param gameMode Which mode to play
   * @param boardSize BoardSize specifying the size of the board
   * @param maxDuration Number of minutes maximum allowed to play. Not relevant if PVP
   * @param playerService PlayerService instance
   * @param questionService IQuestionService instance
   */
  public Game(
    Long gameId,String gameName,GameMode gameMode, BoardSize boardSize, MaxDuration maxDuration
    ,PlayerService playerService
    ,IQuestionService questionService
    ) {
    this.gameId = gameId;
    this.gameName = gameName;
    this.gameMode = gameMode;
    this.boardSize = boardSize;
    this.maxDuration = maxDuration;
    this.playerService = playerService;
    this.questionService = questionService;

    // upon creation, set gameStatus to INLOBBY
    this.gameStatus = GameStatus.INLOBBY;

    // upon creation, set turnNumber to 0
    this.turnNumber = 0;

    // upon creation ...
    // set the startDatetime
    // set the playingTimeInSeconds to 0
    // create empty leaderboard and barrierLeaderboard, both Leaderboard class
    // create empty list of resolved BarrierQuestions
    // create empty list of players ready to move
    // set currentBarrierQuestion to null
    // set waitingForBarrierAnswer to false
    this.startDatetime = LocalDateTime.now();
    this.playingTimeInSeconds = 0;
    this.currentBarrierQuestion = null;
    this.leaderboard = new Leaderboard();
    this.barrierLeaderboard = new Leaderboard();
    this.resolvedBarriers = new ArrayList<>();
    this.playerIdReadyToMove = new ArrayList<>();
    this.waitingForBarrierAnswer = false;

    // set joinable to true
    this.joinable = true;

    // disable barriers in barriersDisabled for certain game modes
    this.barriersEnabled = true;
    if (this.gameMode.equals(GameMode.HOWFAR)) {
      log.info("Gamemode HOWFAR : Barriers are disabled");
      this.barriersEnabled = false;
    }
  }

  // default no args constructor - needed for mapper
  public Game() {}

  public void setCurrentBarrierQuestion(BarrierQuestion currentBarrierQuestion) {
    this.currentBarrierQuestion = currentBarrierQuestion;
  }
  public BarrierQuestion getCurrentBarrierQuestion() {
    return currentBarrierQuestion;
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
  public boolean getBarriersEnabled() {return this.barriersEnabled;}
  public void setWaitingForBarrierAnswer(boolean waitingForBarrierAnswer) {this.waitingForBarrierAnswer = waitingForBarrierAnswer;}
  public boolean getWaitingForBarrierAnswer() {return this.waitingForBarrierAnswer;}
  public int getTurnNumber() {
    return turnNumber;
  }
  public Leaderboard getLeaderboard() {
    return leaderboard;
  }
  public Leaderboard getBarrierLeaderboard() {
    return barrierLeaderboard;
  }
  public BoardSize getBoardSize() {
    return boardSize;
  }
  public void setBoardSize(BoardSize boardSize) {
    this.boardSize = boardSize;
  }
  public MaxDuration getMaxDuration() {
    return maxDuration;
  }
  public void setMaxDuration(MaxDuration maxDuration) {this.maxDuration = maxDuration;}
  public Turn getTurn() {
    return turn;
  }
  public void setTurn(Turn turn) {this.turn = turn;}
  public boolean getJoinable() {return this.joinable;}
  public int getPlayingTimeInSeconds() {return playingTimeInSeconds;}

  /**
   * Check if moving playerId by one field hits a barrier on the board
   * If current position is 0, always return false
   * If boardSize - current position is smaller than BARRIERPOSITION - 1 also return false
   * @param playerId Player ID
   * @return True if barrier is hit, false otherwise
   */
  public boolean hitsBarrier(Long playerId) {

    // if barriers are not enabled, always return false
    if (!this.barriersEnabled) {
      return false;
    }

    // Get the playerId leaderboard entry
    LeaderboardEntry playerLeaderboardEntry = leaderboard.getEntry(playerId);
    // Get the current score/position of the player
    int currentPosition = playerLeaderboardEntry.getCurrentScore();
    int newPosition = currentPosition+1;

    if (currentPosition == 0) {
      return false;
    }

    if ((boardSize.getBoardSize() - currentPosition) <= BARRIERPOSITION) {
      return false;
    }

    // Check if by adding one, player hits a barrier which is not resolved yet
    if (currentPosition%BARRIERPOSITION == 0) {
      return !resolvedBarriers.contains(newPosition);
    }

    return false;

  }

  /**
   * Check if moving playerId by one field hits an already resolved barrier on the board
   * @param playerId Player ID
   * @return True if a resolved barrier is hit, false otherwise
   */
  public boolean hitsResolvedBarrier(Long playerId) {

    // if barriers are not enabled, always return false
    if (!this.barriersEnabled) {
      return false;
    }

    // Get the playerId leaderboard entry
    LeaderboardEntry playerLeaderboardEntry = leaderboard.getEntry(playerId);
    // Get the current score/position of the player
    int currentPosition = playerLeaderboardEntry.getCurrentScore();
    int newPosition = currentPosition+1;

    // Check if by adding one, player hits an already resolved barrier
    if (currentPosition%BARRIERPOSITION == 0) {
      return resolvedBarriers.contains(newPosition);
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
   * Get the current datetime and update the playingTimeInSeconds by subtracting the startDatetime from it
   */
  public void updateTime() {
    LocalDateTime currentDatetime = LocalDateTime.now();
    if (currentDatetime.compareTo(this.startDatetime) < 0) {
      log.error("Game {} updateTime : currentDatetime {} smaller than startDatetime {}, something is off!", this.gameId, currentDatetime.format(dtf), this.startDatetime.format(dtf));
      throw new RuntimeException();
    }
    this.playingTimeInSeconds = (int) ChronoUnit.SECONDS.between(this.startDatetime, currentDatetime);
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

    // if a game is not INLOBBY, it cannot be joined
    if (getGameStatus() != GameStatus.INLOBBY) {
      return false;
    }

    // if the game is empty, it is joinable
    if (this.players == null) {
      return true;
    }

    // If not PVP, game is Singleplayer and only 1 player is allowed
    if (!(getGameMode().equals(GameMode.PVP))) {
      return players.size() == 0;
    }

    return players.size() < MAXPLAYERS && this.getGameStatus() == GameStatus.INLOBBY;
   }

  /**
   * Check if the conditions for a game over are fulfilled
   * The final field is on boardSize - 1
   * @return True if game is over, false otherwise.
   */
  public boolean gameOver() {
    // if FINISHED, its always true, if INLOBBY always false
    if (gameStatus.equals(GameStatus.FINISHED)) {return true;}
    if (gameStatus.equals(GameStatus.INLOBBY)) {return false;}

    // update the time played in seconds
    updateTime();

    // check winning conditions PVP
    if (gameMode.equals(GameMode.PVP)) {
      for (LeaderboardEntry entry : leaderboard.getEntries()) {
        if (entry.getCurrentScore() >= this.boardSize.getBoardSize()-1) {
          return true;
        }
      }

    // checking winning conditions HOWFAR
    } else if (gameMode.equals(GameMode.HOWFAR)) {
      // no limit on boardsize or fields covered, frontend handles call to /endgame through countdown

    // checking winning conditions HOWFAST
    } else if (gameMode.equals(GameMode.HOWFAST)) {
      // basically same as PVP, as soon as a player reaches the end of the board
      for (LeaderboardEntry entry : leaderboard.getEntries()) {
        if (entry.getCurrentScore() >= this.boardSize.getBoardSize()-1) {
          return true;
        }
      }

    // default for GameMode not covered yet
    } else {
      System.out.println("GAMEOVER : Covered gameModes: PVP, HOWFAR, HOWFAST. Anything else is not covered yet!");
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
   * Process the barrier answer, update the game, leaderboards and the currentBarrierQuestion and the waiting lock
   * DO NOT decrease the turn result by 1 if correct, the player does not use a moving point by answering correctly
   * If the answer was wrong, set the players current turn score to 0
   * @param barrierAnswer The answer given
   * @return True if barrier was answered correct, false otherwise
   */
  public boolean processBarrierAnswer(BarrierAnswer barrierAnswer) {

    // this should never be called on a game with barriersEnabled == false
    if (!this.barriersEnabled) {
      log.error("Game {} with gameMode {} has no barriers enabled, this function should never be called!", this.gameId, this.gameMode);
      throw new RuntimeException();
    }

    // Extract the player that gave the answer
    Player playerGuessed = playerService.getPlayerByUserToken(barrierAnswer.getUserToken());
    Long playerIdGuessed = playerGuessed.getId();
    // Evaluate the guess
    boolean guessCorrect = currentBarrierQuestion.evaluateGuess(barrierAnswer.getGuess());

    // set currentBarrierQuestion to null since the question has been answered
    this.currentBarrierQuestion = null;
    // set the waitingForBarrierAnswer to false since we have received an answer
    this.waitingForBarrierAnswer = false;

    // if the guess was correct, update the leaderboards
    if (guessCorrect) {
      // add to the resolved barriers. It should be where the playerGuessed is in the leaderboard, plus 1
      this.resolvedBarriers.add(this.leaderboard.getEntry(playerIdGuessed).getCurrentScore() + 1);

      // update the leaderboards
      this.leaderboard.addToEntry(playerIdGuessed,1);
      this.barrierLeaderboard.addToEntry(playerIdGuessed, 1);
      // DO NOT decrease the turn results by 1, answering a barrier does not use up moving points

      return true;

      // if the guess was wrong, set the turn score of the player to 0, he cannot move anymore
    } else {
      this.turn.getTurnResult().getEntry(playerIdGuessed).replaceScore(0);
      return false;
    }




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
