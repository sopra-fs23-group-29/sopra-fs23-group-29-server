package ch.uzh.ifi.hase.soprafs23.game.repository;

import ch.uzh.ifi.hase.soprafs23.game.entity.Game;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GameRepository {
  private static final HashMap<Long, Game> gameRepo = new HashMap<>();

  private GameRepository() {
  }

  /**
   * Clear the GameRepository
   */
  public static void clear() {gameRepo.clear();}

  public static int getSize() {return gameRepo.size();}

  public static ArrayList<Game> getAllGames() {
    return new ArrayList<>(gameRepo.values());
  }

  /**
   * There should be no duplicated keys since only GameService does create games.
   * Throw error if an attempt to add a duplicated key is detected
   * @param gameId ID of the game to add. Cannot exist as a key already
   * @param game Game instance to add
   * @throws org.springframework.web.server.ResponseStatusException
   */
  public static void addGame(Long gameId, Game game) {
    if (gameRepo.containsKey(gameId)) {
      throw new ResponseStatusException(
              HttpStatus.CONFLICT,
              String.format("Game with ID %s already exists in GameRepository", gameId)
      );
    }
    gameRepo.put(gameId, game);
  }

  public static void removeGame(Long gameId) {
    gameRepo.remove(gameId);
  }

  public static Game findByGameId(Long gameId) {
    Game game = gameRepo.get(gameId);
    if (game == null) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "This game does not exist!");
    }
    return game;
  }
}
