package ch.uzh.ifi.hase.soprafs23.game.repository;

import ch.uzh.ifi.hase.soprafs23.constant.*;
import ch.uzh.ifi.hase.soprafs23.game.entity.Game;
import ch.uzh.ifi.hase.soprafs23.game.entity.Player;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

@DataJpaTest
class GameRepositoryIntegrationTest {

  @Autowired
  private TestEntityManager entityManager;

  @Autowired
  private UserRepository playerRepository;

  @Autowired
  private GameRepository gameRepository;

  @BeforeEach
  void initBeforeEach() {
    // todo
  }

  @Test
  void findByGamename() {
    // given - Create game

    // when - Search game

    // then - Make sure game is there

  }

  @Test
  void findByToken() {
    // given - Create game

    // when - Search game

    // then - Make sure game is there
  }

  @Test
  void findPlayers() {
    // given - Create game
    // given - Add player

    // when - Search game

    // then - Make sure game is there
  }
}
