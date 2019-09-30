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

package airgapfxwallet;

import com.blockchaincommons.airgap.SignedResponseHandler;
import com.blockchaincommons.airgap.SignedResponseParser;
import com.blockchaincommons.airgap.fx.components.QrCaptureView;
import com.blockchaincommons.airgap.json.TransactionSignatureResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.MoreExecutors;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.SignatureDecodeException;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionBroadcaster;
import org.bitcoinj.wallet.SendRequest;
import org.bitcoinj.walletfx.OverlayableWindowController;
import org.bitcoinj.walletfx.SendMoneyController;
import org.bitcoinj.walletfx.WalletMainWindowController;
import org.bitcoinj.walletfx.WalletSettingsController;
import org.bitcoinj.walletfx.cell.TransactionListCell;
import org.bitcoinj.walletfx.cell.TransactionStringConverter;
import org.bitcoinj.walletfx.controls.ClickableBitcoinAddress;
import org.bitcoinj.walletfx.controls.NotificationBarPane;
import org.bitcoinj.walletfx.utils.easing.EasingMode;
import org.bitcoinj.walletfx.utils.easing.ElasticInterpolator;

import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * Main window controller for Blockchain Commons Network Wallet
 */
@Singleton
public class AirgapFxMainWindowController extends WalletMainWindowController {
    private static final Logger log = LoggerFactory.getLogger(AirgapFxMainWindowController.class);
    @FXML private HBox controlsBox;
    @FXML private Label balance;
    @FXML private Button sendMoneyOutBtn;
    @FXML private Button scanBtn;
    @FXML private ClickableBitcoinAddress addressControl;
    @FXML private ListView<Transaction> transactionListView;

    protected final AirgapFxWalletApp app;
    private AirGapSigner airGapHardwareSigner;
    private final SignedResponseParser signedResponseParser = new SignedResponseParser();
    private final SignedResponseHandler signedResponseHandler = new SignedResponseHandler();

    private ExecutorService executorService = Executors.newFixedThreadPool(1);

    public AirgapFxMainWindowController(AirgapFxWalletApp app) {
        super(app);
        this.app = app;
    }

    // Called by FXMLLoader.
    public void initialize() {
        addressControl.setOpacity(0.0);
    }


    public void onBitcoinSetup() {
        super.onBitcoinSetup();
        airGapHardwareSigner = new AirGapSigner(app.getWallet(), this);

        addressControl.addressProperty().bind(model.addressProperty());
        balance.textProperty().bind(createBalanceStringBinding(model.balanceProperty()));
        // Don't let the user click send money when the wallet is empty.
        sendMoneyOutBtn.disableProperty().bind(model.balanceProperty().isEqualTo(Coin.ZERO));
        // Don't let the user click the "scan QR" button if there is no camera
        var cameraAvailable = app.cameraService != null;
        scanBtn.setDisable(!cameraAvailable);
        // We wait to onBitcoinSetup() to do this because prior to that getWallet() will return null.
        TransactionStringConverter converter = new TransactionStringConverter(this.app.getWallet());
        transactionListView.setCellFactory(list -> {
            var cell = new TransactionListCell(converter);
            // TODO: This doesn't handle selection highlighting correctly
            cell.setStyle("-fx-background-color: transparent; -fx-text-fill: black");
            return cell;
        });
        Bindings.bindContent(transactionListView.getItems(), model.getTransactionList());
    }


    @FXML
    private void sendMoneyOut(ActionEvent event) {
        // Hide this UI and show the send money UI. This UI won't be clickable until the user dismisses send_money.
        OverlayableWindowController.OverlayUI<SendMoneyController> screen = overlayUI("send_money.fxml");
        screen.controller.setSigner(airGapHardwareSigner);  // Sign via QR codes over "Air Gap"
    }

    @FXML
    private void settingsClicked(ActionEvent event) {
        OverlayableWindowController.OverlayUI<WalletSettingsController> screen = overlayUI("wallet_settings.fxml");
        screen.controller.initialize(null);
    }

    @FXML
    public void scanClicked(ActionEvent actionEvent) {
        log.info("scanClicked");

        QrCaptureView captureView  = new QrCaptureView(app.cameraService, this::scanListener);

        Scene scene = new Scene(captureView);
        final Stage stage = new Stage();
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void restoreFromSeedAnimation() {
        // Buttons slide out ...
        TranslateTransition leave = new TranslateTransition(Duration.millis(1200), controlsBox);
        leave.setByY(80.0);
        leave.play();
    }

    @Override
    protected void readyToGoAnimation() {
        // Buttons slide in and clickable address appears simultaneously.
        TranslateTransition arrive = new TranslateTransition(Duration.millis(1200), controlsBox);
        arrive.setInterpolator(new ElasticInterpolator(EasingMode.EASE_OUT, 1, 2));
        arrive.setToY(0.0);
        FadeTransition reveal = new FadeTransition(Duration.millis(1200), addressControl);
        reveal.setToValue(1.0);
        ParallelTransition group = new ParallelTransition(arrive, reveal);
        group.setDelay(NotificationBarPane.ANIM_OUT_DURATION);
        group.setCycleCount(1);
        group.play();
    }

    private void scanListener(String result) {
        log.info("QR Scan Result {}", result);
        SendRequest sendRequest = airGapHardwareSigner.getPendingTransaction();

        TransactionSignatureResponse response = null;
        try {
            response = signedResponseParser.parse(result);
        } catch (IOException e) {
            e.printStackTrace();
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
        TransactionBroadcaster broadcaster = app.getWalletAppKit().peerGroup();
        var broadcast = broadcaster.broadcastTransaction(transaction);
        Futures.addCallback(broadcast.future(), new FutureCallback<Transaction>(){

            @Override
            public void onSuccess(Transaction transaction) {
                log.info("Broadcast success, committing transaction: {}", transaction);
                // Commit tx to wallet
                boolean accepted = app.getWallet().maybeCommitTx(transaction);
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
}
