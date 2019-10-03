package org.consensusj.airgap


import org.bitcoinj.core.Coin
import org.bitcoinj.core.LegacyAddress
import org.bitcoinj.core.Transaction
import org.bitcoinj.core.TransactionOutput
import org.bitcoinj.crypto.ChildNumber
import org.bitcoinj.crypto.DeterministicKey
import org.bitcoinj.crypto.HDPath
import org.bitcoinj.script.Script
import org.bitcoinj.wallet.DeterministicKeyChain
import org.consensusj.airgap.json.TransactionSignatureResponse
import org.consensusj.airgap.json.TransactionSigningRequest
import spock.lang.Shared
import spock.lang.Stepwise

import java.time.Instant

/**
 * Spock Test Specification for the roundtrip process of signing a Bitcoin Testnet transaction.
 *
 *
 * The {code @Stepwise} annotation on this test specification means that the feature methods
 * are run in order and can communicate with each other via the {@code @Shared} variables.
 */
@Stepwise
class KeychainRoundtripStepwiseTest extends DeterministicKeychainBaseSpec  {
    // Account path for the network wallet
    static final HDPath networkAccountPath = HDPath.of(ChildNumber.ZERO_HARDENED)
    // Relative paths to keys used in tests
    static final HDPath fromKeyPath = HDPath.of(ChildNumber.ONE, ChildNumber.ZERO)
    static final HDPath toKeyPath = HDPath.of(ChildNumber.ZERO, ChildNumber.ONE)
    static final HDPath changeKeyPath = HDPath.of(ChildNumber.ONE, ChildNumber.ONE)

    @Shared UnsignedTxQrGenerator qrGenerator
    @Shared String xpub
    @Shared Instant xpubCreationInstant
    @Shared DeterministicKeyChain networkKeyChain
    @Shared String signingRequestJsonString
    @Shared String signedResponseJsonString
    @Shared TransactionSignatureResponse response
    @Shared Transaction transaction
    @Shared DeterministicKey fromKey
    @Shared LegacyAddress fromAddr

    def "Can create an xpub string from signing keychain"() {
        expect: "Test setup provides a DeterministicKeyChain initialized with the Panda Diary seed"
        signingKeychain != null

        when:
        def watchingKey = signingKeychain.getWatchingKey()
        println "watching key = ${watchingKey}"
        println "watching key path = ${watchingKey.pathAsString}"
        xpub = watchingKey.serializePubB58(netParams,  Script.ScriptType.P2PKH)
        xpubCreationInstant = Instant.ofEpochSecond(watchingKey.creationTimeSeconds)
        println "xpub = ${xpub}"
        println "xpub creation time = ${xpubCreationInstant}"

        then:
        xpub.length() > 0
        xpub == "tpubDDpSwdfCsfnYP8SH7YZvu1LK3BUMr3RQruCKTkKdtnHy2iBNJWn1CYvLwgskZxVNBV4KhicZ4FfgFCGjTwo4ATqdwoQcb5UjJ6ejaey5Ff8"
    }

    def "Can create a network keychain from the xpub"() {
        when: "we create a network keychain from the xpub"
        DeterministicKey key = DeterministicKey.deserializeB58(xpub, netParams)
        key.creationTimeSeconds = xpubCreationInstant.epochSecond
        println "Deserialized xpub key: ${key}"
        println "Deserialized xpub creation time: ${Instant.ofEpochSecond(key.creationTimeSeconds)}"
        // The below will create a wallet, but we're not using Wallets in this test spec
        //Wallet networkWallet = Wallet.fromWatchingKey(netParams, key, Script.ScriptType.P2PKH)
        networkKeyChain = DeterministicKeyChain.builder().watch(key).outputScriptType(outputScriptType).build()

        and: "we fetch the keys that are used in later steps"
        DeterministicKey fromKey = networkKeyChain.getKeyByPath(HDPath.of(networkAccountPath).extend(fromKeyPath), true)
        DeterministicKey toKey = networkKeyChain.getKeyByPath(HDPath.of(networkAccountPath).extend(toKeyPath), true)
        DeterministicKey changeKey = networkKeyChain.getKeyByPath(HDPath.of(networkAccountPath).extend(changeKeyPath), true)

        then: "the pubkeys in the network keychain match the pubkeys in the signing keychain"
        networkKeyChain != null
        networkKeyChain.isWatching()
        fromKey.getPubKey() == signingKeychain.getKeyByPath(HDPath.of(signingAccountPath).extend(fromKeyPath), false).getPubKey()
        toKey.getPubKey() == signingKeychain.getKeyByPath(HDPath.of(signingAccountPath).extend(toKeyPath), false).getPubKey()
        changeKey.getPubKey() == signingKeychain.getKeyByPath(HDPath.of(signingAccountPath).extend(changeKeyPath), false).getPubKey()
    }

