package ch.uzh.ifi.hase.soprafs23.game.websockets.dto.outgoing;

import ch.uzh.ifi.hase.soprafs23.constant.PlayerColor;
import ch.uzh.ifi.hase.soprafs23.game.entity.Player;
import ch.uzh.ifi.hase.soprafs23.game.questions.restCountry.BarrierQuestion;

/**
 * Receive a barrier question and a player ID and pack them up to send to the client
 */
public class BarrierQuestionOutgoingDTO {

    private final BarrierQuestion barrierQuestion;
    private final Player playerAnswering;

    public BarrierQuestionOutgoingDTO(BarrierQuestion barrierQuestion, Player playerAnswering) {
        this.barrierQuestion = barrierQuestion;
        this.playerAnswering = playerAnswering;
    }
}
