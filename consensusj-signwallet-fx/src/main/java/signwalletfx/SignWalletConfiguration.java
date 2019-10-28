package signwalletfx;

import org.bitcoinj.script.Script;
import org.bitcoinj.wallet.DeterministicSeed;
import org.bitcoinj.wallet.KeyChain;
import org.bitcoinj.wallet.UnreadableWalletException;
import org.consensusj.airgap.AirGapTransactionSigner;
import org.consensusj.airgap.keychain.BipStandardDeterministicKeyChain;

import javax.inject.Singleton;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

/**
 * Hardcoded configuration using a well-known test seed.
 */
@Singleton
public class SignWalletConfiguration {
    private final String mnemonicString = "panda diary marriage suffer basic glare surge auto scissors describe sell unique";
    private final Instant creationInstant = LocalDate.of(2019, 4, 10).atStartOfDay().toInstant(ZoneOffset.UTC);
    private final int signingAccountIndex = 0;
    private final Script.ScriptType outputScriptType = Script.ScriptType.P2PKH;

    private final BipStandardDeterministicKeyChain signingKeychain;
    private final AirGapTransactionSigner signer;

    public SignWalletConfiguration() throws UnreadableWalletException {
        DeterministicSeed seed =  new DeterministicSeed(mnemonicString, null, "", creationInstant.getEpochSecond());
        signingKeychain = new BipStandardDeterministicKeyChain(seed, outputScriptType, signingAccountIndex);
        // We need to create some leaf keys in the HD keychain so that they can be found for verifying transactions
        signingKeychain.getKeys(KeyChain.KeyPurpose.RECEIVE_FUNDS, 200);  // Generate first 200 receiving address
        signingKeychain.getKeys(KeyChain.KeyPurpose.CHANGE, 200);         // Generate first 200 change address
        signer = new AirGapTransactionSigner(signingKeychain);
    }

    public AirGapTransactionSigner getSigner() {
        return signer;
    }
}