    def "NETWORK wallet can create a transaction and serialize a JSON signing request "() {
        given: "a transaction with a UTXO in output 1"
        // This is actually the first transaction received by the
        // 0'th change address in our "panda diary" keychain.
        fromKey = networkKeyChain.getKeyByPath(HDPath.of(networkAccountPath).extend(fromKeyPath), false)
        fromAddr = addressFromKey(fromKey)
        Transaction utxo_tx = firstChangeTransaction()
        TransactionOutput utxo = utxo_tx.getOutput(1)

        when: "we build a 1-input, 2-output (unsigned) transaction to spend the UTXO"
        LegacyAddress toAddr = addressFromKey(networkKeyChain.getKeyByPath(HDPath.of(networkAccountPath).extend(toKeyPath), false))
        LegacyAddress changeAddr = addressFromKey(networkKeyChain.getKeyByPath(HDPath.of(networkAccountPath).extend(changeKeyPath), false))
        Coin txAmount = 0.01.btc
        Coin changeAmount = 0.20990147.btc
        transaction = buildTestTransaction(fromKey, utxo, toAddr, changeAddr, txAmount, changeAmount)

        and: "We serialize it to transaction signing request JSON"
        signingRequestJsonString =  qrGenerator.createSigningRequestString(transaction)
        println "Signing request JSON: ${signingRequestJsonString}"

        then: "json looks correct"
        signingRequestJsonString != null
        signingRequestJsonString.length() > 10
        // TODO: More checks
    }

    def "AIRGAP wallet can use the signing request JSON to generate a signed response"() {
        given: "An Airgap transaction signer object "
        // (same basic functionality as an airgap device/wallet)
        AirGapTransactionSigner signer = new AirGapTransactionSigner(signingKeychain)

        when: "we parse the signing request JSON and create the transaction signature response JSON"
        TransactionSigningRequest request = signer.parseSigningRequestJson(signingRequestJsonString)
        TransactionSignatureResponse response = signer.signatureResponseFromSigningRequest(request)
        signedResponseJsonString = signer.serializeResponse(response)
        println "Signed response JSON: ${signedResponseJsonString}"

        then: "the response object looks correct"
        response.transaction.inputSignatures.size() == 1
        // TODO: More checks
    }

    def "NETWORK wallet can parse the Signed Tx JSON"() {
        given: "A parser that can parse the transaction response JSON"
        def parser = new SignedResponseParser()

        when: "We parse the JSON"
        response = parser.parse(signedResponseJsonString)

        then: "It deserializes correctly"
        response != null
        response.header.version == 1
        response.header.format == 'AirgappedSigning'
        response.transaction.asset == AirGapProtocol.AssetType.BTCT.toString()
        response.transaction.inputSignatures[0].uid.size() == 'B7DCFE03-96AB-4C0E-92F5-D08FF0BE8266'.size()  // Can't check data itself, it's random
        response.transaction.inputSignatures[0].ecPublicKey == 'Ai4CLThM5uOcmx+RQ/AbJLPBmgUtat1IaW532K1FVdaC'
        response.transaction.inputSignatures[0].ecSignature == 'MEUCIQCE7vpwYqGvmpXpYkDjkfQX9hQ1rUMBAwOUBwZScj69vAIgIv342xyyhT53qn0rRC5CU0vosvFkTegwNH3jUZkAo1IB'
    }

    def "NETWORK wallet can sign and verify a transaction using the signature from the JSON"() {
        given:
        def responseHandler = new SignedResponseHandler()

        when: "we use the signature and pubKey from the response to sign the input"
        responseHandler.signWithResponse(transaction, response)
        
        then: "it verifies"
        transaction.verify()

        when: "We validate the signature on the input"
        SignedResponseHandler.correctlySpendsInput(transaction, 0, fromAddr)

        then: "It validates successfully"
        noExceptionThrown()
    }

    def setupSpec() {
        qrGenerator = new UnsignedTxQrGenerator(netParams, signingKeychain)
    }
}
