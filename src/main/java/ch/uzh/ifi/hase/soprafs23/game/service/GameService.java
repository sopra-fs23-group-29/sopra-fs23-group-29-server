package ch.uzh.ifi.hase.soprafs23.game.service;

import ch.uzh.ifi.hase.soprafs23.constant.*;
import ch.uzh.ifi.hase.soprafs23.game.entity.*;
import ch.uzh.ifi.hase.soprafs23.game.questions.IQuestionService;
import ch.uzh.ifi.hase.soprafs23.game.questions.restCountry.BarrierQuestion;
import ch.uzh.ifi.hase.soprafs23.game.repository.GameRepository;
import ch.uzh.ifi.hase.soprafs23.game.websockets.dto.incoming.Answer;
import ch.uzh.ifi.hase.soprafs23.game.websockets.dto.incoming.BarrierAnswer;
import ch.uzh.ifi.hase.soprafs23.game.websockets.dto.incoming.MovePlayers;
import ch.uzh.ifi.hase.soprafs23.game.websockets.dto.outgoing.BarrierQuestionOutgoingDTO;
import ch.uzh.ifi.hase.soprafs23.game.websockets.dto.outgoing.GameUpdateDTO;
import ch.uzh.ifi.hase.soprafs23.game.websockets.dto.outgoing.MovePlayerDTO;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class GameService {

  private final Logger log = LoggerFactory.getLogger(UserService.class);
  private final PlayerService playerService;
  private final IQuestionService questionService;
  private final WebSocketService webSocketService;
  private int gameCounter;

  @Autowired
  public GameService(
          PlayerService playerService,
          IQuestionService questionService,
          WebSocketService webSocketService) {
    this.playerService = playerService;
    this.questionService = questionService;
    this.webSocketService = webSocketService;
  }

  public Game getGameById(Long gameId) {
    return GameRepository.findByGameId(gameId);
  }

  public void updatePlayers(Long gameId) {
    GameRepository.findByGameId(gameId).updatePlayers();
  }

  /**
   * Return if the game at gameId is joinable
   * Not joinable if
   * Full
   * Not INLOBBY
   * @param gameId Game to check if joinable
   * @throws ResponseStatusException if gameId does not exist
   * @return True if joinable, False otherwise
   */
  public boolean gameJoinable(Long gameId) {
    Game gameToJoin = GameRepository.findByGameId(gameId);
    // update the game
    gameToJoin.updatePlayers();
    return gameToJoin.getJoinable();
  }

  /**
   * Create a new game and return the corresponding int
   * @return gameId of the created game
   */
  public Long createNewGame(String gameName, GameMode gameMode, BoardSize boardSize, MaxDuration maxDuration) {
    gameCounter++;
    removeAllPlayersFromGame((long) gameCounter);
    Game newGame = new Game(
      (long) gameCounter,
      gameName,
      gameMode,
      boardSize,
      maxDuration,
      playerService,
      questionService
    );
    GameRepository.addGame((long) gameCounter, newGame);
    return (long) gameCounter;
  }

  /**
   * Remove a game from the GameRepository, also delete all Player entries in PlayerRepository
   * Throw NOT_FOUND if the game at gameId does not exist
   */
  public void deleteGame(Long gameId) {
    // Check if game exists
    GameRepository.findByGameId(gameId);
    // Remove all players
    removeAllPlayersFromGame(gameId);
    GameRepository.removeGame(gameId);
  }

  /**
   * Start a game with the gameId. If the game has already started, throw error
   * @param gameId Game ID to start
   */
  public void startGame(Long gameId) {
    Game gameToStart = GameRepository.findByGameId(gameId);
    if (gameToStart.getGameStatus() != GameStatus.INLOBBY) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Game %s is not in Lobby and cannot be started".formatted(gameId));
    }
    gameToStart.initGame();
  }

  /**
   * Update the game/players/leaderboards and start a new turn
   * Throws BAD_REQUEST when the game is not INPROGRESS, only then a new turn should be created
   * @param gameId The game to start the next turn
   */
  public void startNextTurn(Long gameId) {
    Game gameNextTurn = GameRepository.findByGameId(gameId);

    // Throw error if the game is not INPROGRESS
    if (!gameNextTurn.getGameStatus().equals(GameStatus.INPROGRESS)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Game %s is not INPROGRESS and cannot start a next turn".formatted(gameId));
    }

    // Check if game has reached winning conditions
    // If yes, go to endGame step
    boolean gameOver = gameNextTurn.gameOver();
    if (gameOver) {
      log.info("Game {} is over, ending ...", gameId);
      gameNextTurn.endGame();
      return;
    }

    // Update Players, drop not existing Players from Leaderboard
    gameNextTurn.updatePlayers();
    gameNextTurn.updateLeaderboards();

    gameNextTurn.nextTurn();
  }

  /**
   * Given an answer from a player, update the corresponding turn in the game
   * @param answer Answer object with the answer of the player
   * @param playerId Player ID of the player the answer is from
   * @return Turn object with the updated Turn
   */
  public Turn processAnswer(Answer answer, Long playerId, int turnNumber, Long gameId) throws ResponseStatusException {
    // Fetch/Check the Player at playerId. Throws NOT_FOUND if playerId is not existent
    Player player = playerService.getPlayerById(playerId);
    // Compare to the player from the userToken from answer, should match
    Player playerFromToken = playerService.getPlayerByUserToken(answer.getUserToken());
    if (!player.getId().equals(playerFromToken.getId())) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Player with ID %s cannot answer for Player with ID %s".formatted(player.getId(), playerFromToken.getId()));
    }

    // Fetch the game and the turn, throw error if turnNumber is not the current Turn
    Game gameToUpdate = GameRepository.findByGameId(gameId);
    if (gameToUpdate.getTurnNumber() != turnNumber) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Game %s is currently not at Turn %s".formatted(gameId, turnNumber));
    }

    // Error if the player from playerId is not participating in the game/turn
    // Compare ID, not player object!
    Turn turnToUpdate = gameToUpdate.getTurn();
    if (!turnToUpdate.getTurnPlayersID().contains(playerId)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Player ID %s is not part of Game %s Turn %s".formatted(playerId, gameId, turnNumber));
    }

    // Error if the player has already answered this turn
    // Compare ID, not player object!
    if (turnToUpdate.getTurnPlayersDoneID().contains(playerId)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Player %s has already answered in Game %s Turn %s".formatted(playerId, gameId, turnNumber));
    }

    // Error if the supplied answer.countryCode is not one of the options in the turn question
    if (!turnToUpdate.getRankQuestion().getCountryCodes().contains(answer.getCountryCode())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Country Code %s is not one of the options in Game %s Turn %s".formatted(answer.getCountryCode(), gameId, turnNumber));
    }

    // Error if the supplied answer.guess is not between 1 and Game.MAXPLAYERS
    if (answer.getGuess() < 1 || answer.getGuess() > Game.MAXPLAYERS) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Guess %s not between 1 to %s in Game %s Turn %s".formatted(answer.getGuess(), Game.MAXPLAYERS, gameId, turnNumber));
    }

    // Process the answer
    gameToUpdate.updateTurn(answer);

    // Fetch the new turn
    return gameToUpdate.getTurn();
  }

  /**
   * Given a barrier answer from a player, update the corresponding game and its current turn results
   * Either the game is updated by advancing the playerId by one or not
   * @param barrierAnswer Answer object with the answer of the player
   * @param playerId Player ID of the player the answer is from
   * @param gameId Game ID the barrier question is for
   */
  public void processBarrierAnswer(BarrierAnswer barrierAnswer, Long playerId, Long gameId) {
    // Fetch/Check the Player at playerId. Throws NOT_FOUND if playerId is not existent
    Player player = playerService.getPlayerById(playerId);
    // Compare to the player from the userToken from answer, should match
    Player playerFromToken = playerService.getPlayerByUserToken(barrierAnswer.getUserToken());
    if (!player.getId().equals(playerFromToken.getId())) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Player with ID %s cannot answer for Player with ID %s".formatted(player.getId(), playerFromToken.getId()));
    }

    // Fetch the game
    Game gameToUpdate = GameRepository.findByGameId(gameId);

    // Evaluate the answer, done by the game
    gameToUpdate.processBarrierAnswer(barrierAnswer);

  }

  /**
   * End the current turn, returning a new leaderboard with the updated scores
   * @return A Leaderboard object containing the TURN RESULTS
   */
  public Leaderboard endTurn(Long gameId, int turnNumber) {
    // Fetch the game
    Game gameToEndRound = GameRepository.findByGameId(gameId);

    // throw error if turnNumber is not the current Turn
    if (gameToEndRound.getTurnNumber() != turnNumber) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Game %s is currently not at Turn %s".formatted(gameId, turnNumber));
    }

    // Evaluate all the guesses from the current turn object, update the leaderboard and the turnResult
    gameToEndRound.endTurn();

    // return that new Leaderboard
    return gameToEndRound.getTurn().getTurnResult();

  }

  /**
   * Given a message from the client to be ready to hide the scoreboard and move the players.
   * Check the token, if it's a player in the game set the playersReadyToMove and check if all players have answered
   * @param movePlayers Incoming object when agreeing to move on after the scoreboard containing a userToken
   * @param gameId The game id the movePlayers message is for
   * @return True if all players have answered, false otherwise
   */
  public boolean processMovePlayers(MovePlayers movePlayers, Long gameId) throws ResponseStatusException {
    String userToken = movePlayers.getUserToken();
    // Fetch player from userToken in movePlayers. Throw NOT_FOUND if the userToken does not belong to a player
    Player playerFromToken = playerService.getPlayerByUserToken(userToken);
    if (playerFromToken == null) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "UserToken %s is not linked to a Player".formatted(userToken));
    }
    // Throw UNAUTHORIZED if the userToken Player isn't part of the gameId Game
    if (!playerFromToken.getGameId().equals(gameId)) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Player with UserToken is not part of the game");
    }

    // Fetch the game, GameRepository throws NOT_FOUND if gameId is not a game
    Game gameToMove = GameRepository.findByGameId(gameId);
    // Add the Player to the List of playersReadyToMove
    gameToMove.addPlayerIdReadyToMove(playerFromToken.getId());
    // Then ask the game to check if all players are ready to move on
    return gameToMove.readyToMovePlayers();
  }

  /**
   * Given the turn is over and all players have agreed to move on
   * Try to process a step by fetching the results and finding the first current turn entry with scores left
   * Process that, return True only if no more results to process are found
   * If the game has a waitingForBarrierAnswer lock, we always return False
   * @return True if all current turn results in gameIdToProcess have been processed, false otherwise
   */
  public boolean processTurnResults(Long gameIdToProcess) {

    if (getGameById(gameIdToProcess).getWaitingForBarrierAnswer()) {
      return false;
    }

    List<LeaderboardEntry> resultsToProcess  = getGameById(gameIdToProcess).getTurn().getTurnResult().getEntries();

    // Loop through each result, try to move the player
    for (LeaderboardEntry e : resultsToProcess) {

      // fetch the player belonging to the entry
      Player playerToProcess = playerService.getPlayerById(e.getPlayerId());
      Long pId = playerToProcess.getId();
      PlayerColor pCol = playerToProcess.getPlayerColor();
      int pTurnScoreLeft = e.getCurrentScore();
      int pCurrentScore = getGameById(gameIdToProcess).getLeaderboard().getEntry(pId).getCurrentScore();

      // skip if positive score left, meaning the result has been processed for this turn
      if (pTurnScoreLeft <= 0) {
        continue;
      }

      // Try to move the player by one
      boolean hitsBarrier = tryMovePlayerByOne(gameIdToProcess, pId);
      boolean hitsResolvedBarrier = getGameById(gameIdToProcess).hitsResolvedBarrier(pId);

      // If no barrier is hit, we have to check if a resolved barrier is hit
      // if yes, add one to the global leaderboard and move the player by one field, WITHOUT decreasing his turn score by 1
      // if no, add one to the global leaderboard and move the player by one field, DECREASING his turn score by 1
      if (!hitsBarrier) {

        if (!hitsResolvedBarrier) {
          // case no resolved barrier hit
          log.info("Game {} move Player {} no barrier hit, send message to move by one", gameIdToProcess, pId);
          MovePlayerDTO playerToMoveDTO = new MovePlayerDTO(pId, pCol, pCurrentScore);
          String playerToMoveAsString = new Gson().toJson(playerToMoveDTO);
          webSocketService.sendMessageToClients("/topic/games/" + gameIdToProcess + "/moveByOne", playerToMoveAsString);
          // finally, deduct one field from that player in the TURN RESULT and add one to the GAME LEADERBOARD
          e.addScore(-1);
          getGameById(gameIdToProcess).getLeaderboard().getEntry(pId).addScore(1);

        } else {
          // case resolved barrier hit
          // case no resolved barrier hit
          log.info("Game {} move Player {} resolved barrier hit, send message to move by one without using turn score", gameIdToProcess, pId);
          MovePlayerDTO playerToMoveDTO = new MovePlayerDTO(pId, pCol, pCurrentScore);
          String playerToMoveAsString = new Gson().toJson(playerToMoveDTO);
          webSocketService.sendMessageToClients("/topic/games/" + gameIdToProcess + "/moveByOne", playerToMoveAsString);
          // finally, only add one to the GAME LEADERBOARD
          getGameById(gameIdToProcess).getLeaderboard().getEntry(pId).addScore(1);
        }

      }

      // If a barrier is hit, we send the barrier question and do not move the player until we get notice from the frontend
      // via /resolveBarrierAnswer
      // We set a lock on the game
      if (hitsBarrier) {
        log.info("Game {} move Player {} barrier is hit, sending barrierQuestion to /barrierquestion", gameIdToProcess, pId);

        // set the lock on the game while we wait for an answer
        getGameById(gameIdToProcess).setWaitingForBarrierAnswer(true);

        BarrierQuestion barrierQuestion = questionService.generateBarrierQuestion();
        getGameById(gameIdToProcess).setCurrentBarrierQuestion(barrierQuestion);

        // Create a new BarrierQuestionOutgoingDTO with pId as the ID of the player having to answer the question
        BarrierQuestionOutgoingDTO barrierQuestionOutgoing = new BarrierQuestionOutgoingDTO(barrierQuestion, playerToProcess);
        // make string to send
        String barrierQuestionOutgoingAsString = new Gson().toJson(barrierQuestionOutgoing);
        // send the barrierQuestion together with the player ID answering and his color
        webSocketService.sendMessageToClients("/topic/games/" + gameIdToProcess + "/barrierquestion", barrierQuestionOutgoingAsString);

      }

      // since we had a result to process, we return false
      return false;

    }

    // if we reach this part, this means we have no more results processed
    return true;
  }

  /**
   * Check if we can move the player with playerId in gameId by one field.
   * No authentication with a user token is done, no body is sent.
   * If no barrier question is hit, return false, true otherwise
   * @return True if a barrier question is hit with the move, False otherwise
   * Throw
   * - NOT_FOUND if playerId is not found or if the gameId is not found
   * - BAD_REQUEST if playerId is not participating in gameId
   */
  public boolean tryMovePlayerByOne(Long gameId, Long playerId) {
    // Fetch/Check the Player at playerId. Throws NOT_FOUND if playerId is not existent
    Player player = playerService.getPlayerById(playerId);

    // Fetch the game, throw NOT_FOUND if gameId is not existent
    Game game = GameRepository.findByGameId(gameId);

    // Error if the player from playerId is not participating in the game, throw BAD_REQUEST if not in game
    // Compare ID, not player object!
    List<Long> playersInGame = game.getPlayersView().stream().map(Player::getId).toList();
    if (!playersInGame.contains(playerId)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Player ID %s is not part of Game %s".formatted(playerId, gameId));
    }

    // Check with the game leaderboard if moving by 1 place hits a barrier, return that
    return game.hitsBarrier(playerId);
  }

  private void removeAllPlayersFromGame(Long gameId) {
    List<Player> players = playerService.getPlayersByGameId(gameId);
    for (Player player : players) {
      log.info("Deleted Player: {}", player.getPlayerName());
      playerService.deletePlayerById(player.getId());
    }
  }

  /**
   * Update all subscribers in /topics/games
   */
  public void greetGames() {
    // fetch all games
    List<Game> games = GameRepository.getAllGames();
    List<GameUpdateDTO> tmpGames = new ArrayList<>();

    // Copy the user object, remove token and password before sending
    for (Game game : games) {

      // Create a temporary copy of the game, without the playerRepository
      GameUpdateDTO tmpGame = new GameUpdateDTO(game);

      // Add temporary User to list
      tmpGames.add(tmpGame);

    }

    String gamesString = new Gson().toJson(tmpGames);

    webSocketService.sendMessageToClients("/topic/games", gamesString);
  }

  /**
   * Update all subscribers to that gameId in topics/games/{gameId}
   * @param gameId ID of the game to update
   *
   * todo: Rename that function to greetSingleGame or something like that...
   */
  public void updateGame(Long gameId) {
    // fetch the game
    Game gameToUpdate = GameRepository.findByGameId(gameId);

    GameUpdateDTO gameUpdateDTO = new GameUpdateDTO(gameToUpdate);

    String gameString = new Gson().toJson(gameUpdateDTO);
    webSocketService.sendMessageToClients("/topic/games/" + gameId + "/lobby", gameString);
    // todo: game in progress should also be notified
  }

}
