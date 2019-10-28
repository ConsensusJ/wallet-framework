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

package org.consensusj.airgap.fx.camera;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamResolution;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.util.function.Consumer;

/**
 *
 * Based on https://github.com/sarxos/webcam-capture/tree/master/webcam-capture-examples/webcam-capture-javafx-service
 */
public class CameraService extends Service<Image> {
    private static final Logger log = LoggerFactory.getLogger(CameraService.class);
    private static final boolean logViewSizes = true;

    private final Webcam camera;
    private final WebcamResolution resolution;
    private final WritableImage fxImage;
    private Consumer<String> listener = null;

    public CameraService(Webcam camera, WebcamResolution resolution) {
        this.camera = camera;
        this.resolution = resolution;
        //camera.setCustomViewSizes(resolution.getSize());
        camera.setViewSize(resolution.getSize());
        if (logViewSizes) {
            log.info("Camera supports the following sizes:");
            for (Dimension size : camera.getViewSizes()) {
                log.info(size.toString());
            }
        }

        fxImage = new WritableImage(resolution.getWidth(), resolution.getHeight());
    }

    public CameraService(Webcam camera) {
        this(camera, WebcamResolution.VGA);
    }

    @Override
    public Task<Image> createTask() {
        return new Task<>() {
            @Override
            protected Image call() {

                try {
                    camera.open();
                    while (!isCancelled()) {
                        if (camera.isImageNew()) {
                            BufferedImage bimg = camera.getImage();
                            log.debug("BufferedImage: {}", bimg);
                            log.debug("BufferedImage: {} x {}", bimg.getWidth(), bimg.getHeight());
                            SwingFXUtils.toFXImage(bimg, fxImage);
                            updateValue(fxImage);
                            Result result = scanForQR(bimg);
                            if (result != null && listener != null) {
                                Platform.runLater(() -> listener.accept(result.getText()));
                            }
                        }
                    }
                    log.info("Cancelled, closing camera");
                    camera.close();
                    log.debug("Camera closed");
                    return getValue();
                } finally {
                    camera.close();
                }
            }

        };
    }
    
    public void addQRListener(Consumer<String> listener) {
        this.listener = listener;
    }
    private Result scanForQR(BufferedImage image) {
        // TODO: Considering doing this on a different thread
        Result result = null;
        LuminanceSource source = new BufferedImageLuminanceSource(image);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

        try {
            result = new MultiFormatReader().decode(bitmap);
            log.debug("QR Result: {}", result.getText());
        } catch (NotFoundException e) {
            // fall thru, it means there is no QR code in image
            log.trace("QR code not found in image");
        }
        return result;
    }

    public int getCameraWidth() {
        return resolution.getSize().width ;
    }

    public int getCameraHeight() {
        return resolution.getSize().height ;
    }

}
