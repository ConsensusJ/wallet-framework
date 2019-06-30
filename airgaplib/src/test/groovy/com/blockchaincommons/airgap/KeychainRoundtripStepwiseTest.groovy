package com.blockchaincommons.airgap

import com.blockchaincommons.airgap.json.TransactionSignatureResponse
import com.blockchaincommons.airgap.json.TransactionSigningRequest
import org.bitcoinj.core.Coin
import org.bitcoinj.core.LegacyAddress
import org.bitcoinj.core.Transaction
import org.bitcoinj.core.TransactionInput
import org.bitcoinj.core.TransactionOutput
import org.bitcoinj.crypto.DeterministicKey
import org.bitcoinj.crypto.TransactionSignature
import org.bitcoinj.script.ScriptBuilder
import spock.lang.Shared
import spock.lang.Stepwise

/**
 * Spock Test Specification for the roundtrip process of signing a Bitcoin Testnet transaction.
 *
 *
 * The {code @Stepwise} annotation on this test specification means that the feature methods
 * are run in order and can communicate with each other via the {@code @Shared} variables.
 */
@Stepwise
class KeychainRoundtripStepwiseTest extends DeterministicKeychainBaseSpec  {

    @Shared UnsignedTxQrGenerator qrGenerator
    @Shared String signingRequestJsonString
    @Shared String signedResponseJsonString
    @Shared TransactionSignatureResponse response
    @Shared Transaction transaction
    @Shared DeterministicKey fromKey
    @Shared LegacyAddress fromAddr

    def "NETWORK wallet can create a transaction and serialize to signing request JSON"() {
        given: "a transaction with a UTXO in output 1"
        // This is actually the first transaction received by the
        // 0'th change address in our "panda diary" keychain.
        fromKey = signingKeychain.changeKey(0)
        fromAddr = signingKeychain.changeAddr(0)
        Transaction utxo_tx = firstChangeTransaction()
        TransactionOutput utxo = utxo_tx.getOutput(1)

        when: "we build a 1-input, 2-output (unsigned) transaction to spend the UTXO"
        LegacyAddress toAddr = signingKeychain.receivingAddr(1)
        LegacyAddress changeAddr = signingKeychain.changeAddr(1)
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
        when: "we extract the signature from the JSON"
        byte[]  signatureBytes = Base64.getDecoder().decode(response.transaction.inputSignatures[0].ecSignature)
        TransactionSignature signature = TransactionSignature.decodeFromBitcoin(signatureBytes, true, true)

        and: "we use the signature to sign the input"
        TransactionInput input = transaction.getInput(0)
        input.setScriptSig(ScriptBuilder.createInputScript(signature, fromKey))
        input.setWitness(null)
        println "Signature Chunk: ${transaction.getInput(0).getScriptSig().getChunks().get(0)}"

        then: "it verifies"
        transaction.verify()

        when: "We validate the signature on the input"
        correctlySpendsInput(transaction, 0, fromAddr)

        then: "It validates successfully"
        noExceptionThrown()
    }

    def setupSpec() {
        qrGenerator = new UnsignedTxQrGenerator(netParams, signingKeychain)
    }
}
