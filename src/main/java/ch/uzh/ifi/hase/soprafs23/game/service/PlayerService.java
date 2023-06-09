package ch.uzh.ifi.hase.soprafs23.game.service;

import ch.uzh.ifi.hase.soprafs23.constant.PlayerColor;
import ch.uzh.ifi.hase.soprafs23.game.entity.Game;
import ch.uzh.ifi.hase.soprafs23.game.entity.Player;
import ch.uzh.ifi.hase.soprafs23.game.entity.User;
import ch.uzh.ifi.hase.soprafs23.game.repository.PlayerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class PlayerService {

  private final Logger log = LoggerFactory.getLogger(UserService.class);

  private final PlayerRepository playerRepository;
  private final GameService gameService;
  private final UserService userService;

  @Autowired
  public PlayerService(
          @Qualifier("playerRepository") PlayerRepository playerRepository,
          @Lazy GameService gameService,
          UserService userService) {
    this.playerRepository = playerRepository;
    this.gameService = gameService;
    this.userService = userService;
  }


  public List<Player> getPlayers() {return this.playerRepository.findAll();}

  /**
   * Get all Players with a given gameId
   */
  public List<Player> getPlayersByGameId(Long gameId) {
    return this.playerRepository.findByGameId(gameId);
  }

  /**
   * Get Player by ID
   * @param playerId The ID of the player
   * @return Player
   * @throws org.springframework.web.server.ResponseStatusException Throws not found if playerId is not found
   */
  public Player getPlayerById(Long playerId) {
    Player playerSearched = this.playerRepository.findById(playerId)
            .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            String.format("Player with ID %s not found", playerId)
                    )
            );

    return playerSearched;
  }

  public Player getPlayerByUserToken(String userToken) {
    return this.playerRepository.findByUserToken(userToken);
  }

  /**
   * Update a Player with a given ID to the given PlayerColor
   * Do nothing if Player has already assigned that PlayerColor
   * @param playerId The ID of the Player to update
   * @param playerColorToSet The PlayerColor to set
   */
  public void updatePlayerColor(Long playerId, PlayerColor playerColorToSet) {
    Player playerToUpdate = getPlayerById(playerId);
    playerToUpdate.setPlayerColor(playerColorToSet);
    savePlayer(playerToUpdate);
  }

  /**
   * Given the userToken, either create a new player or return an existing player found by that userToken
   * Throws NOT_FOUND if there is no user to that userToken
   * @param userToken Token of the user
   * @return Player instance, either newly created or the existing one for that userToken
   * @throws org.springframework.web.server.ResponseStatusException
   */
  public Player createPlayerFromUserToken(String userToken) {

    // fetch the user
    User userToCreateFrom = userService.getUserByToken(userToken);

    Player playerFound = getPlayerByUserToken(userToken);

    if (playerFound == null) {
      // if player does not exist with userToken, create
      log.info("No player with that userToken, create Player ...");
      Player playerCreated = new Player();
      playerCreated.setPlayerName(userToCreateFrom.getUsername());
      playerCreated.setUserToken(userToCreateFrom.getToken());
      playerCreated.setToken(UUID.randomUUID().toString());
      playerCreated.setPlayerColor(PlayerColor.NOTSET);
      playerCreated.setIsHost(false);

      // Save the new Player
      savePlayer(playerCreated);
      return playerCreated;

    } else {
      // if Player exists, just fetch him
      log.info("Player already exists, fetch");
      return playerFound;
    }
  }

  public Player joinPlayer(String userTokenToJoin, int gameIdToJoin) {
    // Check if gameIdToJoin exists in GameRepository, throw NOT_FOUND otherwise
    Game gameToJoin = gameService.getGameById((long) gameIdToJoin);

    // Check if the gameToJoin can be joined, throw 409 CONFLICT otherwise
    if (!gameService.gameJoinable((long) gameIdToJoin)) {
      throw new ResponseStatusException(
              HttpStatus.CONFLICT,
              String.format("Game ID %s cannot be joined, either full or not in lobby", gameIdToJoin)
      );
    }

    // Check if a user with userTokenToJoin exists, throw NOT_FOUND otherwise
    User userToJoin = userService.getUserByToken(userTokenToJoin);
    
    // Either fetch the Player associated with the userTokenToJoin, or create a new one
    Player playerToJoin = createPlayerFromUserToken(userTokenToJoin);

    // Set the gameId
    playerToJoin.setGameId((long) gameIdToJoin);

    // Save the player
    savePlayer(playerToJoin);

    return playerToJoin;
  }

  private void savePlayer(Player playerToSave) {
    playerRepository.save(playerToSave);
    playerRepository.flush();
  }

  public void deletePlayerById(Long playerId) {
    Player playerSearched = this.playerRepository.findById(playerId)
            .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            String.format("Player with ID %s not found", playerId)
                    )
            );

    playerRepository.deleteById(playerId);
  }

}
