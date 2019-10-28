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

package signwalletfx;

import javax.inject.Singleton;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.bitcoinj.walletfx.utils.QRCodeImages;
import org.consensusj.airgap.AirGapTransactionSigner;
import org.consensusj.airgap.fx.components.QrCaptureView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main window controller for Java FX signing wallet application
 */
@Singleton
public class SignWalletFxMainWindowController {
    private static final Logger log = LoggerFactory.getLogger(SignWalletFxMainWindowController.class);
    @FXML private Button signBtn;
    @FXML private ImageView signedTxImageView;

    private final SignWalletFxApp app;
    private Stage standaloneQrScanStage;
    private AirGapTransactionSigner signer;


    /**
     * Temporary constructor until SuperautFX has better configuration options
     *
     * @param app The Application main class
     * @param configuration A configuration object containing an AirGapTransactionSigner
     * @deprecated With proper Micronaut configuration support in SupernautFX, signer can be directly injected.
     */
    @Deprecated
    public SignWalletFxMainWindowController(SignWalletFxApp app, SignWalletConfiguration configuration) {
        this(app, configuration.getSigner());
    }

    /**
     * Preferred constructor in the future.
     *
     * @param app The Application main class
     * @param signer An airgap signer configured with a DeterministicKeychain
     */
    public SignWalletFxMainWindowController(SignWalletFxApp app, AirGapTransactionSigner signer) {
        this.app = app;
        this.signer = signer;
    }

    // Called by FXMLLoader.
    public void initialize() {
    }

    Scene controllerStart(Pane mainUI, String cssResourceName) {
        Scene scene = new Scene(mainUI);
        scene.getStylesheets().add(getClass().getResource(cssResourceName).toString());
        return scene;
    }

    @FXML
    private void signTransaction(ActionEvent event) {
        displayCaptureWindow();
    }

    /**
     * Display standalone QR capture window
     */
    public void displayCaptureWindow() {
        QrCaptureView captureView  = new QrCaptureView(app.cameraService, this::scanListener, this::closeListener);

        Scene scene = new Scene(captureView);
        standaloneQrScanStage = new Stage();
        standaloneQrScanStage.setScene(scene);
        standaloneQrScanStage.show();
    }

    private void scanListener(String signingRequestJsonString) {
        String signedTxQrString = signer.signatureResponseFromSigningRequestJson(signingRequestJsonString);
        Image qrImage = QRCodeImages.imageFromString(signedTxQrString, 600, 450);
        signedTxImageView.setImage(qrImage);
    }

    private void closeListener(Object unused) {
        if (standaloneQrScanStage != null) {
            standaloneQrScanStage.hide();
        }
    }


}
