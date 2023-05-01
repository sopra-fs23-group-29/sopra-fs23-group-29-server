package ch.uzh.ifi.hase.soprafs23.game.service;

import ch.uzh.ifi.hase.soprafs23.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs23.game.entity.Country;
import ch.uzh.ifi.hase.soprafs23.game.entity.User;
import ch.uzh.ifi.hase.soprafs23.game.questions.restCountry.CountryService;
import ch.uzh.ifi.hase.soprafs23.game.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs23.game.websockets.dto.outgoing.UserListDTO;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;

import static ch.uzh.ifi.hase.soprafs23.game.service.QuestionServiceRestcountries.CIOC_CODES;

/**
 * User Service
 * This class is the "worker" and responsible for all functionality related to
 * the user
 * (e.g., it creates, modifies, deletes, finds). The result will be passed back
 * to the caller.
 */
@Service
@Transactional
public class UserService {

    private final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final WebSocketService webSocketService;
    private final UserListDTO userListDTO;
    private final CountryService countryService = new CountryService();

    @Autowired
    public UserService(@Qualifier("userRepository") UserRepository userRepository, WebSocketService webSocketService) {
        this.userRepository = userRepository;
        this.webSocketService = webSocketService;
        this.userListDTO = new UserListDTO();
    }

    /**
     *
     */
    public String getUserListAsString() {
        return userListDTO.buildUserList(userRepository.findAll());
    }

    /**
     * Set STATUS to UserStatus.ONLINE
     * @param id The id of the user to change status to ONLINE
     * @return The user changed status to ONLINE
     */
    public User setUserOnline(Long id) {
        User userToUpdate = getUserById(id);

        userToUpdate.setStatus(UserStatus.ONLINE);

        // save entry back to db
        this.userRepository.save(userToUpdate);
        userRepository.flush();

        return userToUpdate;

    }

    /**
     * Set STATUS to UserStatus.ONLINE
     * @param id The id of the user to change status to ONLINE
     * @return The user changed status to ONLINE
     */
    public User setUserOffline(Long id) {
        User userToUpdate = getUserById(id);

        userToUpdate.setStatus(UserStatus.OFFLINE);

        // save entry back to db
        this.userRepository.save(userToUpdate);
        userRepository.flush();

        return userToUpdate;

    }

    public List<User> getUsers() {
        return this.userRepository.findAll();
    }

    /**
     * Check if a user exists and if his password is correct
     * Check for existence is based on
     * username
     *
     * Check for password is based on
     * password
     *
     * Return the user if found
     *
     * @param userToCheck
     * @throws org.springframework.web.server.ResponseStatusException
     * @return User
     */
    public User checkLogin(User userToCheck) {

        String usernameToCheck = userToCheck.getUsername();
        String passwordToCheck = userToCheck.getPassword();

        User userSearched = this.userRepository.findByUsername(usernameToCheck);

        // check if the username exists in the db
        if (userSearched == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("User with username %s not found", usernameToCheck));
        }

        // fetch the password
        String userSearchedPassword = userSearched.getPassword();

