package users;


import Requests.Request;
import card.Card;
import dbManagement.DbManagement;
import org.junit.jupiter.api.AfterEach;
import user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class UserTest
{
    private User user;
    private DbManagement db;
    private Request req;

    @BeforeEach
    public void setUp()
    {
        this.db = new DbManagement();
        this.req = new Request();
    }

    @Test
    public void isUserOwnerOfCardTest() throws Exception
    {
        this.user = this.db.getUserByToken("kienboec-mtcgToken");

        assertTrue(user.isUserOwnerOfCard("d04b736a-e874-4137-b191-638e0ff3b4e7"));
        assertFalse(user.isUserOwnerOfCard("dcd93250-25a7-4dca-85da-cad2789f7198"));

        this.user = this.db.getUserByToken("altenhof-mtcgToken");

        assertFalse(user.isUserOwnerOfCard("d04b736a-e874-4137-b191-638e0ff3b4e7"));
        assertTrue(user.isUserOwnerOfCard("9e8238a4-8a7a-487f-9f7d-a8c97899eb48"));

        assertNull(this.db.getUserByToken(""));
    }

    @Test
    public void isCardInDeckTest() throws Exception
    {
        this.user = this.db.getUserByToken("kienboec-mtcgToken");

        assertTrue(user.isCardInDeck("d04b736a-e874-4137-b191-638e0ff3b4e7"));
        assertFalse(user.isCardInDeck("dcd93250-25a7-4dca-85da-cad2789f7198"));

        this.user = this.db.getUserByToken("altenhof-mtcgToken");

        assertFalse(user.isCardInDeck("d04b736a-e874-4137-b191-638e0ff3b4e7"));
        assertTrue(user.isCardInDeck("9e8238a4-8a7a-487f-9f7d-a8c97899eb48"));

        assertNull(this.db.getUserByToken(""));
    }

    @Test
    public void isCardLockedTest() throws Exception
    {
        this.user = this.db.getUserByToken("kienboec-mtcgToken");
        this.user = this.db.getCardsByUserId(this.user);

        assertTrue(user.isCardLocked("951e886a-0fbf-425d-8df5-af2ee4830d85"));
        assertFalse(user.isCardLocked("44c82fbc-ef6d-44ab-8c7a-9fb19a0e7c6e"));

        this.user = this.db.getUserByToken("altenhof-mtcgToken");
        this.user = this.db.getCardsByUserId(this.user);

        assertFalse(user.isCardLocked("951e886a-0fbf-425d-8df5-af2ee4830d85"));
        assertTrue(user.isCardLocked("44c82fbc-ef6d-44ab-8c7a-9fb19a0e7c6e"));

        assertNull(this.db.getUserByToken(""));
    }

    @Test
    public void updateTradingHistoryTest() throws Exception
    {
        this.user = this.db.getUserByToken("kienboec-mtcgToken");
        assertTrue(this.user.updateTradingsHistory());

        this.user = this.db.getUserByToken("altenhof-mtcgToken");
        assertFalse(this.user.updateTradingsHistory());
    }

    @Test
    public void setBestCardsInDeckTest() throws Exception
    {
        this.user = this.db.getUserByToken("kienboec-mtcgToken");
        List<Card> howItShouldLookLikeForUser1 = new ArrayList<Card>();

        Card card1 = new Card();
        card1.setId(918);
        card1.setCardName("Dragon");
        card1.setDamage(70);
        card1.setElement("REGULAR");
        card1.setCardType("Monster");
        card1.setCardId("d04b736a-e874-4137-b191-638e0ff3b4e7");
        card1.setPackageId(178);
        card1.setUserid(75);
        card1.setLocked(false);
        howItShouldLookLikeForUser1.add(card1);

        Card card2 = new Card();
        card2.setId(913);
        card2.setCardName("Dragon");
        card2.setDamage(55);
        card2.setElement("REGULAR");
        card2.setCardType("Monster");
        card2.setCardId("4a2757d6-b1c3-47ac-b9a3-91deab093531");
        card2.setPackageId(177);
        card2.setUserid(75);
        card2.setLocked(false);
        howItShouldLookLikeForUser1.add(card2);

        Card card3 = new Card();
        card3.setId(915);
        card3.setCardName("Ork");
        card3.setDamage(55);
        card3.setElement("REGULAR");
        card3.setCardType("Monster");
        card3.setCardId("4ec8b269-0dfa-4f97-809a-2c63fe2a0025");
        card3.setPackageId(177);
        card3.setUserid(75);
        card3.setLocked(false);
        howItShouldLookLikeForUser1.add(card3);

        Card card4 = new Card();
        card4.setId(908);
        card4.setCardName("Dragon");
        card4.setDamage(50);
        card4.setElement("REGULAR");
        card4.setCardType("Monster");
        card4.setCardId("99f8f8dc-e25e-4a95-aa2c-782823f36e2a");
        card4.setPackageId(176);
        card4.setUserid(75);
        card4.setLocked(false);
        howItShouldLookLikeForUser1.add(card4);

        assertEquals(howItShouldLookLikeForUser1.get(0).getCardId(), this.user.getDeck().get(0).getCardId());
    }


    @AfterEach
    public void destroy()
    {
        this.user = null;
        this.db = null;
        this.req.reset();
    }

}
