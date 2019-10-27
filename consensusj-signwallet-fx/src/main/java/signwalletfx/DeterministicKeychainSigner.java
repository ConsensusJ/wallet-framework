package signwalletfx;

import org.bitcoinj.script.Script;
import org.bitcoinj.wallet.DeterministicSeed;
import org.bitcoinj.wallet.KeyChain;
import org.bitcoinj.wallet.UnreadableWalletException;
import org.consensusj.airgap.AirGapTransactionSigner;
import org.consensusj.airgap.json.TransactionSignatureResponse;
import org.consensusj.airgap.json.TransactionSigningRequest;
import org.consensusj.airgap.keychain.BipStandardDeterministicKeyChain;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

/**
 *
 */
public class DeterministicKeychainSigner {
    private final String mnemonicString = "panda diary marriage suffer basic glare surge auto scissors describe sell unique";
    private final Instant creationInstant = LocalDate.of(2019, 4, 10).atStartOfDay().toInstant(ZoneOffset.UTC);
    private final int signingAccountIndex = 0;
    private final Script.ScriptType outputScriptType = Script.ScriptType.P2PKH;

    private final BipStandardDeterministicKeyChain signingKeychain;
    private final AirGapTransactionSigner signer;

    public DeterministicKeychainSigner() throws UnreadableWalletException {
        DeterministicSeed seed =  new DeterministicSeed(mnemonicString, null, "", creationInstant.getEpochSecond());
        signingKeychain = new BipStandardDeterministicKeyChain(seed, outputScriptType, signingAccountIndex);
        // We need to create some leaf keys in the HD keychain so that they can be found for verifying transactions
        signingKeychain.getKeys(KeyChain.KeyPurpose.RECEIVE_FUNDS, 200);  // Generate first 200 receiving address
        signingKeychain.getKeys(KeyChain.KeyPurpose.CHANGE, 200);         // Generate first 200 change address
        signer = new AirGapTransactionSigner(signingKeychain);
    }

    public TransactionSignatureResponse sign(TransactionSigningRequest request) {
        return signer.signatureResponseFromSigningRequest(request);
    }

    public String sign(String requestJsonString) {
        TransactionSigningRequest request = signer.parseSigningRequestJson(requestJsonString);
        TransactionSignatureResponse response = sign(request);
        return signer.serializeResponse(response);
    }
}

