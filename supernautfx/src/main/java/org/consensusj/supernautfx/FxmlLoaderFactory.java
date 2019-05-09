package org.consensusj.supernautfx;

import io.micronaut.context.BeanContext;
import javafx.fxml.FXMLLoader;

import javax.inject.Provider;
import javax.inject.Singleton;

/**
 * Factory for providing FXMLLoaders that do full DI
 */
@Singleton
public class FxmlLoaderFactory implements Provider<FXMLLoader> {
    BeanContext context;

    public FxmlLoaderFactory(BeanContext context) {
        this.context = context;
    }

    public <T> T getControllerFactory(Class<T> clazz) {
        return context.getBean(clazz);
    }

    @Override
    public FXMLLoader get() {
        FXMLLoader loader = new FXMLLoader();
        loader.setControllerFactory(this::getControllerFactory);
        return loader;
    }
}
