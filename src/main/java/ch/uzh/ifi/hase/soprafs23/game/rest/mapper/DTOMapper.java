package ch.uzh.ifi.hase.soprafs23.game.rest.mapper;

import ch.uzh.ifi.hase.soprafs23.game.entity.User;
import ch.uzh.ifi.hase.soprafs23.game.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs23.game.rest.dto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs23.game.rest.dto.UserPutDTO;
import ch.uzh.ifi.hase.soprafs23.game.rest.dto.UserDeleteDTO;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

/**
 * DTOMapper
 * This class is responsible for generating classes that will automatically
 * transform/map the internal representation
 * of an entity (e.g., the User) to the external/API representation (e.g.,
 * UserGetDTO for getting, UserPostDTO for creating)
 * and vice versa.
 * Additional mappers can be defined for new entities.
 * Always created one mapper for getting information (GET) and one mapper for
 * creating information (POST).
 */
@Mapper
public interface DTOMapper {

  DTOMapper INSTANCE = Mappers.getMapper(DTOMapper.class);

  @Mapping(source = "username", target = "username")
  @Mapping(source = "password", target = "password")
  @Mapping(source = "token", target = "token")
  User convertUserPostDTOtoEntity(UserPostDTO userPostDTO);

  @Mapping(source = "id", target = "id")
  @Mapping(source = "username", target = "username")
  @Mapping(source = "status", target = "status")
  @Mapping(source = "creationDate", target = "creationDate")
  @Mapping(source = "birthday", target = "birthday")
  UserGetDTO convertEntityToUserGetDTO(User user);

  @Mapping(source = "username", target = "username")
  @Mapping(source = "birthday", target = "birthday")
  @Mapping(source = "password", target = "password")
  User convertUserPutDTOtoEntity(UserPutDTO userPutDTO);

  @Mapping(source = "username", target = "username")
  @Mapping(source = "password", target = "password")
  User convertUserDeleteDTOtoEntity(UserDeleteDTO userDeleteDTO);
}