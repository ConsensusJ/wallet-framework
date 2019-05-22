package com.blockchaincommons.airgap.fx.demoapp;

import com.blockchaincommons.airgap.fx.camera.CameraService;
import com.blockchaincommons.airgap.fx.components.CameraView;
import com.github.sarxos.webcam.Webcam;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class DemoQRScannerApplication extends Application {
    private static final Logger log = LoggerFactory.getLogger(DemoQRScannerApplication.class);

    private CameraService cameraService;

    @Override
    public void init() {
        // note this is in init as it **must not** be called on the FX Application Thread:
        Webcam camera = Webcam.getWebcams().get(0);
        cameraService = new CameraService(camera);
    }

    @Override
    public void start(Stage primaryStage) {

        cameraService.addQRListener(this::resultListener);

        Button startStop = new Button();
        startStop.textProperty()
                .bind(Bindings
                        .when(cameraService.runningProperty())
                        .then("Stop")
                        .otherwise("Start"));

        startStop.setOnAction(e -> {
            if (cameraService.isRunning()) {
                cameraService.cancel();
            } else {
                cameraService.restart();
            }
        });

        CameraView view = new CameraView(cameraService);

        BorderPane root = new BorderPane(view);
        BorderPane.setAlignment(startStop, Pos.CENTER);
        BorderPane.setMargin(startStop, new Insets(5));
        root.setBottom(startStop);

        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void resultListener(String result) {
        log.info("QR result: {}", result);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
