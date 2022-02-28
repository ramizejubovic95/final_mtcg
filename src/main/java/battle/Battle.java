package battle;

import dbManagement.DbManagement;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import response.ResponseHandler;
import user.User;

import java.sql.SQLException;
import java.util.concurrent.BlockingQueue;
@NoArgsConstructor
@Getter
@Setter
public class Battle {
    private int battleId;
    private int fighter1;
    private int fighter2;
    private int winner;
    private int loser;
    private boolean draws;
    private boolean isFinished;
    private ResponseHandler response;

    public Battle(User fighter1, User fighter2, ResponseHandler response)
    {
        this.setFighter1(fighter1.getId());
        this.setFighter2(fighter2.getId());
        this.response = response;
    }

    public void searchForBattle(User user)
    {
        //if (= db.searchForBattle();)
        //return battle;
    }

    public Battle fight(User fighter1, User fighter2) throws SQLException
    {
        System.out.println("THIS IS BATTLE ID: " + this.getBattleId());
        for (int i = 0; i < fighter1.getDeck().size(); i++)
        {
            this.response.reply("CARDNAME FIGHTER1: " + fighter1.getDeck().get(i).getCardName() + "\r\n");
            this.response.reply("CARDNAME FIGHTER2: " + fighter2.getDeck().get(i).getCardName() + "\r\n");
        }

        this.setWinner(fighter1.getId());
        this.setLoser(fighter2.getId());
        this.setFinished(true);

        fighter1.setEloPoints(fighter1.getEloPoints() + 3);
        fighter1.setWins(fighter1.getWins() + 1);
        fighter1.setTotalBattles(fighter1.getTotalBattles() + 1);

        fighter2.setEloPoints(fighter2.getEloPoints() -5);
        fighter2.setLosses(fighter1.getLosses() + 1);
        fighter2.setTotalBattles(fighter2.getTotalBattles() + 1);

        DbManagement db = new DbManagement();
        db.saveLastUserData(fighter1);
        db.saveLastUserData(fighter2);

        return this;

    }
}
