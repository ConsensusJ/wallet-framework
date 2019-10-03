package org.consensusj.airgap;

import org.consensusj.airgap.json.InputSignature;
import org.consensusj.airgap.json.TransactionSignatureResponse;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.LegacyAddress;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.SignatureDecodeException;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionInput;
import org.bitcoinj.crypto.TransactionSignature;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import org.bitcoinj.script.ScriptException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Base64;
import java.util.List;

/**
 *
 */
public class SignedResponseHandler {
    private static final Logger log = LoggerFactory.getLogger(SignedResponseHandler.class);
    private final NetworkParameters netParams = TestNet3Params.get();
    private final boolean verify = true;

    public void signWithResponse(Transaction transaction, TransactionSignatureResponse response) throws SignatureDecodeException {
        List<InputSignature> inputSignatures = response.getTransaction().getInputSignatures();
        int inputIndex = 0;
        for (InputSignature inputSig : inputSignatures) {
            TransactionSignature signature = sigFromEcString(inputSig.getEcSignature());
            ECKey pubKey = pubKeyFromString(inputSig.getEcPublicKey());
            this.signInput(transaction.getInput(inputIndex), signature, pubKey);
            if (verify) {
                correctlySpendsInput(transaction, inputIndex, LegacyAddress.fromKey(netParams, pubKey));
            }
            inputIndex++;
        }
    }

    TransactionSignature signature(TransactionSignatureResponse response, int index) throws SignatureDecodeException {
        String ecSignature = response.getTransaction().getInputSignatures().get(index).getEcSignature();
        return sigFromEcString(ecSignature);
    }

    static TransactionSignature sigFromEcString(String ecSignature) throws SignatureDecodeException{
        byte[]  signatureBytes = Base64.getDecoder().decode(ecSignature);
        return TransactionSignature.decodeFromBitcoin(signatureBytes,
                true,
                true);
    }

    static ECKey pubKeyFromString(String ecPublicKey) {
        byte[]  pubKeyBytes = Base64.getDecoder().decode(ecPublicKey);
        return ECKey.fromPublicOnly(pubKeyBytes);
    }

    ECKey pubKey(TransactionSignatureResponse response, int index) throws SignatureDecodeException {
        String ecPublicKey = response.getTransaction().getInputSignatures().get(index).getEcPublicKey();
        return pubKeyFromString(ecPublicKey);
    }

    public void signInput(TransactionInput input, TransactionSignature signature, ECKey publicKey ) {
        input.setScriptSig(ScriptBuilder.createInputScript(signature, publicKey));
        input.setWitness(null);
        log.info("Signed input with signature chunk: {}",  input.getScriptSig().getChunks().get(0));
    }

    /**
     * Verify that a transaction correctly spends the input specified by index. Throws {@link ScriptException}
     * if verification fails.
     *
     * @param tx The transaction to verify
     * @param inputIndex The input to verify
     * @param fromAddr The address we are trying to spend funds from
     * @throws ScriptException If {@code scriptSig#correctlySpends} fails with exception
     */
    protected static void correctlySpendsInput(Transaction tx, int inputIndex, Address fromAddr) throws ScriptException {
        log.info("About to validate signed input {} of transaction {}", inputIndex, tx.getTxId().toString());
        Script scriptSig = tx.getInputs().get(inputIndex).getScriptSig();
        Script scriptPubKey = ScriptBuilder.createOutputScript(fromAddr);
        log.info("scriptSig: {}", scriptSig);
        log.info("scriptPubKey: {}", scriptPubKey);
        scriptSig.correctlySpends(tx, inputIndex, scriptPubKey, Script.ALL_VERIFY_FLAGS);
        log.info("Transaction correctly spends input {} (no exception thrown)", inputIndex);
    }
}
