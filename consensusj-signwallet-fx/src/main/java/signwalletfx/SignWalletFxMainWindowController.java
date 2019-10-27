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
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.bitcoinj.wallet.UnreadableWalletException;
import org.bitcoinj.walletfx.controls.NotificationBarPane;
import org.bitcoinj.walletfx.utils.QRCodeImages;
import org.bitcoinj.walletfx.utils.TextFieldValidator;
import org.consensusj.airgap.fx.components.QrCaptureView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 */
@Singleton
public class SignWalletFxMainWindowController {
    private static final Logger log = LoggerFactory.getLogger(SignWalletFxMainWindowController.class);
    @FXML private Button signBtn;
    @FXML private ImageView signedTxImageView;

    private final SignWalletFxApp app;
    private Stage standaloneQrScanStage;
    private DeterministicKeychainSigner deterministicKeychainSigner;


    public SignWalletFxMainWindowController(SignWalletFxApp app) throws UnreadableWalletException {
        this.app = app;
        deterministicKeychainSigner = new DeterministicKeychainSigner();
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
        String signedTxQrString = deterministicKeychainSigner.sign(signingRequestJsonString);
        Image qrImage = QRCodeImages.imageFromString(signedTxQrString, 600, 450);
        signedTxImageView.setImage(qrImage);
    }

    private void closeListener(Object unused) {
        if (standaloneQrScanStage != null) {
            standaloneQrScanStage.hide();
        }
    }


}
