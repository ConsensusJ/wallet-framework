package com.blockchaincommons.airgap.fx.camera;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.util.function.Consumer;

/**
 *
 * Based on https://github.com/sarxos/webcam-capture/tree/master/webcam-capture-examples/webcam-capture-javafx-service
 */
public class CameraService extends Service<Image> {
    private static final Logger log = LoggerFactory.getLogger(CameraService.class);

    private final Webcam camera;
    private final WebcamResolution resolution ;
    private Consumer<String> listener = null;

    public CameraService(Webcam camera, WebcamResolution resolution) {
        this.camera = camera;
        this.resolution = resolution;
        camera.setCustomViewSizes(resolution.getSize());
        camera.setViewSize(resolution.getSize());
    }

    public CameraService(Webcam camera) {
        this(camera, WebcamResolution.HVGA);
    }

    @Override
    public Task<Image> createTask() {
        return new Task<>() {
            @Override
            protected Image call() throws Exception {

                try {
                    camera.open();
                    while (!isCancelled()) {
                        if (camera.isImageNew()) {
                            BufferedImage bimg = camera.getImage();
                            // TODO: Consider passing WritableImage for possible perf boost
                            updateValue(SwingFXUtils.toFXImage(bimg, null));
                            Result result = scanForQR(bimg);
                            if (result != null) {
                                if (listener != null) {
                                    Platform.runLater(() -> listener.accept(result.getText()));
                                }
                            }
                        }
                    }
                    log.info("Cancelled, closing camera");
                    camera.close();
                    log.info("Camera closed");
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
