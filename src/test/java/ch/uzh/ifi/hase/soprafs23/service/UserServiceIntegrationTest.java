package ch.uzh.ifi.hase.soprafs23.service;

import ch.uzh.ifi.hase.soprafs23.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs23.entity.User;
import ch.uzh.ifi.hase.soprafs23.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for the UserResource REST resource.
 *
 * @see UserService
 */
@WebAppConfiguration
@SpringBootTest
public class UserServiceIntegrationTest {

  @Qualifier("userRepository")
  @Autowired
  private UserRepository userRepository;

  @Autowired
  private UserService userService;

  @BeforeEach
  public void setup() {
    userRepository.deleteAll();
  }

  // current date
  private DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
  private LocalDateTime now = LocalDateTime.now();
  private String currentDate = dtf.format(now);

  @Test
  public void createUser_validInputs_success() {
    // given
    assertNull(userRepository.findByUsername("testUsername"));

    User testUser = new User();
    testUser.setPassword("testPassword");
    testUser.setUsername("testUsername");

    // when
    User createdUser = userService.createUser(testUser);

    // then
    assertEquals(testUser.getId(), createdUser.getId());
    assertEquals(testUser.getUsername(), createdUser.getUsername());
    assertNotNull(createdUser.getToken());
    assertEquals(UserStatus.ONLINE, createdUser.getStatus());
    assertEquals(currentDate, createdUser.getCreationDate());
    assertEquals("", createdUser.getBirthday());
  }

  @Test
  public void createUser_duplicateUsername_throwsException() {
    assertNull(userRepository.findByUsername("testUsername"));

    User testUser = new User();
    testUser.setPassword("testPassword");
    testUser.setUsername("testUsername");
    User createdUser = userService.createUser(testUser);

    // attempt to create second user with same username
    User testUser2 = new User();

    // change the password but forget about the username
    testUser2.setPassword("testPassword2");
    testUser2.setUsername("testUsername");

    // check that an error is thrown
    assertThrows(ResponseStatusException.class, () -> userService.createUser(testUser2));
  }

  @Test
  public void checkLogin_success() {

    // given
    assertNull(userRepository.findByUsername("testUsername"));

    // create user
    User testUser = new User();
    testUser.setPassword("testPassword");
    testUser.setUsername("testUsername");
    User createdUser = userService.createUser(testUser);

    // get user
    User retrievedUser = userService.checkLogin(testUser);

    // then
    assertEquals(testUser.getId(), retrievedUser.getId());
    assertEquals(testUser.getUsername(), retrievedUser.getUsername());
    assertNotNull(retrievedUser.getToken());
    assertEquals("", retrievedUser.getBirthday());
    assertEquals(UserStatus.ONLINE, retrievedUser.getStatus());
    assertEquals(currentDate, createdUser.getCreationDate());

  }

  @Test
  public void checkLogin_nonexistentUsername_throwsException() {

    // user not created in db
    User testUser = new User();
    testUser.setPassword("testPassword");
    testUser.setUsername("testUsername");

    // fail because of nonexistent username
    assertThrows(ResponseStatusException.class, () -> userService.checkLogin(testUser));

  }

  @Test
  public void checkLogin_wrongPassword_throwsException() {

    // user created in db
    User testUser = new User();
    testUser.setPassword("testPassword");
    testUser.setUsername("testUsername");
    User createdUser = userService.createUser(testUser);

    // user object with correct username, but wrong password
    User testUser_wrongPassword = new User();
    testUser.setPassword("wrong");
    testUser.setUsername("testUsername");

    // fail because of nonexistent username
    assertThrows(ResponseStatusException.class, () -> userService.checkLogin(testUser_wrongPassword));

  }

  @Test
  public void getUserById_success() {

    // given
    assertNull(userRepository.findByUsername("testUsername"));

    // create user
    User testUser = new User();
    testUser.setPassword("testPassword");
    testUser.setUsername("testUsername");
    User createdUser = userService.createUser(testUser);

    // get user
    // Since id is generated, it's a bit unpredictable
    // Fetch the id from the createdUser
    Long createdUserId = createdUser.getId();
    User retrievedUser = userService.getUserById(createdUserId);

    // then
    assertEquals(testUser.getId(), retrievedUser.getId());
    assertEquals(testUser.getUsername(), retrievedUser.getUsername());
    assertNotNull(retrievedUser.getToken());
    assertEquals(UserStatus.ONLINE, retrievedUser.getStatus());
    assertEquals(currentDate, retrievedUser.getCreationDate());
    assertEquals("", retrievedUser.getBirthday());

  }

  @Test
  public void getUserById_notExist_throwsException() {
    assertThrows(ResponseStatusException.class, () -> userService.getUserById(99L));
  }

