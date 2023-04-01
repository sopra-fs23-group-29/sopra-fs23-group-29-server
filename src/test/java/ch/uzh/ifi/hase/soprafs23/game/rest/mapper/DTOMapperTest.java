package ch.uzh.ifi.hase.soprafs23.game.rest.mapper;

import ch.uzh.ifi.hase.soprafs23.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs23.game.entity.User;
import ch.uzh.ifi.hase.soprafs23.game.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs23.game.rest.dto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs23.game.rest.dto.UserPutDTO;
import ch.uzh.ifi.hase.soprafs23.game.rest.dto.UserDeleteDTO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * DTOMapperTest
 * Tests if the mapping between the internal and the external/API representation
 * works.
 */
public class DTOMapperTest {
  @Test
  public void testCreateUser_fromUserPostDTO_toUser_success() {
    // create UserPostDTO
    UserPostDTO userPostDTO = new UserPostDTO();
    userPostDTO.setPassword("password");
    userPostDTO.setUsername("username");

    // MAP -> Create user
    User user = DTOMapper.INSTANCE.convertUserPostDTOtoEntity(userPostDTO);

    // check content
    assertEquals(userPostDTO.getPassword(), user.getPassword());
    assertEquals(userPostDTO.getUsername(), user.getUsername());
  }

  @Test
  public void testGetUser_fromUser_toUserGetDTO_success() {
    // create User
    User user = new User();
    user.setPassword("Password");
    user.setUsername("firstname@lastname");
    user.setStatus(UserStatus.OFFLINE);
    user.setToken("1");

    // MAP -> Create UserGetDTO
    UserGetDTO userGetDTO = DTOMapper.INSTANCE.convertEntityToUserGetDTO(user);

    // check content
    assertEquals(user.getId(), userGetDTO.getId());
    assertEquals(user.getUsername(), userGetDTO.getUsername());
    assertEquals(user.getStatus(), userGetDTO.getStatus());
  }

  @Test
  public void testUpdateUser_success() {

    // create put DTO
    UserPutDTO userPutDTO = new UserPutDTO();
    userPutDTO.setBirthday("1990-01-01");
    userPutDTO.setUsername("testUsername");

    // MAP to User object
    User user = DTOMapper.INSTANCE.convertUserPutDTOtoEntity(userPutDTO);

    // check content
    assertEquals(userPutDTO.getBirthday(), user.getBirthday());
    assertEquals(userPutDTO.getUsername(), user.getUsername());

  }

  @Test
  public void testCreateUser_fromUserDeleteDTO_toUser_success() {
    // create UserPostDTO
    UserDeleteDTO userDeleteDTO = new UserDeleteDTO();
    userDeleteDTO.setUsername("username");
    userDeleteDTO.setPassword("password");

    // MAP -> Create user
    User user = DTOMapper.INSTANCE.convertUserDeleteDTOtoEntity(userDeleteDTO);

    // check content
    assertEquals(userDeleteDTO.getUsername(), user.getUsername());
    assertEquals(userDeleteDTO.getPassword(), user.getPassword());
  }
}