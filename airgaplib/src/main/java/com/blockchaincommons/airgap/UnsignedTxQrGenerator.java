package com.blockchaincommons.airgap;

import com.blockchaincommons.airgap.json.Derivation;
import com.blockchaincommons.airgap.json.Derivation_;
import com.blockchaincommons.airgap.json.Header;
import com.blockchaincommons.airgap.json.Input;
import com.blockchaincommons.airgap.json.Output;
import com.blockchaincommons.airgap.json.TransactionSigningRequest;
import com.blockchaincommons.airgap.json.UnsignedTransaction;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionInput;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.wallet.Wallet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 *
 */
public class UnsignedTxQrGenerator {
    private static final Logger log = LoggerFactory.getLogger(UnsignedTxQrGenerator.class);
    private static NetworkParameters netParams = TestNet3Params.get();
    private final ObjectMapper mapper;
    private final Wallet wallet;

    public UnsignedTxQrGenerator(Wallet wallet) {
        this.wallet = wallet;
        mapper = new ObjectMapper();
    }


    public TransactionSigningRequest of(Transaction transaction) {
        TransactionSigningRequest txSignRequest;
        Header header = new Header("AirgappedSigning", 1L);

        List<Input> inputs = new ArrayList<>();
        transaction.getInputs().forEach((txInput) -> {
            inputs.add(inputFromTxInput(txInput));
        });
        List<Output> outputs = new ArrayList<>();
        transaction.getOutputs().forEach((txOutput) -> {
            outputs.add(outputFromTxOutput(txOutput));
        });

        UnsignedTransaction unsignedTx = new UnsignedTransaction(randomUid(), "BTC", inputs, outputs);


        txSignRequest = new TransactionSigningRequest(header, unsignedTx);
        return txSignRequest;
    }

    private Input inputFromTxInput(TransactionInput txInput) {
        Address inputAddress = txInput.getConnectedOutput().getScriptPubKey().getToAddress(netParams);
        DeterministicKey key = (DeterministicKey) wallet.findKeyFromAddress(inputAddress);
        log.info("Input {} has path {}",txInput.getIndex(), key.getPathAsString());
        Derivation derivation = new Derivation((long) key.getPath().get(0).num(),
                (long) key.getPath().get(1).num());
        return new Input(randomUid(),
                        txInput.getParentTransaction().getTxId().toString(),
                        (long) txInput.getIndex(),
                        inputAddress.toString(),
                        derivation,
                        txInput.getValue().value);
    }

    private static Output outputFromTxOutput(TransactionOutput txOutput) {
        Derivation_ derivation = new Derivation_(0L, 0L);
        return new Output(randomUid(),
                txOutput.getScriptPubKey().getToAddress(netParams).toString(),
                txOutput.getValue().value,
                derivation);
    }

    private static String randomUid() {
        return UUID.randomUUID().toString();
    }

    public String txToSigningReqJson(Transaction tx) {
        TransactionSigningRequest req = of(tx);
        return toJson(req);
    }

    public String toJson(TransactionSigningRequest txSignReq) {
        String json;
        try {
            json = mapper.writeValueAsString(txSignReq);

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        log.info("txSignReq json: {}", json);
        return json;
    }


}