  @Test
  public void updateUser_success() {
    // given
    assertNull(userRepository.findByUsername("testUsername"));

    User testUser = new User();
    testUser.setPassword("testPassword");
    testUser.setUsername("testUsername");

    // create the user, fetch his id
    User createdUser = userService.createUser(testUser);
    Long createdUserId = createdUser.getId();

    // update the user
    userService.updateUser(createdUserId, "newUsername", "newBirthday");

    // fetch the user just updated
    User updatedUser = userService.getUserById(createdUserId);

    // then
    assertEquals(createdUser.getId(), updatedUser.getId());
    assertEquals("newUsername", updatedUser.getUsername());
    assertNotNull(updatedUser.getToken());
    assertEquals(UserStatus.ONLINE, updatedUser.getStatus());
    assertEquals(currentDate, updatedUser.getCreationDate());
    assertEquals("newBirthday", updatedUser.getBirthday());
  }

  @Test
  public void updateUser_notExist_throwsException() {

    // given
    assertNull(userRepository.findByUsername("testUsername"));

    // update the non-existent user id
    assertThrows(
      ResponseStatusException.class,
      () -> userService.updateUser(1L, "newUsername", "newBirthday")
    );
  }

  @Test
  public void updateUser_duplicateUsername_throwsException() {

    // given
    assertNull(userRepository.findByUsername("testUsername"));

    User testUser = new User();
    testUser.setPassword("testPassword");
    testUser.setUsername("testUsername");

    User testUser2 = new User();
    testUser2.setPassword("testPassword");
    testUser2.setUsername("testUsername2");

    // create the users
    User createdUser = userService.createUser(testUser);
    User createdUser2 = userService.createUser(testUser2);

    // update the user with the identical username
    assertThrows(
            ResponseStatusException.class,
            () -> userService.updateUser(createdUser.getId(), "testUsername", "newBirthday")
    );

    // update the user with another used username
    assertThrows(
            ResponseStatusException.class,
            () -> userService.updateUser(createdUser.getId(), "testUsername2", "newBirthday")
    );

  }

  @Test
  public void updateUser_nullBirthdayInput_success() {
    // given
    assertNull(userRepository.findByUsername("testUsername"));

    User testUser = new User();
    testUser.setPassword("testPassword");
    testUser.setUsername("testUsername");

    // create the user, fetch his id
    User createdUser = userService.createUser(testUser);
    Long createdUserId = createdUser.getId();

    // update the user
    userService.updateUser(createdUserId, "newUsername", null);

    // fetch the user just updated
    User updatedUser = userService.getUserById(createdUserId);

    // then
    assertEquals(createdUser.getId(), updatedUser.getId());
    assertEquals("newUsername", updatedUser.getUsername());
    assertNotNull(updatedUser.getToken());
    assertEquals(UserStatus.ONLINE, updatedUser.getStatus());
    assertEquals(currentDate, updatedUser.getCreationDate());
    assertEquals("", updatedUser.getBirthday());
  }

  @Test
  public void updateUser_emptyUsername_success() {
    // given
    assertNull(userRepository.findByUsername("testUsername"));

    User testUser = new User();
    testUser.setPassword("testPassword");
    testUser.setUsername("testUsername");

    // create the user, fetch his id
    User createdUser = userService.createUser(testUser);
    Long createdUserId = createdUser.getId();

    System.out.println(createdUser.getToken());
    System.out.println(createdUser.getId());

    // update the user, expect conflict since username cannot be duplicated
    userService.updateUser(createdUserId, null, "newBirthday");

    // fetch the user just updated
    User updatedUser = userService.getUserById(createdUserId);

    // then
    assertEquals(createdUser.getId(), updatedUser.getId());
    assertEquals("testUsername", updatedUser.getUsername());
    assertNotNull(updatedUser.getToken());
    assertEquals(UserStatus.ONLINE, updatedUser.getStatus());
    assertEquals(currentDate, updatedUser.getCreationDate());
    assertEquals("newBirthday", updatedUser.getBirthday());

  }

  @Test
  public void checkToken_success() {

    // given
    assertNull(userRepository.findByUsername("testUsername"));

    // create user
    User testUser = new User();
    testUser.setPassword("testPassword");
    testUser.setUsername("testUsername");
    User createdUser = userService.createUser(testUser);

    System.out.println(createdUser.getToken());
    System.out.println(createdUser.getId());

    // check success of fetching by token
    userService.checkToken(createdUser.getToken(), createdUser.getId());

  }

  @Test
  public void checkToken_throwsException() {

    // given
    assertNull(userRepository.findByUsername("testUsername"));

    // create user
    User testUser = new User();
    testUser.setPassword("testPassword");
    testUser.setUsername("testUsername");
    testUser.setToken("1");
    User createdUser = userService.createUser(testUser);

    assertThrows(
            ResponseStatusException.class,
            () -> userService.checkToken("wrongToken",1L)
    );

    assertThrows(
            ResponseStatusException.class,
            () -> userService.checkToken("1", createdUser.getId()-1)
    );
  }
}
