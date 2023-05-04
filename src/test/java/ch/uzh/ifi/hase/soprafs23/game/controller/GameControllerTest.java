package ch.uzh.ifi.hase.soprafs23.game.controller;

import ch.uzh.ifi.hase.soprafs23.constant.*;
import ch.uzh.ifi.hase.soprafs23.game.entity.Game;
import ch.uzh.ifi.hase.soprafs23.game.entity.Player;
import ch.uzh.ifi.hase.soprafs23.game.entity.User;
import ch.uzh.ifi.hase.soprafs23.game.repository.GameRepository;
import ch.uzh.ifi.hase.soprafs23.game.rest.dto.GamePostDTO;
import ch.uzh.ifi.hase.soprafs23.game.service.GameService;
import ch.uzh.ifi.hase.soprafs23.game.service.PlayerService;
import ch.uzh.ifi.hase.soprafs23.game.service.UserService;
import ch.uzh.ifi.hase.soprafs23.game.service.WebSocketService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.web.server.ResponseStatusException;

import org.junit.jupiter.api.Assertions.*;

import static org.hamcrest.Matchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(GameController.class)
class GameControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private GameService gameService;

    @MockBean
    private PlayerService playerService;

    @MockBean
    private WebSocketService webSocketService;

    @BeforeEach
    void init() {
        GameRepository.clear();
    }

    @Test
    void createGame_thenGetGameId() throws Exception {

        // given
        Player p1 = new Player();
        p1.setIsHost(true);
        p1.setGameId(1L);
        p1.setPlayerColor(PlayerColor.INDIANRED);
        p1.setToken("p1token");
        p1.setUserToken("dummy");
        p1.setId(1L);
        p1.setPlayerName("p1");

        // given
        Game game = new Game();
        game.setGameId(1L);
        game.setGameName("game1");
        game.setGameMode(GameMode.PVP);
        game.setBoardSize(BoardSize.SMALL);
        game.setMaxDuration(MaxDuration.NA);
        game.setGameStatus(GameStatus.INLOBBY);

        // given - add the game to the GameRepository
        GameRepository.addGame(game.getGameId(), game);

        GamePostDTO gamePostDTO = new GamePostDTO();
        gamePostDTO.setGameName("game1");
        gamePostDTO.setGameMode(GameMode.PVP);
        gamePostDTO.setBoardSize(BoardSize.SMALL);
        gamePostDTO.setMaxDuration(MaxDuration.NA);

        given(gameService.createNewGame("game1", GameMode.PVP, BoardSize.SMALL, MaxDuration.NA)).willReturn(game.getGameId());
        given(playerService.joinPlayer("dummy", game.getGameId().intValue())).willReturn(p1);

        // when
        MockHttpServletRequestBuilder postRequest = post("/games")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "dummy")
                .content(asJsonString(gamePostDTO));

        // then
        mockMvc.perform(postRequest).andExpect(status().isCreated())
                .andDo(MockMvcResultHandlers.print())
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$", notNullValue()))
                .andExpect(jsonPath("$.*", hasSize(1)))
                .andExpect(jsonPath("$.gameId", is(1)))
                ;

    }

    @Test
    void createPlayer_thenGetPlayer() throws Exception {
        // given
        Game game = new Game();
        game.setGameId(1L);
        game.setGameName("game1");
        game.setGameMode(GameMode.PVP);
        game.setGameStatus(GameStatus.INLOBBY);

        User user = new User();
        user.setPassword("password");
        user.setUsername("username");
        user.setStatus(UserStatus.OFFLINE);
        user.setToken("1");

        Player player = new Player();
        player.setId(1L);
        player.setGameId(1L);
        player.setToken("1");
        player.setUserToken("1");

        // given - add the game to the GameRepository
        GameRepository.addGame(game.getGameId(), game);

        // given - add the user to the UserRepository
        userService.createUser(user);

        // mock the services
        given(gameService.createNewGame("game1", GameMode.PVP, BoardSize.SMALL, MaxDuration.NA)).willReturn(game.getGameId());
        given(userService.getUserByToken("dummy")).willReturn(user);
        given(playerService.joinPlayer("dummy", game.getGameId().intValue())).willReturn(player);
        // when
        MockHttpServletRequestBuilder postRequest = post("/games/1")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "dummy");

        // then
        mockMvc.perform(postRequest).andExpect(status().isOk())
                .andDo(MockMvcResultHandlers.print())
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$", notNullValue()))
                .andExpect(jsonPath("$.*", hasSize(6)))
                .andExpect(jsonPath("$.gameId", is(1)))
                .andExpect(jsonPath("$.token", is("1")))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.userToken", is("1")))
        ;
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
                    String.format("The request body could not be created.%s", e.toString()));
        }
    }
}