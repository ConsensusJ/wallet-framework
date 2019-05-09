package org.consensusj.supernautfx

import io.micronaut.context.BeanContext
import javafx.fxml.FXMLLoader
import org.consensusj.supernautfx.sample.hello.HelloSupernautFxApp

import spock.lang.Ignore
import spock.lang.Specification

import javax.inject.Provider

/**
 *
 */
class SupernautFxIntegrationSpec extends Specification {
    def "Can create and find an FXMLLoader provider/factory"() {
        when:
        BeanContext ctx = BeanContext.build()
        def loaderFactory = new FxmlLoaderFactory(ctx);
        ctx.registerSingleton(Provider.class, loaderFactory);
        ctx.start();
        Provider<FXMLLoader> foundFactory = ctx.getBean(Provider.class);
        FXMLLoader loader = (FXMLLoader) foundFactory.get();

        then:
        foundFactory != null
        foundFactory instanceof FxmlLoaderFactory
        loader != null
        loader instanceof FXMLLoader
    }

    @Ignore
    def "Can create an FXMLLoader factory and inject into test class"() {
        when:
        BeanContext ctx = BeanContext.build()
        def loaderFactory = new FxmlLoaderFactory(ctx)
        ctx.registerSingleton(Provider.class, loaderFactory)
        HelloSupernautFxApp testBean = ctx.createBean(HelloSupernautFxApp.class)
        Provider<FXMLLoader> foundFactory = ctx.getBean(Provider.class)


        then:
        foundFactory != null
        foundFactory instanceof FxmlLoaderFactory
    }

}
