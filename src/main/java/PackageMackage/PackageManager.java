package PackageMackage;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
public class PackageManager
{
    private int id;
    private int price;
    private boolean isBought;

    public PackageManager()
    {
        this.price = 5;
        this.isBought = false;
    }

}
