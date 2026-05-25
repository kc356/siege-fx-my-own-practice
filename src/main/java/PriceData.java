import java.math.BigDecimal;

public class PriceData {
    String symbol;
    BigDecimal price;

    public PriceData(String symbol, BigDecimal price) {
        this.symbol = symbol;
        this.price = price;
    }
}
