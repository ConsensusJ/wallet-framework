package org.consensusj.supernautfx.sample.hello;

import io.micronaut.context.annotation.Factory;

import javax.inject.Named;
import javax.inject.Singleton;

/**
 * Bean Factory
 */
@Factory
public class HelloBeanFactory {
    @Named("Greeted")
    @Singleton
    public String getGreeted() {
        return "Mars";
    }
}
