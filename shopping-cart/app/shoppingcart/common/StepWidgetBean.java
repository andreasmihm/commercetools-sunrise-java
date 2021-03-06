package shoppingcart.common;

public class StepWidgetBean {

    private boolean addressStepActive;
    private boolean shippingStepActive;
    private boolean paymentStepActive;
    private boolean confirmationStepActive;

    public StepWidgetBean() {
    }

    public boolean isAddressStepActive() {
        return addressStepActive;
    }

    public void setAddressStepActive(final boolean addressStepActive) {
        this.addressStepActive = addressStepActive;
    }

    public boolean isShippingStepActive() {
        return shippingStepActive;
    }

    public void setShippingStepActive(final boolean shippingStepActive) {
        this.shippingStepActive = shippingStepActive;
    }

    public boolean isPaymentStepActive() {
        return paymentStepActive;
    }

    public void setPaymentStepActive(final boolean paymentStepActive) {
        this.paymentStepActive = paymentStepActive;
    }

    public boolean isConfirmationStepActive() {
        return confirmationStepActive;
    }

    public void setConfirmationStepActive(final boolean confirmationStepActive) {
        this.confirmationStepActive = confirmationStepActive;
    }
}
