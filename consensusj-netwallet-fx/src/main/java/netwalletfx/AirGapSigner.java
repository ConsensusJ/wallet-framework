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

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.MoreExecutors;
import org.bitcoinj.core.SignatureDecodeException;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionBroadcaster;
import org.bitcoinj.walletfx.WalletSettingsController;
import org.consensusj.airgap.SignedResponseHandler;
import org.consensusj.airgap.SignedResponseParser;
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
import org.consensusj.airgap.fx.components.QrCaptureView;
import org.consensusj.airgap.json.TransactionSignatureResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Incomplete implementation of signing using the Airgap protocol
 * TODO: Get the signed transaction and pass it to the "Send" function
 */
public class AirGapSigner implements HardwareSigner {
    private static final Logger log = LoggerFactory.getLogger(AirGapSigner.class);
    private final UnsignedTxQrGenerator qrJsonGenerator;
    private final NetWalletFxMainWindowController windowController;
    private final SignedResponseParser signedResponseParser = new SignedResponseParser();
    private final SignedResponseHandler signedResponseHandler = new SignedResponseHandler();
    private ExecutorService executorService = Executors.newFixedThreadPool(1);
    private SendRequest pendingSendRequest;

    public AirGapSigner(Wallet wallet, NetWalletFxMainWindowController windowController) {
        qrJsonGenerator = new UnsignedTxQrGenerator(wallet.getParams(), wallet.getActiveKeyChain());
        this.windowController = windowController;
    }

    @Override
    public void displaySigningOverlay(SendRequest sendRequest, SendMoneyController sendMoneyController) {
        pendingSendRequest = sendRequest;
        String qrJson = qrJsonGenerator.createSigningRequestString(pendingSendRequest.tx);
        Image qrImage = QRCodeImages.imageFromString(qrJson, 600, 450);


        URL location = AirGapSigner.class.getResource("QrSigningView.fxml");
        final OverlayableWindowController.OverlayUI<QrSigningViewController> signingViewOverlay = windowController.overlayUI(location);
        signingViewOverlay.ui.setEffect(new DropShadow());
        signingViewOverlay.controller.setUnsignedQrImage(qrImage);
        signingViewOverlay.controller.initCaptureView(windowController.app.cameraService, this::scanListener);
    }

    public void oldDisplaySigningOverlay(SendRequest sendRequest, SendMoneyController sendMoneyController) {
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

    public void scanListener(String result) {
        log.info("QR Scan Result {}", result);
        SendRequest sendRequest = getPendingTransaction();

        TransactionSignatureResponse response = null;
        try {
            response = signedResponseParser.parse(result);
        } catch (IOException e) {
            // TODO: Handle exception properly
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        try {
            signedResponseHandler.signWithResponse(sendRequest.tx, response);
        } catch (SignatureDecodeException e) {
            e.printStackTrace();
        }

        executorService.submit(() -> {
            broadcastTransaction(sendRequest.tx);
        });
    }

    private void broadcastTransaction(Transaction transaction) {
        log.info("Preparing to broadcast tx: {{}", transaction);
        TransactionBroadcaster broadcaster = windowController.app.getWalletAppKit().peerGroup();
        var broadcast = broadcaster.broadcastTransaction(transaction);
        Futures.addCallback(broadcast.future(), new FutureCallback<Transaction>(){

            @Override
            public void onSuccess(Transaction transaction) {
                log.info("Broadcast success, committing transaction: {}", transaction);
                // Commit tx to wallet
                boolean accepted = windowController.app.getWallet().maybeCommitTx(transaction);
                if (!accepted) {
                    log.warn("Transaction already pending");
                }
            }

            @Override
            public void onFailure(Throwable t) {
                log.error("Broadcast failure", t);
            }
        }, MoreExecutors.directExecutor());
    }

    public SendRequest getPendingTransaction() {
        return pendingSendRequest;
    }
}
