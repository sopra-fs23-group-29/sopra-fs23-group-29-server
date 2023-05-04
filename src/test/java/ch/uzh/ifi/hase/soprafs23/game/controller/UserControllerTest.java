package ch.uzh.ifi.hase.soprafs23.game.controller;

import ch.uzh.ifi.hase.soprafs23.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs23.game.entity.User;
import ch.uzh.ifi.hase.soprafs23.game.controller.UserController;
import ch.uzh.ifi.hase.soprafs23.game.rest.dto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs23.game.rest.dto.UserPutDTO;
import ch.uzh.ifi.hase.soprafs23.game.service.UserService;
import ch.uzh.ifi.hase.soprafs23.game.service.WebSocketService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * UserControllerTest
 * This is a WebMvcTest which allows to test the UserController i.e. GET/POST
 * request without actually sending them over the network.
 * This tests if the UserController works.
 */
@WebMvcTest(UserController.class)
public class UserControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private UserService userService;

  @MockBean
  private WebSocketService webSocketService;

  // current date
  private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
  private final LocalDateTime now = LocalDateTime.now();
  private final String currentDate = dtf.format(now);
  private final String fakeBirthday = "1990-01-01";

  @Test
  public void givenUsers_whenGetUsers_thenReturnJsonArray() throws Exception {
    // given
    User user = new User();
    user.setPassword("Password");
    user.setUsername("firstname@lastname");
    user.setStatus(UserStatus.OFFLINE);
    user.setCreationDate(currentDate);
    user.setBirthday(fakeBirthday);
    user.setToken("1");

    List<User> allUsers = Collections.singletonList(user);

    // this mocks the UserService -> we define above what the userService should
    // return when getUsers() is called
    given(userService.getUsers()).willReturn(allUsers);

    // when
    MockHttpServletRequestBuilder getRequest = get("/users").contentType(MediaType.APPLICATION_JSON)
      .header("Authorization", "1");

    // then
    mockMvc.perform(getRequest).andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].username", is(user.getUsername())))
        .andExpect(jsonPath("$[0].status", is(user.getStatus().toString())))
        .andExpect(jsonPath("$[0].creationDate", is(user.getCreationDate())))
        .andExpect(jsonPath("$[0].birthday", is(user.getBirthday())));
  }

  @Test
  public void givenUsers_whenGetSingleUser_thenReturnJsonArray() throws Exception {
    // given
    User user = new User();
    user.setId(1L);
    user.setPassword("Password");
    user.setUsername("firstname@lastname");
    user.setToken("1");
    user.setStatus(UserStatus.OFFLINE);
    user.setCreationDate(currentDate);
    user.setBirthday(fakeBirthday);

    // this mocks the UserService -> we define above what the userService should
    // return when getUsers() is called
    given(userService.getUserById(user.getId())).willReturn(user);

    // when
    MockHttpServletRequestBuilder getRequest = get("/users/1").contentType(MediaType.APPLICATION_JSON)
      .header("Authorization", "1");

    // then
    mockMvc.perform(getRequest).andExpect(status().isOk())
            .andExpect(jsonPath("$.id", is(1)))
            .andExpect(jsonPath("$.username", is(user.getUsername())))
            .andExpect(jsonPath("$.status", is(user.getStatus().toString())))
            .andExpect(jsonPath("$.creationDate", is(user.getCreationDate())))
            .andExpect(jsonPath("$.birthday", is(user.getBirthday())));
  }

  @Test
  public void givenUsers_whenGetSingleUser_thenThrowNotFound() throws Exception {
    // given
    User user = new User();
    user.setId(1L);
    user.setPassword("Password");
    user.setUsername("firstname@lastname");
    user.setToken("1");
    user.setStatus(UserStatus.OFFLINE);
    user.setCreationDate(currentDate);
    user.setBirthday(fakeBirthday);

    // this mocks the UserService -> we define above what the userService should
    // return when getUsers() is called
    given(userService.getUserById(1L)).willThrow(
      new ResponseStatusException(HttpStatus.NOT_FOUND)
    );

    // when
    MockHttpServletRequestBuilder getRequest = get("/users/1").contentType(MediaType.APPLICATION_JSON)
      .header("Authorization", "1");


    // then
    mockMvc.perform(getRequest).andExpect(status().isNotFound());
  }

  @Test
  public void createUser_validInput_userCreated() throws Exception {
    // given
    User user = new User();
    user.setId(1L);
    user.setPassword("testPassword");
    user.setUsername("testUsername");
    user.setToken("1");
    user.setStatus(UserStatus.ONLINE);
    user.setCreationDate(currentDate);
    user.setBirthday(fakeBirthday);

    UserPostDTO userPostDTO = new UserPostDTO();
    userPostDTO.setUsername("testUsername");
    userPostDTO.setPassword("testPassword");

    given(userService.createUser(Mockito.any())).willReturn(user);

    // when/then -> do the request + validate the result
    MockHttpServletRequestBuilder postRequest = post("/users")
        .contentType(MediaType.APPLICATION_JSON)
        .content(asJsonString(userPostDTO));

    // then
    mockMvc.perform(postRequest)
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id", is(user.getId().intValue())))
        .andExpect(jsonPath("$.username", is(user.getUsername())))
        .andExpect(jsonPath("$.status", is(user.getStatus().toString())))
        .andExpect(jsonPath("$.creationDate", is(user.getCreationDate())))
        .andExpect(jsonPath("$.birthday", is(user.getBirthday())));
  }

  @Test
  public void createUser_duplicatedName_thenThrow() throws Exception {
    // given
    User user = new User();
    user.setId(1L);
    user.setPassword("testPassword");
    user.setUsername("testUsername");
    user.setToken("1");
    user.setStatus(UserStatus.ONLINE);
    user.setCreationDate(currentDate);
    user.setBirthday(fakeBirthday);

    UserPostDTO userPostDTO = new UserPostDTO();
    userPostDTO.setUsername("testUsername");
    userPostDTO.setPassword("testPassword");

    given(userService.createUser(Mockito.any())).willThrow(
      new ResponseStatusException(HttpStatus.CONFLICT)
    );

    // when/then -> do the request + validate the result
    MockHttpServletRequestBuilder postRequest = post("/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content(asJsonString(userPostDTO));

    // then
    mockMvc.perform(postRequest)
            .andExpect(status().isConflict());
  }

  @Test
  public void updateUser_validInput_userUpdated() throws Exception {
    // given
    User user = new User();
    user.setId(1L);
    user.setPassword("testPassword");
    user.setUsername("testUsername");
    user.setToken("1");
    user.setStatus(UserStatus.ONLINE);
    user.setCreationDate(currentDate);
    user.setBirthday("testBirthday");

    UserPutDTO userPutDTO = new UserPutDTO();
    userPutDTO.setUsername("testUsername");
    userPutDTO.setBirthday("testBirthday");

    given(userService.updateUser(Mockito.any(), Mockito.any(), Mockito.any())).willReturn(user);

    // when/then -> do the request + validate the result
    MockHttpServletRequestBuilder putRequest = put("/users/1")
            .contentType(MediaType.APPLICATION_JSON)
            .content(asJsonString(userPutDTO))
            .header("Authorization", "1");

    // then
    mockMvc.perform(putRequest)
            .andExpect(status().isNoContent());
  }

  @Test
  public void updateUser_duplicatedUsername_thenThrow() throws Exception {

    // given
    User user = new User();
    user.setId(1L);
    user.setPassword("testPassword");
    user.setUsername("testUsername");
    user.setToken("1");
    user.setStatus(UserStatus.ONLINE);
    user.setCreationDate(currentDate);
    user.setBirthday("testBirthday");

    UserPutDTO userPutDTO = new UserPutDTO();
    userPutDTO.setUsername("testUsername");
    userPutDTO.setBirthday("testBirthday");

    given(userService.updateUser(Mockito.any(), Mockito.any(), Mockito.any())).willThrow(
      new ResponseStatusException(HttpStatus.CONFLICT)
    );

    // when/then -> do the request + validate the result
    MockHttpServletRequestBuilder putRequest = put("/users/1")
            .contentType(MediaType.APPLICATION_JSON)
            .content(asJsonString(userPutDTO))
            .header("Authorization", "1");

    // then
    mockMvc.perform(putRequest)
            .andExpect(status().isConflict());

  }

  @Test
  public void updateUser_wrongToken_thenThrowForbidden() throws Exception {

    // given
    User user = new User();
    user.setId(1L);
    user.setPassword("testPassword");
    user.setUsername("testUsername");
    user.setToken("1");
    user.setStatus(UserStatus.ONLINE);
    user.setCreationDate(currentDate);
    user.setBirthday("testBirthday");

    UserPutDTO userPutDTO = new UserPutDTO();
    userPutDTO.setUsername("testUsername");
    userPutDTO.setBirthday("testBirthday");

    given(userService.checkToken("1", 1L)).willThrow(
            new ResponseStatusException(HttpStatus.FORBIDDEN)
    );
    given(userService.updateUser(Mockito.any(), Mockito.any(), Mockito.any())).willThrow(
            new ResponseStatusException(HttpStatus.CONFLICT)
    );

    // when/then -> do the request + validate the result
    MockHttpServletRequestBuilder putRequest = put("/users/1")
            .contentType(MediaType.APPLICATION_JSON)
            .content(asJsonString(userPutDTO))
            .header("Authorization", "1");

    // then
    mockMvc.perform(putRequest)
            .andExpect(status().isForbidden());

  }

  @Test
  public void loginUser_validInput_thenReturnJsonArray() throws Exception {

    // given
    User user = new User();
    user.setId(1L);
    user.setPassword("testPassword");
    user.setUsername("testUsername");
    user.setToken("1");
    user.setStatus(UserStatus.ONLINE);
    user.setCreationDate(currentDate);
    user.setBirthday("testBirthday");

    UserPutDTO userPutDTO = new UserPutDTO();
    userPutDTO.setUsername("testUsername");
    userPutDTO.setPassword("testPassword");

    given(userService.checkLogin(Mockito.any())).willReturn(user);

    // when/then -> do the request + validate the result
    MockHttpServletRequestBuilder putRequest = put("/users/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(asJsonString(userPutDTO));

    // then
    mockMvc.perform(putRequest)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id", is(user.getId().intValue())))
            .andExpect(jsonPath("$.username", is(user.getUsername())))
            .andExpect(jsonPath("$.status", is(user.getStatus().toString())))
            .andExpect(jsonPath("$.creationDate", is(user.getCreationDate())))
            .andExpect(jsonPath("$.birthday", is(user.getBirthday())));

  }

  @Test
  public void logoutUser_validInput_thenReturnJsonArray() throws Exception {

    // given
    User user = new User();
    user.setId(1L);
    user.setPassword("testPassword");
    user.setUsername("testUsername");
    user.setToken("1");
    user.setStatus(UserStatus.ONLINE);
    user.setCreationDate(currentDate);
    user.setBirthday("testBirthday");

    UserPutDTO userPutDTO = new UserPutDTO();
    userPutDTO.setUsername("testUsername");
    userPutDTO.setPassword("testPassword");

    // given
    given(userService.getUserByToken(Mockito.any())).willReturn(user);

    // when/then -> do the request + validate the result
    MockHttpServletRequestBuilder putRequest = put("/users/logout")
        .contentType(MediaType.APPLICATION_JSON)
        .header("Authorization","1");

    // then
    mockMvc.perform(putRequest)
        .andExpect(status().isOk());
  }

  // todo: deleteUser
  @Test
  public void deleteUser_validInput_userDeleted() {
  }


  /**
   * Helper Method to convert userPostDTO into a JSON string such that the input
   * can be processed
   * Input will look like this: {"name": "Test User", "username": "testUsername"}
   *
   * @param object
   * @return string
   */
  private String asJsonString(final Object object) {
    try {
      return new ObjectMapper().writeValueAsString(object);
    } catch (JsonProcessingException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          String.format("The request body could not be created.%s", e));
    }
  }
}