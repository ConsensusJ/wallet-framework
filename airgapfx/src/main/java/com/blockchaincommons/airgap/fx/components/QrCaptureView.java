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

package com.blockchaincommons.airgap.fx.components;

import com.blockchaincommons.airgap.fx.camera.CameraService;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;

/**
 * QR Capture view with Camera view, buttons and (not yet)
 * text verification, rescan, etc.
 */
public class QrCaptureView extends BorderPane {
    private final CameraView cameraView;
    private final CameraService service;

    public QrCaptureView(CameraService cameraService) {
        service = cameraService;
        cameraView = new CameraView(cameraService);

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

        setCenter(cameraView);

        BorderPane.setAlignment(startStop, Pos.CENTER);
        BorderPane.setMargin(startStop, new Insets(5));
        setBottom(startStop);
    }
}
