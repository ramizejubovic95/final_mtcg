package stats;

import dbManagement.DbManagement;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
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

    public void updateScoreBoard() throws SQLException
    {
        DbManagement db = new DbManagement();
        this.setScoreBoard(db.updateScoreBoard());
    }

    public void print()
    {
        for (int i = 0; i < this.getScoreBoard().size(); i++)
        {
            System.out.println(i+1 + " place " + "-> " + this.getScoreBoard().get(i).getNameHandleOfUser() + " with " + this.getScoreBoard().get(i).getEloPoints());
        }
    }
}
