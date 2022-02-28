package trading;

import card.Card;
import dbManagement.DbManagement;
import lombok.NoArgsConstructor;
import response.ResponseHandler;

import java.sql.SQLException;

@NoArgsConstructor
public class TradingService
{
    public boolean tradeCards(Card seller, Card buyer, ResponseHandler response) throws SQLException
    {

        boolean meetsRequirementsOfSeller = checkIfCardMeetsRequirements(seller, buyer);
        if (!meetsRequirementsOfSeller)
        {
            response.reply("\r\n" + "CARD DOES NOT MEET REQUIREMENTS" + "\r\n");
            return false;
        }

        response.reply("\r\n" + "Trading in process ... " + "\r\n");

        DbManagement db = new DbManagement();
        db.switchOwnerOfCard(seller, buyer);

        return true;
    }

    public boolean checkIfCardMeetsRequirements(Card seller, Card buyer)
    {
        boolean meetsRequirements = seller.getDamage() <= buyer.getDamage() && seller.getCardType().equals(buyer.getCardType());
        return meetsRequirements;
    }
}

