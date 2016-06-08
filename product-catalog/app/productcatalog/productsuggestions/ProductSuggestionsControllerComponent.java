package productcatalog.productsuggestions;

import common.contexts.UserContext;
import common.controllers.SunrisePageData;
import common.hooks.SunrisePageDataHook;
import common.suggestion.ProductRecommendation;
import framework.ControllerComponent;
import io.sphere.sdk.products.ProductProjection;
import play.Configuration;
import productcatalog.common.ProductListBean;
import productcatalog.common.ProductListBeanFactory;
import productcatalog.common.SuggestionsData;
import productcatalog.hooks.SingleProductProjectionHook;
import productcatalog.productdetail.ProductDetailPageContent;

import javax.inject.Inject;
import java.util.Set;
import java.util.concurrent.CompletionStage;

public class ProductSuggestionsControllerComponent implements ControllerComponent, SingleProductProjectionHook, SunrisePageDataHook {

    @Inject
    private UserContext userContext;
    @Inject
    private ProductRecommendation productRecommendation;
    @Inject
    private ProductListBeanFactory productListBeanFactory;
    private int numSuggestions;
    private Set<ProductProjection> suggestions;

    @Inject
    public void setConfig(final Configuration configuration) {
        this.numSuggestions = configuration.getInt("productSuggestions.count");
     }

    @Override
    public CompletionStage<?> onSingleProductProjectionLoaded(final ProductProjection product) {
        return productRecommendation.relatedToProduct(product, numSuggestions, userContext)
                .thenAccept(m -> suggestions = m);
    }

    @Override
    public void acceptSunrisePageData(final SunrisePageData sunrisePageData) {
        if (sunrisePageData.getContent() instanceof ProductDetailPageContent) {
            final ProductDetailPageContent content = (ProductDetailPageContent) sunrisePageData.getContent();
            content.setSuggestions(createSuggestions(userContext, suggestions));
        }
    }

    private SuggestionsData createSuggestions(final UserContext userContext, final Set<ProductProjection> suggestions) {
        final ProductListBean productListData = productListBeanFactory.create(suggestions);
        return new SuggestionsData(productListData);
    }
}