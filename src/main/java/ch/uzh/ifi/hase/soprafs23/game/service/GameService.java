package ch.uzh.ifi.hase.soprafs23.game.service;

import ch.uzh.ifi.hase.soprafs23.constant.GameMode;
import ch.uzh.ifi.hase.soprafs23.constant.GameStatus;
import ch.uzh.ifi.hase.soprafs23.game.entity.Game;
import ch.uzh.ifi.hase.soprafs23.game.entity.Leaderboard;
import ch.uzh.ifi.hase.soprafs23.game.entity.Player;
import ch.uzh.ifi.hase.soprafs23.game.entity.Turn;
import ch.uzh.ifi.hase.soprafs23.game.repository.GameRepository;
import ch.uzh.ifi.hase.soprafs23.game.websockets.dto.incoming.Answer;
import ch.uzh.ifi.hase.soprafs23.game.websockets.dto.outgoing.GameUpdateDTO;
import ch.uzh.ifi.hase.soprafs23.game.websockets.dto.outgoing.LeaderboardDTO;
import ch.uzh.ifi.hase.soprafs23.game.websockets.dto.outgoing.TurnOutgoingDTO;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
  private final WebSocketService webSocketService;
  private int gameCounter;

  @Autowired
  public GameService(
          PlayerService playerService,
          WebSocketService webSocketService) {
    this.playerService = playerService;
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
   * @param gameId
   * @throws ResponseStatusException if gameId does not exist
   * @return True if joinable, False otherwise
   */
  public boolean gameJoinable(Long gameId) {
    Game gameToJoin = GameRepository.findByGameId(gameId);
    return gameToJoin.isJoinable();
  }

  /**
   * Create a new game and return the corresponding int
   * @return gameId of the created game
   * @throws org.springframework.web.server.ResponseStatusException
   */
  public Long createNewGame(String gameName, GameMode gameMode) {
    gameCounter++;
    removeAllPlayersFromGame((long) gameCounter);
    Game newGame = new Game((long) gameCounter, gameName, gameMode, playerService);
    GameRepository.addGame((long) gameCounter, newGame);
    return (long) gameCounter;
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

  public void startNextTurn(Long gameId) {
    Game gameNextTurn = GameRepository.findByGameId(gameId);

    // todo: Update Players, drop not existing Players from Leaderboard

    gameNextTurn.nextTurn();

  }

  /**
   * Given an answer from a player, update the corresponding turn in the
   * @param answer Answer object with the answer of the player
   * @param playerId Player ID of the player the answer is from
   * @return TurnOutgoingDTO object with the updated Turn
   */
  public TurnOutgoingDTO processAnswer(Answer answer, Long playerId, int turnNumber, Long gameId) throws ResponseStatusException {
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

    // Process the answer
    gameToUpdate.updateTurn(answer);

    // Fetch the new turn
    Turn updatedTurn = gameToUpdate.getTurn();

    return new TurnOutgoingDTO(updatedTurn);

  }

  /**
   * End the current turn, returning a new leaderboard with the updated scores
   */
  public LeaderboardDTO endTurn(Long gameId) {
    // Fetch the game
    Game gameToEndRound = GameRepository.findByGameId(gameId);

    // Evaluate all the guesses from the current turn object, update the leaderboard
    gameToEndRound.updateLeaderboard();

    // return that new Leaderboard
    Leaderboard newLeaderboard = gameToEndRound.getLeaderboard();

    // create DTO object and return it
    return new LeaderboardDTO(newLeaderboard);

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

    // Debugging only, send message also to /users
    webSocketService.sendMessageToClients("/topic/users", gamesString);
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
    webSocketService.sendMessageToClients("/topic/games/" + gameId, gameString);

    // Debugging only, send message also to /users
    webSocketService.sendMessageToClients("/topic/users", gameString);
  }

}
