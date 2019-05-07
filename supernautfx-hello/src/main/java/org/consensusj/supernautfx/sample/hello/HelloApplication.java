package org.consensusj.supernautfx.sample.hello;

import javafx.application.Application;
import javafx.stage.Stage;
import org.consensusj.supernautfx.SupernautFxApp;

import io.micronaut.context.ApplicationContext;
import io.micronaut.context.env.Environment;


/**
 * Sample Hello Application
 */
public class HelloApplication extends Application {
    private ApplicationContext ctx;
    private SupernautFxApp app;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void init() throws Exception {
        ctx = ApplicationContext.build()
                .mainClass(HelloSupernautFxApp.class)
                .environments(Environment.CLI).start();
        app = ctx.getBean(HelloSupernautFxApp.class);
    }


    @Override
    public void start(Stage primaryStage) throws Exception {
        app.start(primaryStage);
    }

    @Override
    public void stop() throws Exception {
        app.stop();
        if(ctx != null && ctx.isRunning()) {
            ctx.stop();
        }
    }
}
