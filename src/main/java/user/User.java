package user;

import card.Card;
import com.fasterxml.jackson.annotation.JsonProperty;
import dbManagement.DbManagement;
import lombok.*;
import response.ResponseHandler;
import trading.Tradeable;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class User {

    private int id;
    @JsonProperty(value = "Username")
    private String username;
    @JsonProperty(value = "Password")
    private String password;
    private String authToken;
    private int gems;
    private int eloPoints;

    // Profile Stuff
    @JsonProperty(value = "Name")
    private String nameHandleOfUser;
    @JsonProperty(value = "Bio")
    private String bio;
    @JsonProperty(value = "Image")
    private String image;

    // Stats Stuff
    private int wins;
    private int losses;
    private int draws;
    private int totalBattles;

    // Deck
    private List<Card> deck = new ArrayList<Card>();

    // Stack
    private List<Card> allCards = new ArrayList<Card>();

    // Trades
    private List<Tradeable> tradings = new ArrayList<Tradeable>();

    public void printDeck()
    {
        int i = 0;
        if (this.getDeck().size() == 0)
            System.out.println("No Cards in Deck!\n");
        else
        {
            System.out.println("MY DECK: \n");
            for (i = 0; i < this.getDeck().size(); i++)
            {
                System.out.println("ID: " + this.getDeck().get(i).getCardId());
                System.out.println("NAME: " + this.getDeck().get(i).getCardName());
                System.out.println("TYPE: " + this.getDeck().get(i).getCardType());
                System.out.println("ELEMEENT: " + this.getDeck().get(i).getElement());
                System.out.println("DAMAGE: " + this.getDeck().get(i).getDamage());
                System.out.println("USERID: " + this.getDeck().get(i).getUserid());
                System.out.println("PKGID: " + this.getDeck().get(i).getPackageId());
                System.out.println("\n");
            }
        }
    }

    public void print(ResponseHandler response)
    {
        int i = 0;
        if (this.getAllCards().size() == 0)
            response.reply("No Cards in Stack" + "\r\n");
        else
        {
            response.reply("\r\n" + "My Stack" + "\r\n");
            for (i = 0; i < this.getAllCards().size(); i++)
            {
                response.reply("\r\n" + "ID: " + this.getAllCards().get(i).getCardId() + "\r\n");
                response.reply("NAME: " + this.getAllCards().get(i).getCardName() + "\r\n");
                response.reply("TYPE: " + this.getAllCards().get(i).getCardType() + "\r\n");
                response.reply("ELEMENT: " + this.getAllCards().get(i).getElement() + "\r\n");
                response.reply("DAMAGE: " + this.getAllCards().get(i).getDamage() + "\r\n");
                response.reply("USERID: " + this.getAllCards().get(i).getUserid() + "\r\n");
                response.reply("PKGID: " + this.getAllCards().get(i).getPackageId() + "\r\n");
            }
        }
    }

    public void setBestCardsFromStackToDeck()
    {
        if (deck.size() == 5) deck.clear();

        for(int i = 0; getDeck().size() < 4; i++)
        {
            if (this.getAllCards().get(i).isLocked())
            {
                continue;
            }
            deck.add(this.getAllCards().get(i));
        }
    }

    public void userPrintsHimself(ResponseHandler response)
    {
        response.reply("\r\n" + "ID: " + this.getId() + "\r\n");
        response.reply("Username: " + this.getUsername() + "\r\n");
        response.reply("Handle: " + this.getNameHandleOfUser() + "\r\n");
        response.reply("Image: " + this.getImage()+ "\r\n");
        response.reply( "Bio: " + this.getBio() + "\r\n");
        response.reply( "Gems: " + this.getGems() + "\r\n");
        response.reply( "Elopoints: " + this.getEloPoints() + "\r\n");
        response.reply( "Number of Cards in current deck: " + this.getDeck().size() + "\r\n");
        response.reply( "You own " + this.getAllCards().size() + " cards " + "\r\n");
    }

    public void showStats(ResponseHandler response)
    {
        response.reply("\r\n" + "You fought " + this.getTotalBattles() + " Battles" + "\r\n");
        response.reply("You won " + this.getWins() + " Battles" + "\r\n");
        response.reply("You lost " + this.getLosses() + " Battles" + "\r\n");
        response.reply("Do not know what happend here " + this.getDraws() + " Times" + "\r\n");
    }

    public void printTradingsHistory(ResponseHandler response)
    {
        int i = 0;
        if (this.getTradings().size() == 0)
            response.reply("\r\n" + "No Tradings" + "\r\n");
        else
        {
            response.reply("\r\n" + "MY TRADINGS: " + "\r\n");
            for (i = 0; i < this.getTradings().size(); i++)
            {
                response.reply("\r\n" + "ID: " + this.getTradings().get(i).getTradeId() + "\r\n");
                response.reply("CARD ID: " + this.getDeck().get(i).getCardId() + "\r\n");
                response.reply("TYPE: " + this.getTradings().get(i).getCardTypeOfTradeable() + "\r\n");
                response.reply("DAMAGE: " + this.getTradings().get(i).getDamageOfTradeable() + "\r\n");
                response.reply("OWNER: " + this.getTradings().get(i).getCurrentUserId() + "\r\n");
                response.reply("1. OWNER: " + this.getTradings().get(i).getOriginUserId() + "\r\n");
            }
        }
    }

    public boolean updateTradingsHistory() throws SQLException
    {
        DbManagement db = new DbManagement();
        List<Tradeable> newHistory = db.updateTradingsHistory(this);

        if (newHistory == null)
        {
            return false;
        }
        else
        {
            this.setTradings(newHistory);
            return true;
        }
    }

    public boolean isUserOwnerOfCard(String cardToCheck)
    {
        boolean isOwner = false;
        for (int i = 0; i < getAllCards().size(); i++)
        {
            if (getAllCards().get(i).getCardId().equals(cardToCheck))
            {
                isOwner = true;
                break;
            }
        }
        return isOwner;
    }

    public boolean isCardInDeck(String cardid)
    {
        boolean isCardInDeck = false;

        for (int i = 0; i < getDeck().size(); i++)
        {
            if (getAllCards().get(i).getCardId().equals(cardid))
            {
                isCardInDeck = true;
                break;
            }
        }
        return isCardInDeck;
    }

    public boolean isCardLocked(String cardid)
    {
        boolean isCardLocked = false;

        for (int i = 0; i < getDeck().size(); i++)
        {
            if (getAllCards().get(i).getCardId().equals(cardid) && getAllCards().get(i).isLocked())
            {
                isCardLocked = true;
                break;
            }
        }
        return isCardLocked;
    }

}


