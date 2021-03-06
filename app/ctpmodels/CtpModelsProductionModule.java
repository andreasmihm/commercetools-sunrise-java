package ctpmodels;

import com.google.inject.AbstractModule;
import common.contexts.ProjectContext;
import common.models.ProductDataConfig;

import javax.inject.Singleton;

/**
 * Configuration for the Guice {@link com.google.inject.Injector} which shall be used in production.
 */
public class CtpModelsProductionModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(ProjectContext.class).toProvider(ProjectContextProvider.class).in(Singleton.class);
        bind(ProductDataConfig.class).toProvider(ProductDataConfigProvider.class).in(Singleton.class);
    }
}
