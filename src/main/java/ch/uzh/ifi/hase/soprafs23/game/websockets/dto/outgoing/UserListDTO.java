package ch.uzh.ifi.hase.soprafs23.game.websockets.dto.outgoing;

import ch.uzh.ifi.hase.soprafs23.game.entity.User;
import java.util.List;

public class UserListDTO {
    public UserListDTO() {}

    public static String buildUserList(List<User> userList) {
        StringBuilder usersString = new StringBuilder("{");

        // join all users together in a string
        for(User user: userList) {
            usersString.append(user.toString());
            usersString.append(",");
        }

        // remove the last ","
        if (usersString.length() > 0) {
            usersString.setLength(usersString.length() - 1);
        }

        usersString.append("}");

        return usersString.toString();
    }
}
