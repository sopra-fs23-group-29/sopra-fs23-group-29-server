package ch.uzh.ifi.hase.soprafs23.game.controller;

import ch.uzh.ifi.hase.soprafs23.game.entity.User;
import ch.uzh.ifi.hase.soprafs23.game.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs23.game.rest.dto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs23.game.rest.dto.UserPutDTO;
import ch.uzh.ifi.hase.soprafs23.game.rest.dto.UserDeleteDTO;
import ch.uzh.ifi.hase.soprafs23.game.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs23.game.service.UserService;
import ch.uzh.ifi.hase.soprafs23.game.service.WebSocketService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;

/**
 * User Controller
 * This class is responsible for handling all REST request that are related to
 * the user.
 * The controller will receive the request and delegate the execution to the
 * UserService and finally return the result.
 */
@RestController
public class UserController {

  private final UserService userService;
  private final WebSocketService webSocketService;

  UserController(UserService userService, WebSocketService webSocketService) {
    this.userService = userService;
    this.webSocketService = webSocketService;
  }


  @GetMapping("/users")
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public List<UserGetDTO> getAllUsers() {
    // fetch all users in the internal representation
    List<User> users = userService.getUsers();
    List<UserGetDTO> userGetDTOs = new ArrayList<>();

    // convert each user to the API representation
    for (User user : users) {
      // remove token information
      user.setToken("");
      userGetDTOs.add(DTOMapper.INSTANCE.convertEntityToUserGetDTO(user));
    }
    return userGetDTOs;
  }

  @GetMapping("/users/{id}")
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public UserGetDTO getSingleUserByUsername(@PathVariable Long id) {
    // fetch the single user in the internal representation
    User retrievedUser = userService.getUserById(id);

    // convert user to the API representation
    UserGetDTO retrievedUserDTO = DTOMapper.INSTANCE.convertEntityToUserGetDTO(retrievedUser);

    return retrievedUserDTO;
  }

  @PostMapping("/users")
  @ResponseStatus(HttpStatus.CREATED)
  @ResponseBody
  public UserGetDTO createUser(@RequestBody UserPostDTO userPostDTO,
                               HttpServletResponse response) {
    // convert API user to internal representation
    User userInput = DTOMapper.INSTANCE.convertUserPostDTOtoEntity(userPostDTO);

    // create user
    User createdUser = userService.createUser(userInput);

    // returning the user token as a header
    response.addHeader("Authorization", createdUser.getToken());

    // Send a message to all WebSocket subscribers in channel /users
    String userListAsString = new Gson().toJson(userService.getUsers());
    webSocketService.sendMessageToClients("/topic/users", userListAsString);

    // convert internal representation of user back to API
    return DTOMapper.INSTANCE.convertEntityToUserGetDTO(createdUser);
  }

  @PutMapping("/users/login")
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public UserGetDTO checkLoginUser(@RequestBody UserPutDTO userPutDTO,
                                   HttpServletResponse response) {
    // convert API user to internal representation
    User userInput = DTOMapper.INSTANCE.convertUserPutDTOtoEntity(userPutDTO);

    // check login
    User checkedUser = userService.checkLogin(userInput);

    // returning the user token as a header
    response.addHeader("Authorization", checkedUser.getToken());

    // set status of user to ONLINE
    userService.setUserOnline(checkedUser.getId());

    // convert internal representation of user back to API
    return DTOMapper.INSTANCE.convertEntityToUserGetDTO(checkedUser);
  }


  @PutMapping("/users/logout")
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public void logoutUser(@RequestHeader("Authorization") String auth_token) {

    // check the auth_token
    userService.checkToken(auth_token, null);

    // fetch the user which belongs to the auth_token
    User userToLogout = userService.getUserByToken(auth_token);

    // set status of user to OFFLINE
    User userChanged = userService.setUserOffline(userToLogout.getId());

  }


  @PutMapping("/users/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @ResponseBody
  public void updateUser(@PathVariable long id,
                         @RequestBody UserPutDTO userPutDTO,
                         @RequestHeader("Authorization") String auth_token) {

    // check the auth_token, the ID must match the token!
    userService.checkToken(auth_token, id);

    // convert DTO object to internal representation
    User userInput = DTOMapper.INSTANCE.convertUserPutDTOtoEntity(userPutDTO);

    // extract the new attributes
    String newUsername = userInput.getUsername();
    String newBirthday = userInput.getBirthday();

    // update the user
    User updatedUser = userService.updateUser(id, newUsername, newBirthday);

  }

  @DeleteMapping("/users/{userId}")
  @ResponseStatus(HttpStatus.ACCEPTED)
  @ResponseBody
  public void deleteUser(@PathVariable("userId") Long userId,
                         @RequestHeader(value="Authorization") String token,
                         @RequestBody UserDeleteDTO userDeleteDTO) {
    // check if token was provided
    if (token == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
              "A token is required for deleting a user!");
    }

    // convert DTO object to internal representation
    User userInput = DTOMapper.INSTANCE.convertUserDeleteDTOtoEntity(userDeleteDTO);

    // extract the new attributes
    String reqUsername = userInput.getUsername();
    String reqPassword = userInput.getPassword();

    // letting userService handle the deletion
    userService.deleteUser(userId, reqUsername, reqPassword, token);
  }
}
