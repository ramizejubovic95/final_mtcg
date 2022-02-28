package dbmanagement;

import static org.junit.jupiter.api.Assertions.*;

import battle.Battle;
import card.Card;
import dbManagement.DbManagement;
import org.junit.jupiter.api.*;
import trading.Tradeable;
import user.User;

import java.sql.SQLException;
import java.util.List;

public class DbManagementTest
{
    private DbManagement db;
    private User user;
    private Card card;
    private Battle battle;
    private Tradeable tradeable;

    @BeforeEach
    public void setUp() throws SQLException {
        this.db = new DbManagement();

        this.tradeable = new Tradeable();
        tradeable.setCardIdOfTradeable("111111111111111111");
        tradeable.setTradeId("00000000000000000000000");
        tradeable.setCardTypeOfTradeable("monster");
        tradeable.setCurrentUserId(99);
        tradeable.setCurrentUserAuthToken("kienboec-mtcgToken");
        tradeable.setOriginUserId(100);
        tradeable.setOriginUserAuthToken("altenhof-mtcgToken");
        tradeable.setDamageOfTradeable(33);

        this.db.saveNewTrade(this.tradeable);

        this.user = new User();

        this.card = new Card();
        card.setId(999999);
        card.setCardName("WaterGoblin");
        card.setDamage(99999999);
        card.setElement("Water");
        card.setCardType("Monster");
        card.setCardId("1cb6ab86-bdb2-47e5-b6e4-68c5ab389334");
        card.setPackageId(255);
        card.setUserid(99);

        this.battle = new Battle();
        battle.setFighter1(75);
        battle.setFighter2(76);
        battle.setWinner(75);
        battle.setLoser(76);
        battle.setFinished(true);
        battle.setBattleId(26);
    }

    @Test
    public void testIfUserGetsHisCards() throws Exception
    {
        this.user = db.getUserByToken("kienboec-mtcgToken");
        this.user = db.getCardsByUserId(this.user);
        assertEquals("kienboec-mtcgToken", user.getAuthToken());

        this.user = db.getUserByToken("blabla-mtcgToken");
        this.user = db.getCardsByUserId(this.user);
        assertNull(this.user);
    }

    @Test
    public void testIfUserWillReturnFromToken() throws Exception
    {
        this.user = db.getUserByToken("kienboec-mtcgToken");
        assertNotNull(this.user);

        this.user = db.getUserByToken("blabla-mtcgToken");
        assertNull(this.user);
    }

    @Test
    public void testIfScoreBoardIsReturned() throws Exception
    {
        List<User> scoreboard = this.db.updateScoreBoard();

        assertNotNull(scoreboard);
    }

    @Test
    public void testToGetCardIdFromTradeId() throws Exception
    {
        assertEquals("1cb6ab86-bdb2-47e5-b6e4-68c5ab389334", this.db.getCardIdByTradeId("6cd85277-4590-49d4-b0cf-ba0a921faad0"));
    }

    @Test
    public void testToGetTradeableFromTradeId() throws Exception
    {
        assertNotNull(this.db.getTradeableByTradeId("6cd85277-4590-49d4-b0cf-ba0a921faad0"));
        assertNull(this.db.getTradeableByTradeId("BLABLBABLABLAB"));
    }

    @Test
    public void testToSeeIfCardsGetsLocked() throws Exception
    {
        assertTrue(this.db.setCardToLocked("1cb6ab86-bdb2-47e5-b6e4-68c5ab389334"));
        assertFalse(this.db.setCardToLocked("BLABLABAL"));
    }

    @Test
    public void testToSeeIfCardsGetsUnlocked() throws Exception
    {
        assertTrue(this.db.unlockCard("1cb6ab86-bdb2-47e5-b6e4-68c5ab389334"));
        assertFalse(this.db.unlockCard("BLABLABAL"));
    }

    @Test
    public void testToCheckIfCardsComeBackByCardId() throws Exception
    {
        assertNotNull(this.db.getCardsByCardId("1cb6ab86-bdb2-47e5-b6e4-68c5ab389334"));
        assertNull(this.db.getCardsByCardId("BLABLABAL"));
    }

    @Test
    public void testIfBattleDataIsSavedAfterFight() throws Exception
    {
        this.battle = this.db.createBattle(this.battle);
        assertTrue(this.db.saveBattleData(this.battle));

        Battle nullBattle = new Battle();
        assertFalse(this.db.saveBattleData(nullBattle));
    }

    @Test
    public void testToSeeIfTradeIsUpdatedByCardId() throws Exception
    {
        assertTrue(this.db.updateTradeFromDbByCardId(this.card));
        assertFalse(this.db.updateTradeFromDbByCardId(null));
    }

    @Test
    public void testToSeeIfTradeIsUpdatedByTradeId() throws Exception
    {
        this.card.setCardId(this.tradeable.getCardIdOfTradeable());
        this.db.saveCardForTestOnly(this.card);

        assertTrue(this.db.updateTradeFromDbByTradeId(this.tradeable.getTradeId()));
        assertFalse(this.db.updateTradeFromDbByCardId(null));
    }

    @AfterEach
    public void destroy() throws SQLException
    {
        this.db.deleteCardForTestOnly(this.card);
        this.db.deleteBattleForTestOnly(this.battle);
        this.db.deleteTradeForTestOnly(this.tradeable);
        this.db = null;
        this.tradeable = null;
        this.battle = null;
        this.user = null;
        this.card = null;
    }
}
