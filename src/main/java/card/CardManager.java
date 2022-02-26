package card;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class CardManager {

    public Card setElement(Card card)
    {
        if (card.getCardName().contains("Water"))
        {
            card.setElement("Water");
        }
        else if (card.getCardName().contains("Fire"))
        {
            card.setElement("Fire");
        }
        else
        {
            card.setElement("Regular");
        }
        return card;
    }

    public Card setType(Card card)
    {
        if (card.getCardName().contains("Spell"))
        {
            card.setCardType("Spell");
        }
        else
        {
            card.setCardType("Monster");
        }
        return card;
    }

    public Card setPackageId(Card card, int id)
    {
        card.setPackageId(id);
        return card;
    }

}
