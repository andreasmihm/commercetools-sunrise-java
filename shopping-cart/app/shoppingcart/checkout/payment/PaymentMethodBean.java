package shoppingcart.checkout.payment;

import common.contexts.UserContext;
import common.models.FormSelectableOptionBean;
import io.sphere.sdk.payments.PaymentMethodInfo;

import java.util.List;
import java.util.Optional;

public class PaymentMethodBean extends FormSelectableOptionBean {

    public PaymentMethodBean() {
    }

    public PaymentMethodBean(final PaymentMethodInfo paymentMethod, final List<String> selectedPaymentMethods,
                             final UserContext userContext) {
        super(createLabel(paymentMethod, userContext), paymentMethod.getMethod(), selectedPaymentMethods.contains(paymentMethod.getMethod()));
    }

    private static String createLabel(final PaymentMethodInfo paymentMethod, final UserContext userContext) {
        return Optional.ofNullable(paymentMethod.getName())
                .flatMap(name -> name.find(userContext.locales()))
                .orElse("");
    }
}
