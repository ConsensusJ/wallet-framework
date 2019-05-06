package org.consensusj.supernautfx;

import javafx.stage.Stage;

/**
 * (Optional) interface for Supernaut Apps
 */
public interface SupernautFxApp {

    void init();
    void start(Stage primaryStage);
    void stop();
}
