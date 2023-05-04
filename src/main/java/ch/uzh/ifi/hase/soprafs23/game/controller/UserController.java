package ch.uzh.ifi.hase.soprafs23.game.controller;

import ch.uzh.ifi.hase.soprafs23.game.entity.User;
import ch.uzh.ifi.hase.soprafs23.game.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs23.game.rest.dto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs23.game.rest.dto.UserPutDTO;
import ch.uzh.ifi.hase.soprafs23.game.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs23.game.service.UserService;
import ch.uzh.ifi.hase.soprafs23.game.service.WebSocketService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

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


  UserController(UserService userService) {
    this.userService = userService;
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
    userService.greetUsers();

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

    // Send a message to all WebSocket subscribers in channel /users
    userService.greetUsers();

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

    // Send a message to all WebSocket subscribers in channel /users
    userService.greetUsers();

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

    // update the flag if the provided cioc is valid
    String newCIOC = userInput.getCioc();
    if (newCIOC != null) {
      userService.replaceFlagWithChosen(id, newCIOC);
    }

    // Send a message to all WebSocket subscribers in channel /users
    userService.greetUsers();

  }

  @DeleteMapping("/users/{userId}")
  @ResponseStatus(HttpStatus.ACCEPTED)
  @ResponseBody
  public void deleteUser(@PathVariable("userId") Long userId,
                         @RequestHeader(value="Authorization") String token,
                         @RequestHeader(value="Password") String password,
                         @RequestHeader(value="Username") String username) {
    // check if token was provided
    if (token == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
              "A token is required for deleting a user!");
    }

    if (username == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
              "Username is required for deleting a user!");
    }

    if (password == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
              "Password is required for deleting a user!");
    }

    // letting userService handle the deletion
    userService.deleteUser(userId, username, password, token);

    // Send a message to all WebSocket subscribers in channel /users
    userService.greetUsers();
  }

  @DeleteMapping("/users/{userId}/flag")
  @ResponseStatus(HttpStatus.ACCEPTED)
  @ResponseBody
  public void deleteFlag(@PathVariable("userId") Long userId,
                         @RequestHeader(value="Authorization") String token) {
    // check if token was provided
    if (token == null) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
              "A token is required for deleting a user!");
    }

    userService.replaceFlagRandomly(userId);
  }
}