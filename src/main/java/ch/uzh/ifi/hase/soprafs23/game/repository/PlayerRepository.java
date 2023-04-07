package ch.uzh.ifi.hase.soprafs23.game.repository;

import ch.uzh.ifi.hase.soprafs23.game.entity.Game;
import ch.uzh.ifi.hase.soprafs23.game.entity.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("playerRepository")
public interface PlayerRepository extends JpaRepository<Player, Long> {
  Player findByPlayerName(String username);
  Player findByToken(String token);
  List<Player> findByGameId(Long gameId);
  Player findByUserToken(String userToken);
  long deleteById(long playerId);

}
