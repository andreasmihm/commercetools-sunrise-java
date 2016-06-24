package com.commercetools.sunrise.myaccount.addressbook.addaddress;

import com.commercetools.sunrise.common.contexts.RequestScoped;
import com.commercetools.sunrise.common.controllers.WithOverwriteableTemplateName;
import com.commercetools.sunrise.myaccount.addressbook.AddressActionData;
import com.commercetools.sunrise.myaccount.addressbook.AddressBookManagementController;
import com.commercetools.sunrise.myaccount.addressbook.AddressFormData;
import com.commercetools.sunrise.myaccount.addressbook.DefaultAddressFormData;
import com.google.inject.Injector;
import io.sphere.sdk.customers.Customer;
import io.sphere.sdk.customers.commands.CustomerUpdateCommand;
import io.sphere.sdk.customers.commands.updateactions.AddAddress;
import io.sphere.sdk.models.Address;
import io.sphere.sdk.models.SphereException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.data.Form;
import play.data.FormFactory;
import play.filters.csrf.AddCSRFToken;
import play.filters.csrf.RequireCSRFCheck;
import play.libs.concurrent.HttpExecution;
import play.mvc.Result;
import play.twirl.api.Html;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletionStage;

import static io.sphere.sdk.utils.FutureUtils.exceptionallyCompletedFuture;
import static io.sphere.sdk.utils.FutureUtils.recoverWithAsync;
import static java.util.Arrays.asList;

@RequestScoped
public abstract class SunriseAddAddressController extends AddressBookManagementController implements WithOverwriteableTemplateName {

    protected static final Logger logger = LoggerFactory.getLogger(SunriseAddAddressController.class);

    @Inject
    private Injector injector;
    @Inject
    private FormFactory formFactory;

    @Override
    public Set<String> getFrameworkTags() {
        final Set<String> frameworkTags = super.getFrameworkTags();
        frameworkTags.addAll(asList("address-book", "add-address", "address"));
        return frameworkTags;
    }

    @Override
    public String getTemplateName() {
        return "my-account-new-address";
    }

    @AddCSRFToken
    public CompletionStage<Result> show(final String languageTag) {
        return doRequest(() -> {
            logger.debug("show new address form for address in locale={}", languageTag);
            return injector.getInstance(AddAddressActionDataDefaultProvider.class).getActionData(session(), null)
                    .thenComposeAsync(this::showAddAddress, HttpExecution.defaultContext());
        });
    }

    @RequireCSRFCheck
    public CompletionStage<Result> process(final String languageTag) {
        return doRequest(() -> {
            logger.debug("try to add address with in locale={}", languageTag);
            final Form<DefaultAddressFormData> form = formFactory.form(DefaultAddressFormData.class).bindFromRequest();
            return injector.getInstance(AddAddressActionDataDefaultProvider.class).getActionData(session(), form)
                    .thenComposeAsync(this::processAddAddress, HttpExecution.defaultContext());
        });
    }

    protected <T extends AddressFormData> CompletionStage<Result> showAddAddress(final AddressActionData<T> data) {
        return ifNotNullCustomer(data.getCustomer().orElse(null), this::showEmptyForm);
    }

    protected CompletionStage<Result> showEmptyForm(final Customer customer) {
        final Form<?> form = obtainFilledForm(null);
        return asyncOk(renderPage(customer, form));
    }

    protected <T extends AddressFormData> CompletionStage<Result> processAddAddress(final AddressActionData<T> data) {
        return ifNotNullCustomer(data.getCustomer().orElse(null), customer -> data.getForm()
                .map(form -> {
                    if (!form.hasErrors()) {
                        return applySubmittedAddress(customer, form.get());
                    } else {
                        return handleInvalidSubmittedAddress(customer, form);
                    }
                }).orElseGet(() -> showEmptyForm(customer))
        );
    }

    protected <T extends AddressFormData> CompletionStage<Result> applySubmittedAddress(final Customer customer, final T formData) {
        final CompletionStage<Result> resultStage = addAddressToCustomer(customer, formData)
                .thenComposeAsync(updatedCustomer -> handleSuccessfulCustomerUpdate(updatedCustomer, formData), HttpExecution.defaultContext());
        return recoverWithAsync(resultStage, HttpExecution.defaultContext(), throwable ->
                handleFailedCustomerUpdate(customer, formData, throwable));
    }

    protected <T extends AddressFormData> CompletionStage<Result> handleSuccessfulCustomerUpdate(final Customer customer, final T formData) {
        return redirectToAddressBook();
    }

    protected <T extends AddressFormData> CompletionStage<Result> handleFailedCustomerUpdate(final Customer customer, final T formData, final Throwable throwable) {
        if (throwable.getCause() instanceof SphereException) {
            saveUnexpectedError((SphereException) throwable.getCause());
            final Form<?> form = obtainFilledForm(formData.extractAddress());
            return asyncBadRequest(renderPage(customer, form));
        }
        return exceptionallyCompletedFuture(throwable);
    }

    protected <T extends AddressFormData> CompletionStage<Result> handleInvalidSubmittedAddress(final Customer customer, final Form<T> form) {
        saveFormErrors(form);
        return asyncBadRequest(renderPage(customer, form));
    }

    protected <T extends AddressFormData> CompletionStage<Customer> addAddressToCustomer(final Customer customer, final T formData) {
        final AddAddress addAddressAction = AddAddress.of(formData.extractAddress());
        return sphere().execute(CustomerUpdateCommand.of(customer, addAddressAction));
//                .thenComposeAsync(updatedCustomer -> {
//                    findAddress(customer, )
//                    final List<Customer> updateActions = new ArrayList<>();
//                    if (formData.isDefaultShippingAddress()) {
//                        updateActions.add(SetDefaultShippingAddress.of())
//                    }
//                });
    }

    protected CompletionStage<Html> renderPage(final Customer customer, final Form<?> form) {
        final AddAddressPageContent pageContent = injector.getInstance(AddAddressPageContentFactory.class).create(customer, form);
        return renderPage(pageContent, getTemplateName());
    }

    protected Form<?> obtainFilledForm(@Nullable final Address address) {
        final DefaultAddressFormData formData = new DefaultAddressFormData();
        formData.apply(address);
        return formFactory.form(DefaultAddressFormData.class).fill(formData);
    }

    private Optional<Address> findAddress(final Customer customer, final String addressId) {
        return customer.getAddresses().stream()
                .filter(a -> Objects.equals(a.getId(), addressId))
                .findFirst();
    }
}