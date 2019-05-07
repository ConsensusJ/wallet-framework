package org.consensusj.supernautfx.sample.hello.service;

import javax.inject.Named;
import javax.inject.Singleton;

/**
 * A demo, dependency injected service
 */
@Singleton
public class GreetingService {
    private final String name;

    public GreetingService(@Named("Greeted") String name) {
        this.name = name;
    }

    public String greeting() {
        return "Hello " + name + "!";
    }
}
