package com.blockchaincommons.airgap


import org.bitcoinj.core.Transaction
import org.bitcoinj.core.TransactionInput
import org.bitcoinj.crypto.TransactionSignature
import org.bitcoinj.script.ScriptBuilder
import org.bitcoinj.script.ScriptChunk
import org.bitcoinj.signers.LocalTransactionSigner
import org.bitcoinj.signers.TransactionSigner
import spock.lang.Ignore
import spock.lang.Shared

/**
 *
 */
class KeychainRoundtripTest extends DeterministicKeychainBaseSpec {
    @Shared
    UnsignedTxQrGenerator qrGenerator

    def "Roundtrip using Key Chain and LocalTransactionSigner works (spent funding utxo)"() {
        given: "an unsigned 1-input, 2-output transaction"
        def fromAddr = signingKeychain.receivingAddr(0)
        def tx = originalFundingTransaction()
        
        when: "We sign it locally"
        LocalTransactionSigner signer = new LocalTransactionSigner()
        TransactionSigner.ProposedTransaction proposedTransaction = new TransactionSigner.ProposedTransaction(tx)
        signer.signInputs(proposedTransaction, keyChainGroup)

        and: "We dump the signature to the console"
        ScriptChunk chunk = tx.getInput(0).getScriptSig().getChunks().get(0)
        println "Signature Chunk: ${chunk}"

        and: "We serialize it to QR json on the console"
        println qrGenerator.createSigningRequestString(tx)

        then: "it verifies"
        tx.verify()

        when: "We validate the signature on the input"
        correctlySpendsInput(tx, 0, fromAddr)

        then:
        noExceptionThrown()
    }

    def "Roundtrip using Key Chain and LocalTransactionSigner works (unspent first change utxo)"() {
        given: "an unsigned 1-input, 2-output transaction"
        def utxo_tx = firstChangeTransaction()
        println "change (utxo) tx: ${utxo_tx}"
        def utxo = utxo_tx.getOutput(1)
        def fromKey = signingKeychain.changeKey(0)
        def fromAddr = signingKeychain.changeAddr(0)
        def toAddr = signingKeychain.receivingAddr(1)
        def changeAddr = signingKeychain.changeAddr(1)
        def txAmount = 0.01.btc
        def changeAmount = 0.20990147.btc

        when: "we calculate the fees"
        def feeAmount = utxo.getValue().longValue() - txAmount.longValue() - changeAmount.longValue()

        then: "fees should be greater than zero (and TBD greater than dust amount)"
        feeAmount > 0 // Should actually be greater than the "dust" amount

        when: "We make a tx"
        def test_tx = buildTestTransaction(fromKey, utxo, toAddr, changeAddr, txAmount, changeAmount)
        def tx_known_good_sig = buildSignedTestTransaction(fromKey, utxo, toAddr, changeAddr, txAmount, changeAmount)

        and: "We sign it locally"
        // THIS WORKS
        TransactionSignature signature = test_tx.calculateSignature(0, fromKey, ScriptBuilder.createOutputScript(fromAddr), Transaction.SigHash.ALL, false);
        byte[] bitcoinSig = signature.encodeToBitcoin()
        String base64Sig = Base64.encoder.encodeToString(bitcoinSig)
        println "base64 sig: ${base64Sig}"

//        byte[] yyy = Base64.getDecoder().decode(base64Sig)
//        TransactionSignature ser_deser_sig = TransactionSignature.decodeFromBitcoin(yyy, true, true)

        TransactionInput input = test_tx.getInput(0)
        input.setScriptSig(ScriptBuilder.createInputScript(signature, fromKey));
        input.setWitness(null);
        // COMMENTED OUT CODE DOESN'T WORK -- I'm not sure why this signs differently than Transaction::calculateSignature above
//        LocalTransactionSigner signer = new LocalTransactionSigner()
//        TransactionSigner.ProposedTransaction proposedTransaction = new TransactionSigner.ProposedTransaction(test_tx)
//        signer.signInputs(proposedTransaction, keyChainGroup)
        println "test tx: ${test_tx}"
        println "known good tx: ${tx_known_good_sig}"


        and: "We dump the signature to the console"
        ScriptChunk chunk = test_tx.getInput(0).getScriptSig().getChunks().get(0)
        println "Signature Chunk: ${chunk}"

        and: "We serialize it to QR json on the console"
        println qrGenerator.createSigningRequestString(test_tx)

        then: "it verifies"
        test_tx.verify()

        when: "we validate the known good tx"
        correctlySpendsInput(tx_known_good_sig, 0, fromAddr)

        then:
        noExceptionThrown()

        when: "We validate the signature on the input"
        correctlySpendsInput(test_tx, 0, fromAddr)

        then:
        noExceptionThrown()
    }

    @Ignore
    def "Roundtrip using externally provided signature works (attempt 1)"() {
        given: "an unsigned 1-input, 2-output transaction"
        def fromKey = signingKeychain.receivingKey(0)
        def fromAddr = signingKeychain.receivingAddr(0)
        def tx = originalFundingTransaction()

        and: "an externally provided signature"
        TransactionSignature externalSig = RoundtripTest.airgapSig1 // Wolf's signature

        when: "we sign it with an external signature"
        def input = tx.getInput(0)
        input.setScriptSig(ScriptBuilder.createInputScript(externalSig, fromKey))
        input.setWitness(null)
        
        and: "We dump the signature to the console"
        ScriptChunk chunk = tx.getInput(0).getScriptSig().getChunks().get(0)
        println "Signature Chunk: ${chunk}"

        and: "We serialize it to QR json on the console"
        println qrGenerator.createSigningRequestString(tx)

        then: "it verifies"
        tx.verify()

        when: "We validate the signature on the input"
        correctlySpendsInput(tx, 0, fromAddr)

        then:
        noExceptionThrown()
    }

    @Ignore
    def "Roundtrip using externally provided signature works (attempt 2)"() {
        given: "an unsigned 1-input, 2-output transaction"
        def fromKey = signingKeychain.changeKey(0)
        def fromAddr = signingKeychain.changeAddr(0)
        def tx = firstChangeTransaction()

        and: "an externally provided signature"
        TransactionSignature externalSig = RoundtripTest.airgapSig2 // Wolf's signature

        when: "we sign it with an external signature"
        def input = tx.getInput(0)
        input.setScriptSig(ScriptBuilder.createInputScript(externalSig, fromKey))
        input.setWitness(null)

        and: "We dump the signature to the console"
        ScriptChunk chunk = tx.getInput(0).getScriptSig().getChunks().get(0)
        println "Signature Chunk: ${chunk}"

        and: "We serialize it to QR json on the console"
        println qrGenerator.createSigningRequestString(tx)

        then: "it verifies"
        tx.verify()

        when: "We validate the signature on the input"
        correctlySpendsInput(tx, 0, fromAddr)

        then:
        noExceptionThrown()
    }

    def setupSpec() {
        qrGenerator = new UnsignedTxQrGenerator(netParams, signingKeychain)
    }
}
