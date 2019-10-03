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

package netwalletfx;

import org.consensusj.airgap.UnsignedTxQrGenerator;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import org.bitcoinj.wallet.SendRequest;
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
    private SendRequest pendingSendRequest;

    public AirGapSigner(Wallet wallet, OverlayableWindowController windowController) {
        qrJsonGenerator = new UnsignedTxQrGenerator(wallet.getParams(), wallet.getActiveKeyChain());
        this.windowController = windowController;
    }

    @Override
    public void displaySigningOverlay(SendRequest sendRequest, SendMoneyController sendMoneyController) {
        pendingSendRequest = sendRequest;
        String qrJson = qrJsonGenerator.createSigningRequestString(pendingSendRequest.tx);
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

    public SendRequest getPendingTransaction() {
        return pendingSendRequest;
    }
}
