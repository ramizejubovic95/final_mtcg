package user;

import card.Card;
import com.fasterxml.jackson.annotation.JsonProperty;
import dbManagement.DbManagement;
import lombok.*;
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

    public void print()
    {
        int i = 0;
        if (this.getAllCards().size() == 0)
            System.out.println("No Cards in Stack!\n");
        else
        {
            System.out.println("MY STACK: \n");
            for (i = 0; i < this.getAllCards().size(); i++)
            {
                System.out.println("ID: " + this.getAllCards().get(i).getCardId());
                System.out.println("NAME: " + this.getAllCards().get(i).getCardName());
                System.out.println("TYPE: " + this.getAllCards().get(i).getCardType());
                System.out.println("ELEMEENT: " + this.getAllCards().get(i).getElement());
                System.out.println("DAMAGE: " + this.getAllCards().get(i).getDamage());
                System.out.println("USERID: " + this.getAllCards().get(i).getUserid());
                System.out.println("PKGID: " + this.getAllCards().get(i).getPackageId());
                System.out.println("\n");
            }
        }
    }

    public void setBestCardsFromStackToDeck()
    {
        if (deck.size() == 4) deck.clear();

        for(int i = 0; i < 4; i++)
        {
            if (this.getAllCards().get(i).isLocked())
                continue;

            deck.add(this.getAllCards().get(i));
        }
    }

    public void userPrintsHimself()
    {
        System.out.println(this.getId());
        System.out.println(this.getUsername());
        System.out.println(this.getNameHandleOfUser());
        System.out.println(this.getImage());
        System.out.println(this.getBio());
        System.out.println(this.getAuthToken());
        System.out.println(this.getGems());
        System.out.println(this.getEloPoints());
        System.out.println("Number of Cards in current deck: " + this.getDeck().size());
        System.out.println("You own " + this.getAllCards().size() + " cards");
    }

    public void showStats()
    {
        System.out.println("You fought " + this.getTotalBattles() + " Battles");
        System.out.println("You won " + this.getWins() + " Battles");
        System.out.println("You lost " + this.getLosses() + " Battles");
        System.out.println("Do not know what happend here " + this.getLosses() + " Times");
    }

    public void printTradingsHistory()
    {
        int i = 0;
        if (this.getTradings().size() == 0)
            System.out.println("No Tradings\n");
        else
        {
            System.out.println("MY TRADINGS: \n");
            for (i = 0; i < this.getTradings().size(); i++)
            {
                System.out.println("ID: " + this.getTradings().get(i).getTradeId());
                System.out.println("CARD ID: " + this.getDeck().get(i).getCardId());
                System.out.println("TYPE: " + this.getTradings().get(i).getCardTypeOfTradeable());
                System.out.println("DAMAGE: " + this.getTradings().get(i).getDamageOfTradeable());
                System.out.println("OWNER: " + this.getTradings().get(i).getCurrentUserId());
                System.out.println("1. OWNER: " + this.getTradings().get(i).getOriginUserId());
                System.out.println("\n");
            }
        }
    }

    public void updateTradingsHistory() throws SQLException
    {
        DbManagement db = new DbManagement();
        this.setTradings(db.updateTradingsHistory(this));
    }

    public boolean isUserOwnerOfCard(String cardToCheck)
    {
        boolean isOwner = false;
        for (int i = 0; i < getAllCards().size(); i++)
        {
            System.out.println("CARD 1: " + getAllCards().get(i).getCardId());
            System.out.println("CARD 2: " + cardToCheck);
            System.out.println(getAllCards().get(i).getCardId().equals(cardToCheck));
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
            if (getDeck().get(i).getCardId().equals(cardid))
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
            if (getDeck().get(i).getCardId().equals(cardid) && getDeck().get(i).isLocked())
            {
                isCardLocked = true;
                break;
            }
        }
        return isCardLocked;
    }


}


