package org.consensusj.supernautfx;

import javafx.stage.Stage;

/**
 * Interface for Supernaut Apps
 */
public interface SupernautFxApp extends AutoCloseable {

    default void init() throws Exception {
    }
    void start(Stage primaryStage) throws Exception;
    default void stop() throws Exception {
    }
    default void close() throws Exception {
        stop();
    }
}
