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

package org.consensusj.airgap.fx.components;

import org.consensusj.airgap.fx.camera.CameraService;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

/**
 * QR Capture view with Camera view, buttons, text verification, rescan, etc.
 */
public class QrCaptureView extends BorderPane {
    private static final Logger log = LoggerFactory.getLogger(QrCaptureView.class);
    private final Consumer<String> scanListener;
    private final Consumer<Object> closeListener;
    private final CameraView cameraView;
    private final CameraService service;
    private TextArea previewText;
    private Button cancelButton;
    private Button rescanButton;
    private Button acceptButton;

    private String previewResult;

    /**
     * JavaFX View with UI for a WebCam-based QR Code Scanner
     * 
     * @param cameraService QR Code Capturing Camera Service
     * @param scanListener Listener called when QR code is accepted (accept button)
     * @param closeListener Listener called when view is closed (cancel or accept button)
     */
    public QrCaptureView(CameraService cameraService,  Consumer<String> scanListener, Consumer<Object> closeListener) {
        service = cameraService;
        cameraView = new CameraView(cameraService);
        this.scanListener = scanListener;
        this.closeListener = closeListener;

        setCenter(cameraView);

        Pane bottomBox = bottom();

        BorderPane.setAlignment(cameraView, Pos.CENTER);
        BorderPane.setMargin(cameraView, new Insets(5));
        BorderPane.setAlignment(bottomBox, Pos.CENTER);
        BorderPane.setMargin(bottomBox, new Insets(5));
        setBottom(bottomBox);

        cameraService.addQRListener(this::resultListener);

        scan();
    }

    private VBox bottom() {
        previewText = new TextArea();

        Pane buttonPane = buttonBox();

        VBox bottomBox = new VBox(10);
        bottomBox.getChildren().addAll(previewText, buttonPane);
        return bottomBox;
    }

    private HBox buttonBox() {
        cancelButton = new Button("Cancel");
        cancelButton.setOnAction(this::cancelAction);

        rescanButton = new Button("Rescan");
        rescanButton.setOnAction(this::rescanAction);

        acceptButton = new Button("Accept");
        acceptButton.setOnAction(this::acceptAction);

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().addAll(cancelButton, rescanButton, acceptButton);
        return buttonBox;
    }

    private void acceptAction(ActionEvent actionEvent) {
        scanListener.accept(previewResult);
        closeParent();
    }

    private void cancelAction(ActionEvent actionEvent) {
        closeParent();
    }

    private void rescanAction(ActionEvent e) {
        scan();
    }

    private void resultListener(String result) {
        if (service.isRunning()) {
            service.cancel();
        }
        log.info("QR result: {}", result);
        previewText.setText(result);
        previewResult = result;

        rescanButton.setDisable(false);
        acceptButton.setDisable(false);
        
    }

    private void scan() {
        if (!service.isRunning()) {
            service.restart();
        }
        previewText.setText("");
        acceptButton.setDisable(true);
        rescanButton.setDisable(true);
    }

    private void stopCameraService() {
        if (service.isRunning()) {
            service.cancel();
        }
    }

    private void closeParent() {
        stopCameraService();
        closeListener.accept(null);
    }

}
