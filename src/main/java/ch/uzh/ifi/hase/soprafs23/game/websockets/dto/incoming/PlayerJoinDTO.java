package ch.uzh.ifi.hase.soprafs23.game.websockets.dto.incoming;

public class PlayerJoinDTO {

    private Long id;
    private String playerName;
    private String userToken;

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public String getPlayerName() {return playerName;}
    public void setPlayerName() {this.playerName = playerName;}

    public String getUserToken() {
        return userToken;
    }
    public void setUserToken(String userToken) {
        this.userToken = userToken;
    }
}
