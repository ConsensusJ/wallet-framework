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

package omnijwallet;

import com.google.common.collect.ImmutableList;
import javafx.stage.Stage;
import org.bitcoinj.crypto.ChildNumber;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.script.Script;
import org.bitcoinj.wallet.KeyChainGroupStructure;
import org.bitcoinj.walletfx.WalletFxApp;
import org.consensusj.supernautfx.FxmlLoaderFactory;
import org.consensusj.supernautfx.SupernautFxLauncher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.net.URI;

/**
 * Main class for WalletTemplate that uses SupernautFx and does not have
 * to subclass javafx.application.Application.
 */
@Singleton
public class OmniJWalletApp extends WalletFxApp {
    private static final Logger log = LoggerFactory.getLogger(OmniJWalletApp.class);
    private static final String APP_NAME = "OmniJWallet";
    private static final String mainFxmlResName = "main.fxml";
    private static final String mainCssResName = "wallet.css";
    
    public static final ImmutableList<ChildNumber>
            BIP44_ACCOUNT_ZERO_PATH_TESTNET = ImmutableList.of(
                    new ChildNumber(44, true),
                    ChildNumber.ONE_HARDENED,   // coinType for TestNet
                    ChildNumber.ZERO_HARDENED); // account zero


    // This may need to change to BIP
    private static KeyChainGroupStructure STRUCTURE_BIP44 = outputScriptType -> {
        if (outputScriptType != null && outputScriptType != Script.ScriptType.P2PKH) {
            if (outputScriptType == Script.ScriptType.P2WPKH) {
                // TODO: I think this is incorrect
                return BIP44_ACCOUNT_ZERO_PATH_TESTNET;
            } else {
                throw new IllegalArgumentException(outputScriptType.toString());
            }
        } else {
            return BIP44_ACCOUNT_ZERO_PATH_TESTNET;
        }
    };


    public static void main(String[] args) {
        SupernautFxLauncher.superLaunch(OmniJWalletApp.class, args);
    }

    OmniJWalletApp(FxmlLoaderFactory loaderFactory) {
        super(loaderFactory,
                TestNet3Params.get(),
                Script.ScriptType.P2PKH,
                STRUCTURE_BIP44,
                mainFxmlResName,
                mainCssResName);
    }

    @Override
    public String getAppName() {
        return OmniJWalletApp.APP_NAME;
    }

    @Override
    public void init() throws Exception {
        super.init();
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
