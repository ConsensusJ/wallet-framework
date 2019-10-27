package signwalletfx;

import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.crypto.HDPath;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.script.Script;
import org.bitcoinj.wallet.DeterministicSeed;
import org.bitcoinj.wallet.KeyChain;
import org.bitcoinj.wallet.KeyChainGroup;
import org.bitcoinj.wallet.UnreadableWalletException;
import org.consensusj.airgap.AirGapTransactionSigner;
import org.consensusj.airgap.BipStandardKeyChainGroupStructure;
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
    public static final String mnemonicString = "panda diary marriage suffer basic glare surge auto scissors describe sell unique";
    public static final Instant creationInstant = LocalDate.of(2019, 4, 10).atStartOfDay().toInstant(ZoneOffset.UTC);
    public static final BipStandardKeyChainGroupStructure bip44KeyChainGroupStructure = new BipStandardKeyChainGroupStructure(TestNet3Params.get());
    public static final int signingAccountIndex = 0;
    public static final Script.ScriptType outputScriptType = Script.ScriptType.P2PKH;
    public static final HDPath signingAccountPath = bip44KeyChainGroupStructure.accountHDPathFor(outputScriptType, signingAccountIndex);
    protected static final NetworkParameters netParams = TestNet3Params.get();

    private BipStandardDeterministicKeyChain signingKeychain;
    private KeyChainGroup keyChainGroup;
    private AirGapTransactionSigner signer;

    public DeterministicKeychainSigner() throws UnreadableWalletException {
        DeterministicSeed seed =  new DeterministicSeed(mnemonicString, null, "", creationInstant.getEpochSecond());
        signingKeychain = new BipStandardDeterministicKeyChain(seed, outputScriptType, signingAccountIndex);
        // We need to create some leaf keys in the HD keychain so that they can be found for verifying transactions
        signingKeychain.getKeys(KeyChain.KeyPurpose.RECEIVE_FUNDS, 200);  // Generate first 200 receiving address
        signingKeychain.getKeys(KeyChain.KeyPurpose.CHANGE, 200);         // Generate first 200 change address
        keyChainGroup = KeyChainGroup
                .builder(netParams)
                .addChain(signingKeychain)
                .build();
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

