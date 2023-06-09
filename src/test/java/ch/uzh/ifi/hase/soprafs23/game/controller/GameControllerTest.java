package ch.uzh.ifi.hase.soprafs23.game.controller;

import ch.uzh.ifi.hase.soprafs23.constant.*;
import ch.uzh.ifi.hase.soprafs23.game.entity.*;
import ch.uzh.ifi.hase.soprafs23.game.questions.IQuestionService;
import ch.uzh.ifi.hase.soprafs23.game.questions.restCountry.BarrierQuestion;
import ch.uzh.ifi.hase.soprafs23.game.questions.restCountry.CountryService;
import ch.uzh.ifi.hase.soprafs23.game.questions.restCountry.RankingQuestion;
import ch.uzh.ifi.hase.soprafs23.game.repository.GameRepository;
import ch.uzh.ifi.hase.soprafs23.game.rest.dto.GamePostDTO;
import ch.uzh.ifi.hase.soprafs23.game.service.GameService;
import ch.uzh.ifi.hase.soprafs23.game.service.PlayerService;
import ch.uzh.ifi.hase.soprafs23.game.service.UserService;
import ch.uzh.ifi.hase.soprafs23.game.service.WebSocketService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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

    @MockBean
    private IQuestionService questionService;
    @MockBean
    private CountryService countryService;

    private IQuestionService dummyQuestionService;

    class DummyQuestionService implements IQuestionService {
        @Override
        public RankingQuestion generateRankQuestion(int size) {
            // always return rankingQuestion of size 1 with same countryCode
            List<Country> dummyList = new ArrayList<>();
            dummyList.add(countryService.getCountryData("GER"));
            return new RankingQuestion(RankingQuestionEnum.AREA, dummyList);
        }

        @Override
        public BarrierQuestion generateBarrierQuestion() {
            BarrierQuestion dummyBarrierQuestion = new BarrierQuestion(
              BarrierQuestionEnum.LANDLOCKED,
              countryService.getCountryData("GER"),
              new ArrayList<>()
            );
            return dummyBarrierQuestion;
        }
    }





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
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$", notNullValue()))
                .andExpect(jsonPath("$.*", hasSize(1)))
                .andExpect(jsonPath("$.gameId", is(1)))
                ;

    }

    @Test
    void createGame_playerAlreadyInGame_throwsConflict() throws Exception {

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
        Player p2 = new Player();
        p2.setIsHost(true);
        p2.setGameId(2L);
        p2.setPlayerColor(PlayerColor.ORANGE);
        p2.setToken("p2token");
        p2.setUserToken("p2userToken");
        p2.setId(2L);
        p2.setPlayerName("p2");

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
        given(playerService.getPlayerByUserToken("dummy")).willReturn(p2);

        // when
        MockHttpServletRequestBuilder postRequest = post("/games")
          .contentType(MediaType.APPLICATION_JSON)
          .header("Authorization", "dummy")
          .content(asJsonString(gamePostDTO));

        // then
        mockMvc.perform(postRequest).andExpect(status().isConflict())
//          .andDo(MockMvcResultHandlers.print())
        ;

    }

    @Test
    void joinGame_thenGetPlayer() throws Exception {
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

    @Test
    void joinGame_playerAlreadyInGame_throwsConflict() throws Exception {
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
        Player p2 = new Player();
        p2.setIsHost(true);
        p2.setGameId(2L);
        p2.setPlayerColor(PlayerColor.ORANGE);
        p2.setToken("p2token");
        p2.setUserToken("p2userToken");
        p2.setId(2L);
        p2.setPlayerName("p2");

        // given - add the game to the GameRepository
        GameRepository.addGame(game.getGameId(), game);

        // given - add the user to the UserRepository
        userService.createUser(user);

        // mock the services
        given(gameService.createNewGame("game1", GameMode.PVP, BoardSize.SMALL, MaxDuration.NA)).willReturn(game.getGameId());
        given(userService.getUserByToken("dummy")).willReturn(user);
        given(playerService.getPlayerByUserToken("dummy")).willReturn(p2);
        given(playerService.joinPlayer("dummy", game.getGameId().intValue())).willReturn(p1);

        // when
        MockHttpServletRequestBuilder postRequest = post("/games/1")
          .contentType(MediaType.APPLICATION_JSON)
          .header("Authorization", "dummy");

        // then
        mockMvc.perform(postRequest).andExpect(status().isConflict())
          .andDo(MockMvcResultHandlers.print())
        ;
    }

    @Test
    void leaveGame_isOk() throws Exception {
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
        User u1 = new User();
        u1.setPassword("password");
        u1.setUsername("p1");
        u1.setStatus(UserStatus.OFFLINE);
        u1.setToken("dummy");

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
        // given - add the player to the Game
        gameService.createNewGame("game1", GameMode.PVP, BoardSize.SMALL, MaxDuration.NA);
        playerService.joinPlayer("dummy", 1);

        given(userService.getUserByToken("dummy")).willReturn(u1);
        given(gameService.getGameById(1L)).willReturn(game);
        given(playerService.getPlayerByUserToken("dummy")).willReturn(p1);

        // when
        MockHttpServletRequestBuilder postRequest = delete("/games/1")
          .contentType(MediaType.APPLICATION_JSON)
          .header("Authorization", "dummy")
          ;

        // then
        mockMvc.perform(postRequest).andExpect(status().isOk())
        ;
    }

    @Test
    void leaveGame_notOnlyPlayerOrHost_isOk() throws Exception {
        // given
        Player p1 = new Player();
        p1.setIsHost(false);
        p1.setGameId(1L);
        p1.setPlayerColor(PlayerColor.INDIANRED);
        p1.setToken("p1token");
        p1.setUserToken("dummy");
        p1.setId(1L);
        p1.setPlayerName("p1");

        // given
        Player p2 = new Player();
        p2.setIsHost(true);
        p2.setGameId(2L);
        p2.setPlayerColor(PlayerColor.ORANGE);
        p2.setToken("p2token");
        p2.setUserToken("p2userToken");
        p2.setId(2L);
        p2.setPlayerName("p2");

        // given
        User u1 = new User();
        u1.setPassword("password");
        u1.setUsername("p1");
        u1.setStatus(UserStatus.OFFLINE);
        u1.setToken("dummy");

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
        // given - add the player to the Game
        gameService.createNewGame("game1", GameMode.PVP, BoardSize.SMALL, MaxDuration.NA);
        playerService.joinPlayer("p2userToken", 1);
        playerService.joinPlayer("dummy", 1);

        given(userService.getUserByToken("dummy")).willReturn(u1);
        given(gameService.getGameById(1L)).willReturn(game);
        given(playerService.getPlayerByUserToken("dummy")).willReturn(p1);

        // when
        MockHttpServletRequestBuilder postRequest = delete("/games/1")
          .contentType(MediaType.APPLICATION_JSON)
          .header("Authorization", "dummy")
          ;

        // then
        mockMvc.perform(postRequest).andExpect(status().isOk())
        ;
    }

    @Test
    void leaveGame_playerNoToken_throwsNotFound() throws Exception {
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
        User u1 = new User();
        u1.setPassword("password");
        u1.setUsername("p1");
        u1.setStatus(UserStatus.OFFLINE);
        u1.setToken("dummy");

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
        // given - add the player to the Game
        gameService.createNewGame("game1", GameMode.PVP, BoardSize.SMALL, MaxDuration.NA);
        playerService.joinPlayer("dummy", 1);

        given(userService.getUserByToken("dummy")).willReturn(u1);
        given(gameService.getGameById(1L)).willReturn(game);
        given(playerService.getPlayerByUserToken("dummy")).willReturn(null);

        // when
        MockHttpServletRequestBuilder postRequest = delete("/games/1")
          .contentType(MediaType.APPLICATION_JSON)
          .header("Authorization", "dummy")
          ;

        // then
        mockMvc.perform(postRequest).andExpect(status().isNotFound())
        ;
    }

    @Test
    void leaveGame_playerInDifferentGame_throwsBadRequest() throws Exception {
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
        Player p2 = new Player();
        p2.setIsHost(true);
        p2.setGameId(2L);
        p2.setPlayerColor(PlayerColor.ORANGE);
        p2.setToken("p2token");
        p2.setUserToken("p2userToken");
        p2.setId(2L);
        p2.setPlayerName("p2");

        // given
        User u1 = new User();
        u1.setPassword("password");
        u1.setUsername("p1");
        u1.setStatus(UserStatus.OFFLINE);
        u1.setToken("dummy");

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
        // given - add the player to the Game
        gameService.createNewGame("game1", GameMode.PVP, BoardSize.SMALL, MaxDuration.NA);
        playerService.joinPlayer("dummy", 1);

        given(userService.getUserByToken("dummy")).willReturn(u1);
        given(gameService.getGameById(1L)).willReturn(game);
        given(playerService.getPlayerByUserToken("dummy")).willReturn(p2);

        // when
        MockHttpServletRequestBuilder postRequest = delete("/games/1")
          .contentType(MediaType.APPLICATION_JSON)
          .header("Authorization", "dummy")
          ;

        // then
        mockMvc.perform(postRequest).andExpect(status().isBadRequest())
        ;
    }

    @Test
    void leaveGame_playerIsHostAndLast_closeGame_ok() throws Exception {
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
        User u1 = new User();
        u1.setPassword("password");
        u1.setUsername("p1");
        u1.setStatus(UserStatus.OFFLINE);
        u1.setToken("dummy");

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
        // given - add the player to the Game
        gameService.createNewGame("game1", GameMode.PVP, BoardSize.SMALL, MaxDuration.NA);
        playerService.joinPlayer("dummy", 1);

        given(userService.getUserByToken("dummy")).willReturn(u1);
        given(gameService.getGameById(1L)).willReturn(game);
        given(playerService.getPlayerByUserToken("dummy")).willReturn(p1);

        // when
        MockHttpServletRequestBuilder postRequest = delete("/games/1")
          .contentType(MediaType.APPLICATION_JSON)
          .header("Authorization", "dummy")
          ;

        // then
        mockMvc.perform(postRequest).andExpect(status().isOk());
    }

    @Test
    void leaveGame_gameInProgress_ok() throws Exception {

        // given
        User u1 = new User();
        u1.setPassword("password");
        u1.setUsername("p1");
        u1.setStatus(UserStatus.OFFLINE);
        u1.setToken("dummy");

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
        Player p2 = new Player();
        p2.setIsHost(true);
        p2.setGameId(2L);
        p2.setPlayerColor(PlayerColor.ORANGE);
        p2.setToken("p2token");
        p2.setUserToken("p2userToken");
        p2.setId(2L);
        p2.setPlayerName("p2");

        // dummy question service
        DummyQuestionService dummyQuestionService = new DummyQuestionService();

        // given fake turn
        Turn turn = new Turn(1, Arrays.asList(p1,p2), dummyQuestionService.generateRankQuestion(1));

        Game game = new Game(1L, "game1", GameMode.PVP, BoardSize.SMALL, MaxDuration.NA, playerService, dummyQuestionService);
        game.setGameStatus(GameStatus.INPROGRESS);
        game.setTurn(turn);

        // setup mock answers
        given(userService.getUserByToken("dummy")).willReturn(u1);
        given(gameService.getGameById(1L)).willReturn(game);
        given(playerService.getPlayerByUserToken("dummy")).willReturn(p1);
        given(playerService.getPlayersByGameId(1L)).willReturn(Arrays.asList(p1,p2));


        // given - add the game to the GameRepository
        GameRepository.addGame(game.getGameId(), game);
        // given - add the players to the Game
//        gameService.createNewGame("game1", GameMode.PVP, BoardSize.SMALL, MaxDuration.NA);
        playerService.joinPlayer("dummy", 1);
        playerService.joinPlayer("p2userToken", 1);

        gameService.updatePlayers(game.getGameId());
        gameService.startGame(game.getGameId());
        gameService.startNextTurn(game.getGameId());



        // when
        MockHttpServletRequestBuilder postRequest = delete("/games/1")
          .contentType(MediaType.APPLICATION_JSON)
          .header("Authorization", "dummy")
          ;

        // then
        mockMvc.perform(postRequest).andExpect(status().isOk());

    }



    @Test
    void leaveAllGames_isOk() throws Exception {
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
        User u1 = new User();
        u1.setPassword("password");
        u1.setUsername("p1");
        u1.setStatus(UserStatus.OFFLINE);
        u1.setToken("dummy");

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
        // given - add the player to the Game
        gameService.createNewGame("game1", GameMode.PVP, BoardSize.SMALL, MaxDuration.NA);
        playerService.joinPlayer("dummy", 1);

        given(userService.getUserByToken("dummy")).willReturn(u1);
        given(gameService.getGameById(1L)).willReturn(game);
        given(playerService.getPlayerByUserToken("dummy")).willReturn(p1);

        // when
        MockHttpServletRequestBuilder postRequest = delete("/games")
          .contentType(MediaType.APPLICATION_JSON)
          .header("Authorization", "dummy")
          ;

        // then
        mockMvc.perform(postRequest).andExpect(status().isOk())
        ;
    }

    @Test
    void leaveAllGames_notLastPlayer_isOk() throws Exception {
        // given p1 not host
        Player p1 = new Player();
        p1.setIsHost(false);
        p1.setGameId(1L);
        p1.setPlayerColor(PlayerColor.INDIANRED);
        p1.setToken("p1token");
        p1.setUserToken("dummy");
        p1.setId(1L);
        p1.setPlayerName("p1");

        // given p2 host
        Player p2 = new Player();
        p2.setIsHost(true);
        p2.setGameId(2L);
        p2.setPlayerColor(PlayerColor.ORANGE);
        p2.setToken("p2token");
        p2.setUserToken("p2userToken");
        p2.setId(2L);
        p2.setPlayerName("p2");

        // given
        User u1 = new User();
        u1.setPassword("password");
        u1.setUsername("p1");
        u1.setStatus(UserStatus.OFFLINE);
        u1.setToken("dummy");

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
        // given - add the player to the Game
        gameService.createNewGame("game1", GameMode.PVP, BoardSize.SMALL, MaxDuration.NA);
        playerService.joinPlayer("dummy", 1);
        playerService.joinPlayer("p2userToken", 1);

        given(userService.getUserByToken("dummy")).willReturn(u1);
        given(gameService.getGameById(1L)).willReturn(game);
        given(playerService.getPlayerByUserToken("dummy")).willReturn(p1);

        // when
        MockHttpServletRequestBuilder postRequest = delete("/games")
          .contentType(MediaType.APPLICATION_JSON)
          .header("Authorization", "dummy")
          ;

        // then
        mockMvc.perform(postRequest).andExpect(status().isOk())
        ;
    }

    @Test
    void leaveAllGames_notInGame_throwsCONFLICT() throws Exception {
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
        User u1 = new User();
        u1.setPassword("password");
        u1.setUsername("p1");
        u1.setStatus(UserStatus.OFFLINE);
        u1.setToken("dummy");

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
        // given - DO NOT add the player to the game
        gameService.createNewGame("game1", GameMode.PVP, BoardSize.SMALL, MaxDuration.NA);

        given(userService.getUserByToken("dummy")).willReturn(u1);
        given(gameService.getGameById(1L)).willReturn(game);
        given(playerService.getPlayerByUserToken("dummy")).willReturn(null);

        // when
        MockHttpServletRequestBuilder postRequest = delete("/games")
          .contentType(MediaType.APPLICATION_JSON)
          .header("Authorization", "dummy")
          ;

        // then
        mockMvc.perform(postRequest).andExpect(status().isConflict())
        ;
    }

    @Test
    void leaveAllGames_gameInProgress_ok() throws Exception {

        // given
        User u1 = new User();
        u1.setPassword("password");
        u1.setUsername("p1");
        u1.setStatus(UserStatus.OFFLINE);
        u1.setToken("dummy");

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
        Player p2 = new Player();
        p2.setIsHost(true);
        p2.setGameId(2L);
        p2.setPlayerColor(PlayerColor.ORANGE);
        p2.setToken("p2token");
        p2.setUserToken("p2userToken");
        p2.setId(2L);
        p2.setPlayerName("p2");

        // dummy question service
        DummyQuestionService dummyQuestionService = new DummyQuestionService();

        // given fake turn
        Turn turn = new Turn(1, Arrays.asList(p1,p2), dummyQuestionService.generateRankQuestion(1));

        Game game = new Game(1L, "game1", GameMode.PVP, BoardSize.SMALL, MaxDuration.NA, playerService, dummyQuestionService);
        game.setGameStatus(GameStatus.INPROGRESS);
        game.setTurn(turn);

        // setup mock answers
        given(userService.getUserByToken("dummy")).willReturn(u1);
        given(gameService.getGameById(1L)).willReturn(game);
        given(playerService.getPlayerByUserToken("dummy")).willReturn(p1);
        given(playerService.getPlayersByGameId(1L)).willReturn(Arrays.asList(p1,p2));


        // given - add the game to the GameRepository
        GameRepository.addGame(game.getGameId(), game);
        // given - add the players to the Game
//        gameService.createNewGame("game1", GameMode.PVP, BoardSize.SMALL, MaxDuration.NA);
        playerService.joinPlayer("dummy", 1);
        playerService.joinPlayer("p2userToken", 1);

        gameService.updatePlayers(game.getGameId());
        gameService.startGame(game.getGameId());
        gameService.startNextTurn(game.getGameId());



        // when
        MockHttpServletRequestBuilder postRequest = delete("/games")
          .contentType(MediaType.APPLICATION_JSON)
          .header("Authorization", "dummy")
          ;

        // then
        mockMvc.perform(postRequest).andExpect(status().isOk());

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