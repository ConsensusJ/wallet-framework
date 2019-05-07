package org.consensusj.supernautfx;

import io.micronaut.context.ApplicationContext;
import io.micronaut.context.env.Environment;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Should contain boilerplate initialization (TBD)
 */
public class SupernautFxLauncher extends Application {
    private static Class<? extends SupernautFxApp> mainClass;
    private ApplicationContext ctx;
    private SupernautFxApp app;

    public static void supernautLaunch(Class<? extends SupernautFxApp> mainClass,  String[] args) {
        SupernautFxLauncher.mainClass = mainClass;
        Application.launch(args);
    }

    @Override
    public void init() throws Exception {
        ctx = ApplicationContext.build()
                .mainClass(mainClass)
                .environments(Environment.CLI).start();
        app = ctx.getBean(mainClass);
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
