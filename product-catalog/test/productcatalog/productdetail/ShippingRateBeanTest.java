package productcatalog.productdetail;

import common.utils.PriceFormatter;
import io.sphere.sdk.shippingmethods.ShippingRate;
import org.javamoney.moneta.Money;
import org.junit.Test;

import javax.money.CurrencyUnit;
import javax.money.Monetary;

import static java.util.Locale.GERMAN;
import static org.assertj.core.api.Assertions.assertThat;

public class ShippingRateBeanTest {
    private static final CurrencyUnit EUR = Monetary.getCurrency("EUR");
    private static final PriceFormatter PRICE_FORMATTER = PriceFormatter.of(GERMAN);

    @Test
    public void create() {
        final ShopShippingRate shippingRate = new ShopShippingRate("DHL", ShippingRate.of(Money.of(10.50, EUR), Money.of(50.49, EUR)));

        final ShippingRateBean shippingRateBean =
                new ShippingRateBean(PRICE_FORMATTER, shippingRate);

        assertThat(shippingRateBean.getShippingMethod()).isEqualTo("DHL");
        assertThat(shippingRateBean.getZone()).isNullOrEmpty();
        assertThat(shippingRateBean.getShippingRate()).isEqualTo("EUR 10,50");
        assertThat(shippingRateBean.getFreeAbove()).isEqualTo("EUR 50,49");
    }
}
