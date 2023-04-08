package ch.uzh.ifi.hase.soprafs23.game.service;

import ch.uzh.ifi.hase.soprafs23.constant.GameMode;
import ch.uzh.ifi.hase.soprafs23.game.entity.Game;
import ch.uzh.ifi.hase.soprafs23.game.entity.Player;
import ch.uzh.ifi.hase.soprafs23.game.repository.GameRepository;
import ch.uzh.ifi.hase.soprafs23.game.repository.PlayerRepository;
import ch.uzh.ifi.hase.soprafs23.game.websockets.dto.outgoing.GameUpdateDTO;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class GameService {

  private final Logger log = LoggerFactory.getLogger(UserService.class);
  private final PlayerRepository playerRepository;
  private final WebSocketService webSocketService;
  private int gameCounter;

  @Autowired
  public GameService(
          @Qualifier("playerRepository") PlayerRepository playerRepository,
          WebSocketService webSocketService) {
    this.playerRepository = playerRepository;
    this.webSocketService = webSocketService;
  }

  public Game getGameById(Long gameId) {
    return GameRepository.findByGameId(gameId);
  }

  /**
   * Create a new game and return the corresponding int
   * @return gameId of the created game
   * @throws org.springframework.web.server.ResponseStatusException
   */
  public Long createNewGame(String gameName, GameMode gameMode) {
    gameCounter++;
    removeAllPlayersFromGame((long) gameCounter);
    Game newGame = new Game((long) gameCounter, gameName, gameMode, playerRepository);
    GameRepository.addGame((long) gameCounter, newGame);
    return (long) gameCounter;
  }

  private void removeAllPlayersFromGame(Long gameId) {
    List<Player> players = playerRepository.findByGameId(gameId);
    for (Player player : players) {
      log.info("Deleted Player: {}", player.getPlayerName());
      playerRepository.deleteById(player.getId());
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
   */
  public void updateGame(Long gameId) {
    // fetch the game
    Game gameToUpdate = GameRepository.findByGameId(gameId);

    GameUpdateDTO gameUpdateDTO = new GameUpdateDTO(gameToUpdate);

    String gameString = new Gson().toJson(gameUpdateDTO);
    webSocketService.sendMessageToClients("/topic/games/" + gameId, gameString);
  }

}
