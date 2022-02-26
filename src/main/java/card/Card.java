package card;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;


@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Card {
    private String[] elementArray = new String[]{ "Regular", "Water", "Fire"};
    private String[] cardTypeArray = new String[] { "Monster", "Spell"};

    private int id;
    @JsonProperty(value = "Id")
    private String cardId;
    @JsonProperty(value = "Name")
    private String cardName;
    @JsonProperty(value = "Damage")
    private float damage;
    private String element;
    private String cardType;
    private int packageId;
    private int userid;
    private boolean isLocked;

}