        // compare to the supplied password
        if (!Objects.equals(passwordToCheck, userSearchedPassword)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Incorrect password provided!"
            );
        }

        User userToReturn = setUserOnline(userSearched.getId());

        return userToReturn;

    }

    /**
     * Update a user in the db. Always updates both
     * username
     * birthday
     *
     * Throws error NOT_FOUND if user is not found in db
     * Throws error CONFLICT if new username is already in use
     *
     * @param idToUpdate The id of the user to fetch if exists which will be updated
     * @param newUsername The new username to set on the user entity
     * @param newBirthday The new birthday to set on the user entity
     * @throws org.springframework.web.server.ResponseStatusException
     */
    public User updateUser(Long idToUpdate, String newUsername, String newBirthday) {

        // find the user to update, throw error if id does not exist
        User userToUpdate = getUserById(idToUpdate);

        // compare the new username to all existing usernames, throw error if already taken
        // only if the newUsername parameter is not NULL
        if (newUsername != null) {
            List<User> allUsers = getUsers();
            for (User user : allUsers) {
                if (user.getUsername().equals(newUsername)) {
                    throw new ResponseStatusException(
                            HttpStatus.CONFLICT,
                            String.format("Username %s is already in use, cannot create duplicated usernames", newUsername)
                    );
                }
            }
        }

        // if any of the new attributes is null, use the old one
        if (newUsername == null) {
            newUsername = userToUpdate.getUsername();
        }

        if (newBirthday == null) {
            newBirthday = userToUpdate.getBirthday();
        }

        // update the user attributes
        userToUpdate.setUsername(newUsername);
        userToUpdate.setBirthday(newBirthday);

        // save entry back to db
        this.userRepository.save(userToUpdate);
        userRepository.flush();

        return userToUpdate;

    }

    public void deleteUser(Long userId, String reqUsername, String reqPassword, String token) {
        User userToDelete = getUserById(userId);

        // check that the usernames match
        if (!userToDelete.getUsername().equals(reqUsername)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "The usernames do not match");
        }

        // check that the passwords match
        if (!userToDelete.getPassword().equals(reqPassword)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "The passwords do not match");
        }

        // check if the token provided authorizes to delete the user with the given id
        if (!userToDelete.getToken().equals(token)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "The given token does not authorize to delete this user.");
        }

        // user exists and will be deleted
        userRepository.delete(userToDelete);
        userRepository.flush();

        log.debug("Deleted Information for User: {}", userToDelete);
    }

    /**
     * Get a single user object by
     * id
     *
     * @param id Number of the user ID to be retrieved
     * @throws org.springframework.web.server.ResponseStatusException
     * @return User
     */
    public User getUserById(Long id) {
        User userSearched = this.userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                String.format("User with ID %s not found", id)
                        )
                );

        return userSearched;
    }

    /**
     * Get a single user object by
     * token
     *
     * @param token String Token of the user to be fetched
     * @throws org.springframework.web.server.ResponseStatusException
     * @return User
     */
    public User getUserByToken(String token) {
        User userSearched = this.userRepository.findByToken(token);

        if (userSearched == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    String.format("User with Token %s not found", token)
            );
        }

        return userSearched;
    }

    public User createUser(User newUser) {

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDateTime now = LocalDateTime.now();

        newUser.setToken(UUID.randomUUID().toString());
        newUser.setStatus(UserStatus.ONLINE); // set a new user online by default
        newUser.setCreationDate(dtf.format(now));
        newUser.setBirthday(""); // set the birthday for new user to an empty string
        newUser.setFlagURL(this.getRandomFlagURL());

        checkIfUserDuplicated(newUser);
        // saves the given entity but data is only persisted in the database once
        // flush() is called
        newUser = userRepository.save(newUser);
        userRepository.flush();

        log.debug("Created Information for User: {}", newUser);
        return newUser;
    }

    /**
     * This is a helper method that will check the uniqueness criteria of the
     * username
     * defined in the User entity. The method will do nothing if the input is unique
     * and throw an error otherwise.
     *
     * @param userToBeCreated
     * @throws org.springframework.web.server.ResponseStatusException
     * @see User
     */
    private void checkIfUserDuplicated(User userToBeCreated) {
        User userByUsername = userRepository.findByUsername(userToBeCreated.getUsername());

        String baseErrorMessage = "The %s provided %s not unique. Therefore, the user could not be created or username changed!";
        if (userByUsername != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, String.format(baseErrorMessage, "username", "is"));
        }
    }

    /**
     * Check if a user with the token tokenToCheck exists in the database, throw error otherwise
     * If idtoCheck is supplied, further check if the user returned by tokenToCheck
     * matches the user returned by idToChieck, throw error otherwise
     *
     * @param tokenToCheck
     * @param idToCheck
     * @throws ResponseStatusException if token does not return user or if token and id don't match
     * @return True if succesfull.
     */
    public boolean checkToken(String tokenToCheck, Long idToCheck) {
        // check if a token has been provided
        if (tokenToCheck == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "No token was provided.");
        }
        User userByToken = getUserByToken(tokenToCheck);

        // check if the token returns any user
        if (userByToken == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    String.format("Token does not correspond to any user. \nToken %s", tokenToCheck));
        }

        // optional, if idToCheck != null, check if that token matches the user id
        if (idToCheck != null) {
            if (!userByToken.getId().equals(idToCheck)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        String.format("Wrong token provided\nToken %s", tokenToCheck));
            }
        }

        return true;
    }

    public void greetUsers() {

        // fetch all users in the internal representation
        List<User> users = getUsers();
        List<User> tmpUsers = new ArrayList<>();

        // Copy the user object, remove token and password before sending
        for (User user : users) {

            // Create a temporary copy of the user, without password and token
            User tmpUser = new User();
            tmpUser.setId(user.getId());
            tmpUser.setStatus(user.getStatus());
            tmpUser.setUsername(user.getUsername());
            tmpUser.setBirthday(user.getBirthday());
            tmpUser.setCreationDate(user.getCreationDate());

            // Add temporary User to list
            tmpUsers.add(tmpUser);

        }

        String tmpUserListAsString = new Gson().toJson(tmpUsers);
        webSocketService.sendMessageToClients("/topic/users", tmpUserListAsString);
    }

    public void replaceFlagRandomly(Long id) {
        // get user
        User userToChangeFlag = this.getUserById(id);

        // replace flag with a new random one
        userToChangeFlag.setFlagURL(this.getRandomFlagURL());
    }

    public void replaceFlagWithChosen(Long id, String iocCode) {
        // get user
        User userToChangeFlag = this.getUserById(id);

        // get new flagURl
        Country chosenCountry = countryService.getCountryData(iocCode);
        if (chosenCountry == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    String.format("IOC code does not correspond to a country. \nIOC code %s", iocCode));
        }

        userToChangeFlag.setFlagURL((chosenCountry.getFlagUrl()));
    }

    public String getRandomFlagURL() {
        while (true) {
            // get random country cioc
            int index = (int) (Math.random() * CIOC_CODES.length);
            String ciocCode = CIOC_CODES[index];

            // get flag of that country
            Country tempCountry = countryService.getCountryData(ciocCode);
            if (tempCountry == null) {
                // there seems to be a problem where sometimes tempCountry is null, not sure why
                continue;
            }
            String url = tempCountry.getFlagUrl();

            // check that we got a url
            if (url != null) {
                return url;
            }
        }
    }
}