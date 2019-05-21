package airgapfxwallet;

import com.blockchaincommons.airgap.UnsignedTxQrGenerator;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.wallet.Wallet;
import org.bitcoinj.walletfx.OverlayableWindowController;
import org.bitcoinj.walletfx.SendMoneyController;
import org.bitcoinj.walletfx.utils.QRCodeImages;
import org.bitcoinj.walletfx.HardwareSigner;

/**
 * Incomplete implementation of signing using the Airgap protocol
 * TODO: Get the signed transaction and pass it to the "Send" function
 */
public class AirGapSigner implements HardwareSigner {
    private final UnsignedTxQrGenerator qrJsonGenerator;
    private final OverlayableWindowController windowController;

    public AirGapSigner(Wallet wallet, OverlayableWindowController windowController) {
        qrJsonGenerator = new UnsignedTxQrGenerator(wallet);
        this.windowController = windowController;
    }

    @Override
    public void displaySigningOverlay(Transaction tx, SendMoneyController sendMoneyController) {
        String qrJson = qrJsonGenerator.txToSigningReqJson(tx);
        Image qrImage = QRCodeImages.imageFromString(qrJson, 600, 450);
        ImageView view = new ImageView(qrImage);
        view.setEffect(new DropShadow());
        // Embed the image in a pane to ensure the drop-shadow interacts with the fade nicely, otherwise it looks weird.
        // Then fix the width/height to stop it expanding to fill the parent, which would result in the image being
        // non-centered on the screen. Finally fade/blur it in.
        Pane pane = new Pane(view);
        pane.setMaxSize(qrImage.getWidth(), qrImage.getHeight());
        final OverlayableWindowController.OverlayUI<SendMoneyController> overlay = windowController.overlayUI(pane, sendMoneyController);
        view.setOnMouseClicked(event1 -> overlay.done());
    }

    @Override
    public String getButtonText() {
        return "Airgap Sign";
    }
}
