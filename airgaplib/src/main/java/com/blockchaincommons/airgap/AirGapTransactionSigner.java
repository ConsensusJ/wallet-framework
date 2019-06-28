package com.blockchaincommons.airgap;

import com.blockchaincommons.airgap.json.Header;
import com.blockchaincommons.airgap.json.Input;
import com.blockchaincommons.airgap.json.InputSignature;
import com.blockchaincommons.airgap.json.Output;
import com.blockchaincommons.airgap.json.TransactionSignatureResponse;
import com.blockchaincommons.airgap.json.TransactionSigningRequest;
import com.blockchaincommons.airgap.keychain.BipStandardDeterministicKeyChain;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.LegacyAddress;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionInput;
import org.bitcoinj.core.TransactionOutPoint;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.script.ScriptBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * A service object that performs Airgap signing (without display or confirmation)
 */
public class AirGapTransactionSigner {
    private final BipStandardDeterministicKeyChain keyChain;
    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Construct a signer from a DeterministicKeyChain object
     *
     * @param keyChain A BIP-44 compliant KeyChain object (with private keys)
     */
    public AirGapTransactionSigner(BipStandardDeterministicKeyChain keyChain) {
        this.keyChain = keyChain;
    }

    /**
     * Parse a JSON string into a TransactionSigningRequest
     *
     * @param json string to parse
     * @return Java POJO for the JSON
     */
    public TransactionSigningRequest parseSigningRequestJson(String json) {
        TransactionSigningRequest request = null;
        try {
            request = mapper.readValue(json, TransactionSigningRequest.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return request;
    }

    /**
     * Serialize a transaction signature response
     *
     * @param response object to serialize
     * @return JSON string containing serialized POJO
     */
    public String serializeResponse(TransactionSignatureResponse response) {
        try {
            return mapper.writeValueAsString(response);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Create a signature response POJO (without display or confirmation) for a signing request
     *
     * @param request Request POJO
     * @return Signed response POJO
     */
    public TransactionSignatureResponse signatureResponseFromSigningRequest(TransactionSigningRequest request) {
        Transaction tx = signTransaction(request);
        String asset = "BTCT";

        TransactionSignatureResponse response = new TransactionSignatureResponse();
        response.setHeader(new Header("AirgappedSigning", 1L));
        com.blockchaincommons.airgap.json.Transaction jsonTx = new com.blockchaincommons.airgap.json.Transaction();
        jsonTx.setUid(UnsignedTxQrGenerator.randomUid());
        jsonTx.setAsset(asset);

        List<InputSignature> signatureList = new ArrayList<>();
        for (Input input : request.getTransaction().getInputs())  {
            signatureList.add(buildSignature(tx, input));
        }
        jsonTx.setInputSignatures(signatureList);
        response.setTransaction(jsonTx);
        return response;
    }

    /**
     * Create an {@code InputSignature} for transaction input
     *
     * @param tx Correctly signed bitcoinj transaction
     * @param input  Airgap Input POJO
     * @return Airgap input signature POJO
     */
    private InputSignature buildSignature(Transaction tx, Input input)  {
        TransactionInput txInput = tx.getInput(input.getInputIndex());
        byte[] sigData = txInput.getScriptSig().getChunks().get(0).data;
        byte[] pubKeyData = txInput.getScriptSig().getChunks().get(1).data;
        String ecSigBase64 = Base64.getEncoder().encodeToString(sigData);
        String ecPubKey = Base64.getEncoder().encodeToString(pubKeyData);
        InputSignature inputSignature = new InputSignature();
        inputSignature.setEcPublicKey(ecPubKey);
        inputSignature.setEcSignature(ecSigBase64);
        inputSignature.setUid(UnsignedTxQrGenerator.randomUid());
        return inputSignature;
    }

    /**
     * Create a signed bitcoinj transaction from the signing request
     *
     * @param request the request POJO
     * @return a signed bitcoinj transaction
     */
    public Transaction signTransaction(TransactionSigningRequest request) {
        NetworkParameters netParams = TestNet3Params.get(); // TODO: Deduce network from request

        // Create an empty bitcoinj transaction
        Transaction tx = new Transaction(netParams);

        // For each output in the signing request, add an output the the bitcoinj transaction
        for (Output output : request.getTransaction().getOutputs()) {
            tx.addOutput(Coin.valueOf(output.getAmount()),
                    Address.fromString(netParams, output.getReceiver()));
        }

        // For each input in the signing request, add a signed input to the bitcoinj transaction
        for (Input input : request.getTransaction().getInputs()) {
            TransactionOutPoint outPoint = new TransactionOutPoint(netParams,   // TESTNET
                    1,  // TODO: UTXO output transaction index should not be hard-coded
                     Sha256Hash.wrap(input.getTxHash()));
            Address fromAddr = LegacyAddress.fromBase58(netParams, input.getSender());
            DeterministicKey fromKey = keyChain.findKeyFromPubHash(fromAddr.getHash());
            tx.addSignedInput(outPoint, ScriptBuilder.createOutputScript(fromAddr), fromKey);
        }
        return tx;
    }
}
