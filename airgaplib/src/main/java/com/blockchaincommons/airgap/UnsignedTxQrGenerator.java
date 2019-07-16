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

package com.blockchaincommons.airgap;

import com.blockchaincommons.airgap.json.Derivation;
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
import org.bitcoinj.crypto.ChildNumber;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.script.Script;
import org.bitcoinj.wallet.DeterministicKeyChain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.blockchaincommons.airgap.AirGapProtocol.AssetType;

/**
 * Generates TransactionSigningRequest from bitcoinj Transaction
 */
public class UnsignedTxQrGenerator {
    private static final Logger log = LoggerFactory.getLogger(UnsignedTxQrGenerator.class);
    private final DeterministicKeyChain keyChain;
    private final NetworkParameters netParams;
    private final AirGapProtocol.AssetType assetType;
    private final ObjectMapper mapper;

    public UnsignedTxQrGenerator(NetworkParameters netParams, DeterministicKeyChain keyChain) {
        this.keyChain = keyChain;
        this.netParams = netParams;
        this.mapper = new ObjectMapper();
        assetType = netParams.getId().equals(NetworkParameters.ID_MAINNET) ? AssetType.BTC : AssetType.BTCT;
    }


    public TransactionSigningRequest createSigningRequest(Transaction transaction) {
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

        UnsignedTransaction unsignedTx = new UnsignedTransaction(randomUid(), assetType.toString(), inputs, outputs);


        txSignRequest = new TransactionSigningRequest(header, unsignedTx);
        return txSignRequest;
    }

    public String createSigningRequestString(Transaction tx) {
        TransactionSigningRequest req = createSigningRequest(tx);
        return toJson(req);
    }

    private Input inputFromTxInput(TransactionInput txInput) {
        Script scriptPubKey = txInput.getConnectedOutput().getScriptPubKey();
        Address inputAddress = scriptPubKey.getToAddress(netParams);
        byte[] pubKeyHash = scriptPubKey.getPubKeyHash();
        DeterministicKey key = keyChain.findKeyFromPubHash(pubKeyHash);
        log.info("Input {} has path {}",txInput.getIndex(), key.getPathAsString());
        Derivation derivation = derivationFromKey(key);
        long value = (txInput.getValue() != null) ? txInput.getValue().value : 0;
        return new Input(randomUid(),
                        txInput.getOutpoint().getHash().toString(),
                        txInput.getOutpoint().getIndex(),
                        inputAddress.toString(),
                        derivation,
                        value);
    }

    private Output outputFromTxOutput(TransactionOutput txOutput) {
        // Outputs aren't signed by the airgap wallet, so not sure why Derivation is used here,
        // set to zeros for now
        Derivation derivation = new Derivation(0L, 0L, null);
        return new Output(randomUid(),
                txOutput.getScriptPubKey().getToAddress(netParams).toString(),
                txOutput.getValue().value,
                derivation);
    }

    public static String randomUid() {
        return UUID.randomUUID().toString();
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

    /**
     * Generate a Derivation from a DeterministicKey
     * The current Derivation structure assumes BIP44
     * @param deterministicKey The key
     * @return Derivation structure
     */
    private Derivation derivationFromKey(DeterministicKey deterministicKey) {
        long expectedCoinType = netParams.getId().equals(NetworkParameters.ID_MAINNET) ? 0 : 1;

        List<ChildNumber> path = deterministicKey.getPath();

        long accountIndex = -1;
        long addressIndex = -1;
        Boolean change = null;
        if (path.size() >= 5) {
            long purpose  = (long) path.get(0).num();
            long coinType = (long) path.get(1).num();
            if ((purpose == 44L) && (coinType == expectedCoinType)) {
                accountIndex = (long) path.get(2).num();
                long changeIndex = (long) path.get(3).num();
                addressIndex = (long) path.get(4).num();
                change = (changeIndex == 1) ? true : null;
            } else {
                log.error("path {} is not BIP44-compatible and correct for current net params", deterministicKey.getPathAsString());
            }
        } else if (path.size() == 3) {
            // xpub subtree for watching wallet
            accountIndex = (long) path.get(0).num();
            long changeIndex = (long) path.get(1).num();
            addressIndex = (long) path.get(2).num();
            change = (changeIndex == 1) ? true : null;
        } else {
            log.error("path {} is too short to be BIP44-compatible",deterministicKey.getPathAsString());
        }
        return new Derivation(accountIndex, addressIndex, change);
    }
}
