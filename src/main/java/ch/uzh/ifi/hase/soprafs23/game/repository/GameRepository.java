package ch.uzh.ifi.hase.soprafs23.game.repository;

import ch.uzh.ifi.hase.soprafs23.game.entity.Game;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;

public class GameRepository {
  private static final HashMap<Integer, Game> gameRepo = new HashMap<>();

  private GameRepository() {
  }

  public static void addGame(int gameId, Game game) {
    gameRepo.put(gameId, game);
  }

  public static void removeGame(int gameId) {
    gameRepo.remove(gameId);
  }

  public static Game findByGameId(int gameId) {
    Game game = gameRepo.get(gameId);
    if (game == null) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "This game does not exist!");
    }
    return game;
  }
}
