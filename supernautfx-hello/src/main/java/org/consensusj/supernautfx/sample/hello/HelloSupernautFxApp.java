package org.consensusj.supernautfx.sample.hello;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.consensusj.supernautfx.SupernautFxApp;
import org.consensusj.supernautfx.sample.hello.service.GreetingService;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * A Supernaut FX App, implements SupernautFxApp but is not required to.
 */
@Singleton
public class HelloSupernautFxApp implements SupernautFxApp {
    private final GreetingService greetingService;

    @Inject
    public HelloSupernautFxApp(GreetingService greetingService) {
        this.greetingService = greetingService;
    }

    @Override
    public void init() {
    }

    @Override
    public void start(Stage primaryStage) {
        String greeting = greetingService.greeting();
        primaryStage.setTitle(greeting);
        Button btn = new Button();
        btn.setText("Say '" + greeting + "'");
        btn.setOnAction(event -> System.out.println(greetingService.greeting()));

        StackPane root = new StackPane();
        root.getChildren().add(btn);
        primaryStage.setScene(new Scene(root, 300, 250));
        primaryStage.show();
    }

    @Override
    public void stop() {
    }
}
