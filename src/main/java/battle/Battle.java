package battle;

import card.Card;
import dbManagement.DbManagement;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import response.ResponseHandler;
import user.User;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
    private Random random;

    public Battle(User fighter1, User fighter2, ResponseHandler response)
    {
        this.setFighter1(fighter1.getId());
        this.setFighter2(fighter2.getId());
        this.response = response;
    }

    public Battle fight(User fighter1, User fighter2) throws SQLException
    {
        this.response.reply("Lets FIIIIIIIGHT!");
        boolean fighter1UsedSpeciality = false;
        boolean fighter2UsedSpeciality = false;
        random = new Random();

        int winsFighter1 = 0;
        int winsFighter2 = 0;
        int lossesFighter1 = 0;
        int lossesFighter2 = 0;

        int battleCounter = 0;
        int battleRounds = 0;
        while (battleRounds != 100 && !fighter1.getDeck().isEmpty() && !fighter2.getDeck().isEmpty())
        {
            this.response.reply("CARDNAME FIGHTER1: " + fighter1.getDeck().get(0).getCardName());
            this.response.reply("CARDNAME FIGHTER2: " + fighter2.getDeck().get(0).getCardName());

            if (fighter1.getDeck().size() < 2 && !fighter1UsedSpeciality && !fighter2UsedSpeciality)
            {
                List<Card> switchDeck = new ArrayList<>();
                switchDeck = fighter2.getDeck();

                if (random.nextInt(3) == 0)
                {
                    this.response.reply(fighter1.getUsername() + " is activating KARTENDIEB!");
                    fighter2 = kartenDieb(fighter2, fighter1.getDeck());
                    fighter1 = kartenDieb(fighter1, switchDeck);
                    fighter1UsedSpeciality = true;
                }
                if (random.nextInt(3) == 1)
                {
                    this.response.reply(fighter1.getUsername() + " is activating ROUNDSKIP");
                    fighter1UsedSpeciality = true;
                    battleRounds = battleRounds + 5;
                    continue;

                }
                if (random.nextInt(3) == 2)
                {
                    this.response.reply(fighter1.getUsername() + " is activating NOTHING");
                    fighter1UsedSpeciality = true;
                }
            }
            if (fighter2.getDeck().size() < 2 && !fighter2UsedSpeciality && !fighter1UsedSpeciality)
            {
                List<Card> switchDeck = new ArrayList<>();
                switchDeck = fighter1.getDeck();

                random = new Random();

                if (random.nextInt(3) == 0)
                {
                    this.response.reply(fighter2.getUsername() + " is activating KARTENDIEB!");
                    fighter1 = kartenDieb(fighter1, fighter2.getDeck());
                    fighter2 = kartenDieb(fighter2, switchDeck);
                    fighter2UsedSpeciality = true;
                }
                if (random.nextInt(3) == 1)
                {
                    this.response.reply(fighter2.getUsername() + " is activating ROUNDSKIP");
                    fighter2UsedSpeciality = true;
                    battleRounds = battleRounds + 5;
                    continue;
                }
                if (random.nextInt(3) == 2)
                {
                    this.response.reply(fighter2.getUsername() + " is activating NOTHING");
                    fighter2UsedSpeciality = true;
                }
            }

            // Here is the fight
            // whoLosses = 1 -> fighter1 verliert, wholosses = 0 -> fighter2 verliert
            int whoLoses = random.nextInt(2);
            if (whoLoses == 1)
            {
                this.response.reply(fighter1.getUsername() + " has lost his monster! Only " + fighter1.getDeck().size() + " Monsters left.");
                fighter2.getDeck().add(fighter1.getDeck().get(0));
                fighter1.getDeck().remove(0);

                battleCounter++;
                lossesFighter1++;
                winsFighter2++;
            }
            if (whoLoses == 0)
            {
                this.response.reply(fighter2.getUsername() + " has lost his monster! Only " + fighter2.getDeck().size() + " Monsters left.");
                fighter1.getDeck().add(fighter2.getDeck().get(0));
                fighter2.getDeck().remove(0);

                battleCounter++;
                lossesFighter2++;
                winsFighter1++;
            }

            battleRounds++;
        }

        if (winsFighter1 > winsFighter2)
        {
            this.setWinner(fighter1.getId());
            this.setLoser(fighter2.getId());
            this.setFinished(true);

            fighter1.setEloPoints(fighter1.getEloPoints() + 3);
            fighter1.setWins(fighter1.getWins() + 1);
            fighter1.setTotalBattles(fighter1.getTotalBattles() + 1);

            fighter2.setEloPoints(fighter2.getEloPoints() - 5);
            fighter2.setLosses(fighter2.getLosses() + 1);
            fighter2.setTotalBattles(fighter2.getTotalBattles() + 1);

            DbManagement db = new DbManagement();
            db.saveLastUserData(fighter1);
            db.saveLastUserData(fighter2);
        }
        else if (winsFighter1 < winsFighter2)
        {
            this.setWinner(fighter2.getId());
            this.setLoser(fighter1.getId());
            this.setFinished(true);

            fighter2.setEloPoints(fighter2.getEloPoints() + 3);
            fighter2.setWins(fighter2.getWins() + 1);
            fighter2.setTotalBattles(fighter2.getTotalBattles() + 1);

            fighter1.setEloPoints(fighter1.getEloPoints() - 5);
            fighter1.setLosses(fighter1.getLosses() + 1);
            fighter1.setTotalBattles(fighter1.getTotalBattles() + 1);

            DbManagement db = new DbManagement();
            db.saveLastUserData(fighter1);
            db.saveLastUserData(fighter2);
        }

        else if (winsFighter1 == winsFighter2)
        {
            this.setDraws(true);
            this.setFinished(true);

            fighter1.setDraws(fighter1.getDraws() + 1);
            fighter2.setDraws(fighter2.getDraws() + 1);
            fighter1.setTotalBattles(fighter1.getTotalBattles() + 1);
            fighter2.setTotalBattles(fighter2.getTotalBattles() + 1);

            DbManagement db = new DbManagement();
            db.saveLastUserData(fighter1);
            db.saveLastUserData(fighter2);
        }

        return this;

    }

    public User kartenDieb(User fighter, List<Card> switchDeck)
    {
        fighter.getDeck().clear();
        fighter.setDeck(switchDeck);
        return fighter;
    }


}
