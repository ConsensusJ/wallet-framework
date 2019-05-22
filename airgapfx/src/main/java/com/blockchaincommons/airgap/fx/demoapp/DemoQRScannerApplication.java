/*
 * Copyright by the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.blockchaincommons.airgap.fx.demoapp;

import com.blockchaincommons.airgap.fx.camera.CameraService;
import com.blockchaincommons.airgap.fx.components.QrCaptureView;
import com.github.sarxos.webcam.Webcam;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Demonstration QR code scanner app with no bitcoinj/crypto dependencies
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

        QrCaptureView captureView  = new QrCaptureView(cameraService);

        Scene scene = new Scene(captureView);
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
