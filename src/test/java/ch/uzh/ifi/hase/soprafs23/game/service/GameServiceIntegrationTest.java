package ch.uzh.ifi.hase.soprafs23.game.service;

import ch.uzh.ifi.hase.soprafs23.constant.GameMode;
import ch.uzh.ifi.hase.soprafs23.constant.PlayerColor;
import ch.uzh.ifi.hase.soprafs23.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs23.game.entity.Game;
import ch.uzh.ifi.hase.soprafs23.game.entity.Player;
import ch.uzh.ifi.hase.soprafs23.game.entity.User;
import ch.uzh.ifi.hase.soprafs23.game.repository.GameRepository;
import ch.uzh.ifi.hase.soprafs23.game.repository.PlayerRepository;
import ch.uzh.ifi.hase.soprafs23.game.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.web.WebAppConfiguration;

import static org.junit.jupiter.api.Assertions.*;

@WebAppConfiguration
@SpringBootTest
class GameServiceIntegrationTest {

  @Qualifier("gameRepository")
  @Autowired
  private GameRepository gameRepository;

  @Qualifier("playerRepository")
  @Autowired
  private PlayerRepository playerRepository;

  @Qualifier("userRepository")
  @Autowired
  private UserRepository userRepository;

  @Autowired
  private GameService gameService;

  private final User dummyUser = new User();

  @BeforeEach
  public void setup() {

    dummyUser.setUsername("userName");
    dummyUser.setPassword("userPassword");
    dummyUser.setToken("userToken");
    dummyUser.setStatus(UserStatus.ONLINE);

    userRepository.deleteAll();
    playerRepository.deleteAll();
    gameRepository.deleteAll();
  }

  @Test
  public void createGameFromUserToken_success() {

    // given - empty
    assertNull(playerRepository.findByPlayername("p1"));
    assertNull(gameRepository.findByGamename("g1"));

    // given - user is saved to repository
    userRepository.save(dummyUser);
    userRepository.flush();

    // when - game is created
    Game g1_created = gameService.createGame(dummyUser.getToken(), GameMode.PVP);

    // then - make sure game is there, including the player, and the owner is the player derived from the user
    assertNotNull(g1_created.getToken());
    assertEquals(g1_created.getOwner().getUserToken(), dummyUser.getToken());

  }

}