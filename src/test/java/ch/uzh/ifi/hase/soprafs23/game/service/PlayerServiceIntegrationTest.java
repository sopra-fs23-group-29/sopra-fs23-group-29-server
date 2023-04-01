package ch.uzh.ifi.hase.soprafs23.game.service;

import ch.uzh.ifi.hase.soprafs23.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs23.game.entity.Player;
import ch.uzh.ifi.hase.soprafs23.game.entity.User;
import ch.uzh.ifi.hase.soprafs23.game.repository.PlayerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.web.WebAppConfiguration;

import static org.junit.jupiter.api.Assertions.*;

@WebAppConfiguration
@SpringBootTest
class PlayerServiceIntegrationTest {

  @Qualifier("playerRepository")
  @Autowired
  private PlayerRepository playerRepository;

  @Autowired
  private PlayerService playerService;

  private final User dummyUser = new User();

  @BeforeEach
  public void setup() {
    dummyUser.setPassword("userPassword");
    dummyUser.setToken("userToken");
    dummyUser.setStatus(UserStatus.ONLINE);

    playerRepository.deleteAll();
  }



  @Test
  public void createPlayer_success() {

    // given - empty
    assertNull(playerRepository.findByPlayername("p1"));

    Player p1 = new Player("p1", "p1", dummyUser.getToken());

    // when - player is created
    Player p1_created = playerService.createPlayer(p1);

    // then - make sure player is there, but no game
    assertNull(p1_created.getGame());
    assertNotNull(p1_created.getId());
    assertEquals(p1_created.getPlayername(), p1.getPlayername());
    assertEquals(p1_created.getPlayercolor(), p1.getPlayercolor());
    assertEquals(p1_created.getUserToken(), p1.getUserToken());
    assertEquals(p1_created.getToken(), p1.getToken());

  }

}