package ch.uzh.ifi.hase.soprafs23.game.controller;

import ch.uzh.ifi.hase.soprafs23.game.entity.User;
import ch.uzh.ifi.hase.soprafs23.game.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs23.game.rest.dto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs23.game.rest.dto.UserPutDTO;
import ch.uzh.ifi.hase.soprafs23.game.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs23.game.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

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


  @GetMapping("/users/{id}")
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public UserGetDTO getSingleUserByUsername(@PathVariable Long id, @RequestHeader("Authorization") String auth_token) {

    // check the auth_token
    userService.checkToken(auth_token, null);

    // fetch the single user in the internal representation
    User retrievedUser = userService.getUserById(id);

    // remove token information if the retrieved user does not match the token
    if (!retrievedUser.getToken().equals(auth_token)) {
      retrievedUser.setToken("");
    }

    // convert user to the API representation
    UserGetDTO retrievedUserDTO = DTOMapper.INSTANCE.convertEntityToUserGetDTO(retrievedUser);

    return retrievedUserDTO;
  }


  @GetMapping("/users")
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public List<UserGetDTO> getAllUsers(@RequestHeader("Authorization") String auth_token) {

    // check the auth_token
    userService.checkToken(auth_token, null);

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

  @PostMapping("/users")
  @ResponseStatus(HttpStatus.CREATED)
  @ResponseBody
  public UserGetDTO createUser(@RequestBody UserPostDTO userPostDTO) {
    // convert API user to internal representation
    User userInput = DTOMapper.INSTANCE.convertUserPostDTOtoEntity(userPostDTO);

    // create user
    User createdUser = userService.createUser(userInput);
    // convert internal representation of user back to API
    return DTOMapper.INSTANCE.convertEntityToUserGetDTO(createdUser);
  }

  @PostMapping("/login")
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public UserGetDTO checkLoginUser(@RequestBody UserPostDTO userPostDTO) {
    // convert API user to internal representation
    User userInput = DTOMapper.INSTANCE.convertUserPostDTOtoEntity(userPostDTO);

    // check login
    User checkedUser = userService.checkLogin(userInput);

    // set status of user to ONLINE
    userService.setUserOnline(checkedUser.getId());

    // convert internal representation of user back to API
    return DTOMapper.INSTANCE.convertEntityToUserGetDTO(checkedUser);
  }

  @PutMapping("/logout")
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
  public void updateUser(@PathVariable long id, @RequestBody UserPutDTO userPutDTO, @RequestHeader("Authorization") String auth_token) {

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
}
