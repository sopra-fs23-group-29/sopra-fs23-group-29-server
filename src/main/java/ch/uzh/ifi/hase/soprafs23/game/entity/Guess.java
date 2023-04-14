package ch.uzh.ifi.hase.soprafs23.game.entity;

import ch.uzh.ifi.hase.soprafs23.constant.PlayerColor;

/**
 * This class represents a taken Guess and consists of
 * Who (Long playerId)
 * Which colour (PlayerColor of playerId)
 * Which country (String countryCode)
 * Guess (int)
 */
public record Guess(Long guessPlayerId, PlayerColor guessPlayerColor, String guessCountryCode, int guess) {

}
