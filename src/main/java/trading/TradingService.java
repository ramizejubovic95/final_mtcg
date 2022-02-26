package trading;

import card.Card;
import dbManagement.DbManagement;
import lombok.NoArgsConstructor;

import java.sql.SQLException;

@NoArgsConstructor
public class TradingService
{
    public boolean tradeCards(String cardOnMarketPlace, String offerFromRequester) throws SQLException
    {
        DbManagement db = new DbManagement();
        Card cardOfSeller = db.getCardsByCardId(cardOnMarketPlace);
        Card cardOfBuyer = db.getCardsByCardId(offerFromRequester);


        boolean meetsRequirementsOfSeller = checkIfCardMeetsRequirements(cardOfSeller, cardOfBuyer);
        if (!meetsRequirementsOfSeller) return false;

        db.switchOwnerOfCard(cardOfSeller, cardOfBuyer);

        return true;
    }
    public boolean checkIfCardMeetsRequirements(Card seller, Card buyer)
    {
        boolean meetsRequirements = seller.getDamage() <= buyer.getDamage() && seller.getCardType().equals(buyer.getCardType());
        if (!meetsRequirements)
        {
            System.out.println("CARD DOES NOT MEET REQUIREMENTS");
            return false;
        }
        else
        {
            System.out.println("Trading in process ... ");
            return true;
        }
    }
}

