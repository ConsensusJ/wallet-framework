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

import com.blockchaincommons.airgap.BipStandardKeyChainGroupStructure;
import com.blockchaincommons.airgap.fx.camera.CameraService;
import javafx.stage.Stage;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.script.Script;
import org.bitcoinj.wallet.KeyChainGroupStructure;
import org.bitcoinj.walletfx.WalletFxApp;
import org.consensusj.supernautfx.FxmlLoaderFactory;
import org.consensusj.supernautfx.SupernautFxLauncher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.util.concurrent.TimeoutException;

import com.github.sarxos.webcam.Webcam;

/**
 * Main class for WalletTemplate that uses SupernautFx and does not have
 * to subclass javafx.application.Application.
 */
@Singleton
public class AirgapFxWalletApp extends WalletFxApp {
    private static final Logger log = LoggerFactory.getLogger(AirgapFxWalletApp.class);
    private static final String APP_NAME = "Blockchain Commons Reference Network-Wallet";
    private static final String mainFxmlResName = "main.fxml";
    private static final String mainCssResName = "wallet.css";

    private static KeyChainGroupStructure STRUCTURE_BIP44 = new BipStandardKeyChainGroupStructure(TestNet3Params.get());

    public CameraService cameraService = null;
    private static final int GET_WEBCAMS_TIMEOUT = 1000;   // Timeout in milliseconds

    public static void main(String[] args) {
        SupernautFxLauncher.superLaunch(AirgapFxWalletApp.class, args);
    }

    AirgapFxWalletApp(FxmlLoaderFactory loaderFactory) {
        super(loaderFactory,
                TestNet3Params.get(),
                Script.ScriptType.P2PKH,
                STRUCTURE_BIP44,
                mainFxmlResName,
                mainCssResName);
    }

    @Override
    public String getAppName() {
        return AirgapFxWalletApp.APP_NAME;
    }

    @Override
    public void init() throws Exception {
        super.init();
        // note this is in init as it **must not** be called on the FX Application Thread:
        Webcam camera = null;
        try {
            camera = Webcam.getWebcams(GET_WEBCAMS_TIMEOUT).get(0);
        } catch (TimeoutException toex) {
            log.warn("No Webcam found within {} ms", GET_WEBCAMS_TIMEOUT);
        }
        if (camera != null) {
            cameraService = new CameraService(camera);
        }
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        super.start(primaryStage);
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        // Forcibly terminate the JVM because Orchid likes to spew non-daemon threads everywhere.
        Runtime.getRuntime().exit(0);
    }
}
