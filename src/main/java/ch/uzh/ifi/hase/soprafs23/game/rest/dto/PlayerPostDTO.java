package ch.uzh.ifi.hase.soprafs23.game.rest.dto;

import ch.uzh.ifi.hase.soprafs23.constant.PlayerColor;

public class PlayerPostDTO {



    private String userToken;

    public String getUserToken() {
        return userToken;
    }

    public void setUserToken(String userToken) {
        this.userToken = userToken;
    }
}
