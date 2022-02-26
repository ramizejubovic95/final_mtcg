package trading;

import card.Card;
import dbManagement.DbManagement;
import lombok.NoArgsConstructor;

import java.sql.SQLException;

@NoArgsConstructor
public class TradingService
{
    public boolean tradeCards(Card seller, Card buyer) throws SQLException
    {

        boolean meetsRequirementsOfSeller = checkIfCardMeetsRequirements(seller, buyer);
        if (!meetsRequirementsOfSeller) return false;

        System.out.println("SELLER: " + seller.getUserid());
        System.out.println("BUYER: " + buyer.getUserid());

        DbManagement db = new DbManagement();
        db.switchOwnerOfCard(seller, buyer);

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

