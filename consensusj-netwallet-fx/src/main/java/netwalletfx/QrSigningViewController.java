package netwalletfx;

import javafx.fxml.FXML;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import org.bitcoinj.walletfx.OverlayWindowController;
import org.bitcoinj.walletfx.OverlayableWindowController;
import org.consensusj.airgap.fx.camera.CameraService;
import org.consensusj.airgap.fx.components.QrCaptureView;

import javax.inject.Singleton;
import java.util.function.Consumer;

/**
 *
 */
@Singleton
public class QrSigningViewController implements OverlayWindowController {
    private OverlayableWindowController.OverlayUI overlayUI;
    @FXML private ImageView unsignedQrImage;
    @FXML private VBox captureBox;

    private QrCaptureView captureView;
    
    public void setUnsignedQrImage(Image image) {
        if (unsignedQrImage != null) {
            unsignedQrImage.setImage(image);
        }
    }

    public void initCaptureView(CameraService cameraService, Consumer<String> scanListener) {
        // TODO: Make sure to gracefully handle case (e.g. inform user) where there is no camera
        captureView  = new QrCaptureView(cameraService, scanListener, this::closeListener);
        if (captureBox != null) {
            captureBox.getChildren().add(captureView);
        }
    }

    @Override
    public OverlayableWindowController.OverlayUI getOverlayUI() {
        return overlayUI;
    }

    @Override
    public void setOverlayUI(OverlayableWindowController.OverlayUI ui) {
        overlayUI = ui;
    }
    
    private void closeListener(Object result) {
        closeOverlay();
    }

    void closeOverlay() {
        overlayUI.done();
    }
}
