import java.math.BigDecimal;

public interface PriceListener {
    void onPrice(String symbol, BigDecimal price);

}