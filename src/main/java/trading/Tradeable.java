package trading;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class Tradeable
{
    private int id;
    @JsonProperty(value = "Id")
    private String tradeId;
    @JsonProperty(value = "CardToTrade")
    private String cardIdOfTradeable;
    @JsonProperty(value = "Type")
    private String cardTypeOfTradeable;
    @JsonProperty(value = "MinimumDamage")
    private float damageOfTradeable;

    private int currentUserId;
    private String currentUserAuthToken;

    private int originUserId;
    private String originUserAuthToken;

    private boolean isTraded;

}
