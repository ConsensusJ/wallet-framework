package org.consensusj.supernautfx.sample.hello;

import javafx.application.Application;
import javafx.stage.Stage;
import org.consensusj.supernautfx.SupernautFxApp;
import org.consensusj.supernautfx.sample.hello.service.GreetingService;

/**
 * Sample Hello Application
 */
public class HelloApplication extends Application {
    private SupernautFxApp app;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void init() throws Exception {
        String name = "Mars";
        GreetingService greetingService = new GreetingService(name);
        app = new HelloSupernautFxApp(greetingService);
        app.init();
    }


    @Override
    public void start(Stage primaryStage) {
        app.start(primaryStage);
    }

    @Override
    public void stop() throws Exception {
        app.stop();
    }
}
