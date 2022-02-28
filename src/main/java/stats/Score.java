package stats;

import dbManagement.DbManagement;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import response.ResponseHandler;
import user.User;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Getter
@Setter
public class Score
{
    private List<User> scoreBoard = new ArrayList<User>();
    private String Linend = "\r\n";

    public void updateScoreBoard() throws SQLException
    {
        DbManagement db = new DbManagement();
        this.setScoreBoard(db.updateScoreBoard());
    }

    public void print(ResponseHandler response)
    {
        for (int i = 0; i < this.getScoreBoard().size(); i++)
        {
            if (i == 0)
                response.reply("\r\n");

            response.reply(i+1 + " place " + "-> " + this.getScoreBoard().get(i).getNameHandleOfUser() + " with " + this.getScoreBoard().get(i).getEloPoints());
        }
    }
}
