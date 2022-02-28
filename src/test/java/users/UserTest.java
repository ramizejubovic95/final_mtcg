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
    private User userKienboec;
    private User userAltenhofer;
    private DbManagement db;


    @BeforeEach
    public void setUp() throws Exception
    {
        this.db = new DbManagement();

        this.userKienboec = this.db.getUserByToken("kienboec-mtcgToken");
        this.userAltenhofer = this.db.getUserByToken("altenhof-mtcgToken");

        this.db.setCardToLocked("951e886a-0fbf-425d-8df5-af2ee4830d85");
        this.db.setCardToLocked("44c82fbc-ef6d-44ab-8c7a-9fb19a0e7c6e");

    }

    @Test
    public void isUserOwnerOfCardTest() throws Exception
    {
        assertTrue(this.userKienboec.isUserOwnerOfCard("d04b736a-e874-4137-b191-638e0ff3b4e7"));
        assertFalse(this.userKienboec.isUserOwnerOfCard("dcd93250-25a7-4dca-85da-cad2789f7198"));

        assertFalse(this.userAltenhofer.isUserOwnerOfCard("d04b736a-e874-4137-b191-638e0ff3b4e7"));
        assertTrue(this.userAltenhofer.isUserOwnerOfCard("9e8238a4-8a7a-487f-9f7d-a8c97899eb48"));

        assertNull(this.db.getUserByToken(""));
    }

    @Test
    public void isCardInDeckTest() throws Exception
    {
        assertTrue(this.userKienboec.isCardInDeck("d04b736a-e874-4137-b191-638e0ff3b4e7"));
        assertFalse(this.userKienboec.isCardInDeck("9e8238a4-8a7a-487f-9f7d-a8c97899eb48"));

        assertFalse(this.userAltenhofer.isCardInDeck("d04b736a-e874-4137-b191-638e0ff3b4e7"));
        assertTrue(this.userAltenhofer.isCardInDeck("9e8238a4-8a7a-487f-9f7d-a8c97899eb48"));

        assertNull(this.db.getUserByToken(""));
    }

    @Test
    public void isCardLockedTest() throws Exception
    {
        assertTrue(this.userKienboec.isCardLocked("951e886a-0fbf-425d-8df5-af2ee4830d85"));
        assertFalse(this.userKienboec.isCardLocked("44c82fbc-ef6d-44ab-8c7a-9fb19a0e7c6e"));

        assertFalse(this.userAltenhofer.isCardLocked("951e886a-0fbf-425d-8df5-af2ee4830d85"));
        assertTrue(this.userAltenhofer.isCardLocked("44c82fbc-ef6d-44ab-8c7a-9fb19a0e7c6e"));

        assertNull(this.db.getUserByToken(""));
    }

    @Test
    public void updateTradingHistoryTest() throws Exception
    {
        assertTrue(this.userKienboec.updateTradingsHistory());
        assertFalse(this.userAltenhofer.updateTradingsHistory());
    }

    @Test
    public void setBestCardsInDeckTest() throws Exception
    {
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

        assertEquals(howItShouldLookLikeForUser1.get(0).getCardId(), this.userKienboec.getDeck().get(0).getCardId());
    }


    @AfterEach
    public void destroy() throws SQLException
    {
        this.db.unlockCard("951e886a-0fbf-425d-8df5-af2ee4830d85");
        this.db.unlockCard("44c82fbc-ef6d-44ab-8c7a-9fb19a0e7c6e");
        this.userKienboec = null;
        this.userAltenhofer = null;
        this.db = null;
    }

}
