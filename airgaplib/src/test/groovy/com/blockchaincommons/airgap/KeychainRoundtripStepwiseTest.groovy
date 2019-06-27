package com.blockchaincommons.airgap

import com.blockchaincommons.airgap.json.TransactionSignatureResponse
import com.blockchaincommons.airgap.json.TransactionSigningRequest
import org.bitcoinj.core.LegacyAddress
import org.bitcoinj.core.Transaction
import org.bitcoinj.core.TransactionInput
import org.bitcoinj.crypto.DeterministicKey
import org.bitcoinj.crypto.TransactionSignature
import org.bitcoinj.script.ScriptBuilder
import org.bitcoinj.script.ScriptChunk
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Stepwise

/**
 * From Transaction to request JSON to signed JSON to validation
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

    def "Can create a transaction and serialize to signing request JSON"() {
        given: "a transaction with a UTXO in output 1"
        fromKey = keyChain.changeKey(0)
        fromAddr = keyChain.changeAddr(0)
        def utxo_tx = firstChangeTransaction()
        def utxo = utxo_tx.getOutput(1)

        when: "we build a 1-input, 2-output transaction to spend it"
        def toAddr = keyChain.receivingAddr(1)
        def changeAddr = keyChain.changeAddr(1)
        def txAmount = 0.01.btc
        def changeAmount = 0.20990147.btc

        and: "we calculate the fees"
        def feeAmount = utxo.getValue().longValue() - txAmount.longValue() - changeAmount.longValue()

        then: "fees should be greater than zero (and TBD greater than dust amount)"
        feeAmount > 0 // Should actually be greater than the "dust" amount

        when: "We make an unsigned tx"
        transaction = buildTestTransaction(fromKey, utxo, toAddr, changeAddr, txAmount, changeAmount)

        and: "We serialize it QR json"
        signingRequestJsonString =  qrGenerator.createSigningRequestString(transaction)
        println "Signing request JSON: ${signingRequestJsonString}"

        then: "json looks correct (TODO: json IS correct)"
        signingRequestJsonString != null
        signingRequestJsonString.length() > 10
        // TODO: More checks
    }

    def "Can use the signing request JSON to generate a signed response"() {
        // Currently this functionality is iOS only, so we just use hardcoded response
        when: "we parse the signing request JSON"
        // TODO: Scan the JSON
        // TransactionSigningRequest signingRequest = t.b.d
        signedResponseJsonString = expectedSignedResponseJsonString;

        then: "it looks reasonable"
        // TODO: Add tests
    }

    def "Can parse the Signed Tx JSON"() {
        given:
        def parser = new SignedResponseParser()

        when: "We parse the JSON"
        response = parser.parse(signedResponseJsonString)

        then: "It deserializes correctly"
        response != null
        response.header.version == 1
        response.header.format == 'AirgappedSigning'
        response.transaction.asset == AirGapProtocol.AssetType.BTCT.toString()
        response.transaction.inputSignatures[0].uid == 'B7DCFE03-96AB-4C0E-92F5-D08FF0BE8266'
        response.transaction.inputSignatures[0].ecPublicKey == 'Ai4CLThM5uOcmx+RQ/AbJLPBmgUtat1IaW532K1FVdaC'
        response.transaction.inputSignatures[0].ecSignature == 'MEQCIEV/hwycaBojR/eHFgg6SaTeHg9Djot6CCP9hhzIpJGmAiBBEUA1r60GUTLiXhib5V5uDnF6ATWi5ZblCP0Yzhz+AQE='
    }

    def "Can sign and verify a transaction using the signature from the JSON"() {
        given:
        //byte[]  signatureBytes = Base64.getDecoder().decode(response.transaction.inputSignatures[0].ecSignature)
        byte[]  signatureBytes = Base64.getDecoder().decode('MEUCIQCE7vpwYqGvmpXpYkDjkfQX9hQ1rUMBAwOUBwZScj69vAIgIv342xyyhT53qn0rRC5CU0vosvFkTegwNH3jUZkAo1IB')
        TransactionSignature signature = TransactionSignature.decodeFromBitcoin(signatureBytes, true, true)

        when: "we sign it with an external signature"
        def input = transaction.getInput(0)
        input.setScriptSig(ScriptBuilder.createInputScript(signature, fromKey))
        input.setWitness(null)
        println "Signature Chunk: ${transaction.getInput(0).getScriptSig().getChunks().get(0)}"

        then: "it verifies"
        transaction.verify()

        when: "We validate the signature on the input"
        correctlySpendsInput(transaction, 0, fromAddr)

        then:
        noExceptionThrown()

    }

    def setupSpec() {
        qrGenerator = new UnsignedTxQrGenerator(netParams, keyChain)
    }

    static final expectedSignedResponseJsonString = '''
{
  "transaction": {
    "uid": "3C11F205-07C2-4484-8C45-EFF75FEDB65A",
    "asset": "BTCT",
    "inputSignatures": [
      {
        "uid": "B7DCFE03-96AB-4C0E-92F5-D08FF0BE8266",
        "ecPublicKey": "Ai4CLThM5uOcmx+RQ\\/AbJLPBmgUtat1IaW532K1FVdaC",
        "ecSignature": "MEQCIEV\\/hwycaBojR\\/eHFgg6SaTeHg9Djot6CCP9hhzIpJGmAiBBEUA1r60GUTLiXhib5V5uDnF6ATWi5ZblCP0Yzhz+AQE="
      }
    ]
  },
  "header": {
    "version": 1,
    "format": "AirgappedSigning"
  }
}
'''
}
