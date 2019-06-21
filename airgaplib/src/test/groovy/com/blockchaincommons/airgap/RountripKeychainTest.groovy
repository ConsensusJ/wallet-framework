package com.blockchaincommons.airgap

import com.blockchaincommons.airgap.test.TestDeterministicKeyChain
import org.bitcoinj.core.Address
import org.bitcoinj.core.Coin
import org.bitcoinj.core.ECKey
import org.bitcoinj.core.LegacyAddress
import org.bitcoinj.core.NetworkParameters
import org.bitcoinj.core.Transaction
import org.bitcoinj.core.TransactionOutput
import org.bitcoinj.crypto.TransactionSignature
import org.bitcoinj.params.TestNet3Params
import org.bitcoinj.script.Script
import org.bitcoinj.script.ScriptBuilder
import org.bitcoinj.script.ScriptChunk
import org.bitcoinj.signers.LocalTransactionSigner
import org.bitcoinj.signers.TransactionSigner
import org.bitcoinj.wallet.DeterministicKeyChain
import org.bitcoinj.wallet.KeyChain
import org.bitcoinj.wallet.KeyChainGroup
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification

/**
 *
 */
class RountripKeychainTest extends Specification {
    //static final LegacyAddress toAddr = LegacyAddress.fromString(null, "mzNv8DXUzkxmKR8n6597yEozDtNz1VRyar")
    private static final NetworkParameters netParams = TestNet3Params.get()

    @Shared
    TestDeterministicKeyChain testDeterministicKeyChain
    
    @Shared
    DeterministicKeyChain keyChain

    @Shared
    KeyChainGroup keyChainGroup

    @Shared
    UnsignedTxQrGenerator qrGenerator

    def "Roundtrip using Key Chain and LocalTransactionSigner works (spent funding utxo)"() {
        given: "an unsigned 1-input, 2-output transaction"
        def fromAddr = testDeterministicKeyChain.receivingAddr(0)
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
        input1CorrectlySpends(tx, fromAddr)

        then:
        noExceptionThrown()
    }

    def "Roundtrip using Key Chain and LocalTransactionSigner works (unspent first change utxo)"() {
        given: "an unsigned 1-input, 2-output transaction"
        def fromAddr = testDeterministicKeyChain.changeAddr(0)
        def tx = firstChangeTransaction()

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
        input1CorrectlySpends(tx, fromAddr)

        then:
        noExceptionThrown()
    }

    @Ignore
    def "Roundtrip using externally provided signature works (attempt 1)"() {
        given: "an unsigned 1-input, 2-output transaction"
        def fromKey = testDeterministicKeyChain.receivingKey(0)
        def fromAddr = testDeterministicKeyChain.receivingAddr(0)
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
        input1CorrectlySpends(tx, fromAddr)

        then:
        noExceptionThrown()
    }

    @Ignore
    def "Roundtrip using externally provided signature works (attempt 2)"() {
        given: "an unsigned 1-input, 2-output transaction"
        def fromKey = testDeterministicKeyChain.changeKey(0)
        def fromAddr = testDeterministicKeyChain.changeAddr(0)
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
        input1CorrectlySpends(tx, fromAddr)

        then:
        noExceptionThrown()
    }

    def setupSpec() {
        testDeterministicKeyChain = new TestDeterministicKeyChain();
        keyChain = testDeterministicKeyChain.keyChain;
        // We need to create some leaf keys in the HD keychain so that they can be found for verifying transactions
        keyChain.getKey(KeyChain.KeyPurpose.RECEIVE_FUNDS)  // Generate 0'th receiving address
        keyChain.getKey(KeyChain.KeyPurpose.CHANGE)         // Generate 0'th change address
        qrGenerator = new UnsignedTxQrGenerator(netParams, keyChain)
        keyChainGroup = KeyChainGroup
                .builder(netParams)
                .addChain(keyChain)
                .build()
    }

    private static void input1CorrectlySpends(Transaction tx, Address fromAddr) {
        Script scriptSig = tx.getInputs().get(0).getScriptSig();
        Script scriptPubKey = ScriptBuilder.createOutputScript(fromAddr);
        scriptSig.correctlySpends(tx, 0, scriptPubKey, Script.ALL_VERIFY_FLAGS)
    }

    private Transaction originalFundingTransaction() {
        ECKey fromKey = testDeterministicKeyChain.receivingKey(0)
        TransactionOutput utxo = RoundtripTest.initial_tx.getOutput(0)
        LegacyAddress toAddr = testDeterministicKeyChain.receivingAddr(1)
        Coin toAmount = 0.01.btc
        Coin changeAmount = RoundtripTest.changeAmount
        LegacyAddress changeAddr = testDeterministicKeyChain.changeAddr(0)
        def tx = buildTestTransaction(fromKey, utxo, toAddr, changeAddr, toAmount, changeAmount)
        return tx
    }

    private Transaction firstChangeTransaction() {
        ECKey fromKey = testDeterministicKeyChain.changeKey(0)
        TransactionOutput utxo = RoundtripTest.change_tx.getOutput(1)
        LegacyAddress toAddr = testDeterministicKeyChain.receivingAddr(1)
        Coin toAmount = 0.01.btc
        Coin changeAmount = RoundtripTest.changeAmount2
        LegacyAddress changeAddr = testDeterministicKeyChain.changeAddr(1)
        def tx = buildTestTransaction(fromKey, utxo, toAddr, changeAddr, toAmount, changeAmount)
        return tx
    }

    /**
     * Build an unsigned proposed transaction with one UTXO input, one destination address, and a change address.
     *
     * @param fromKey The ECKey (public only is ok) needed to build the scriptSig
     * @param fromOutput UTXO to spend
     * @param toAddress destination address
     * @param changeAddress change address
     * @param toAmount amount to send to destination
     * @param changeAmount amount to send to change address
     * @return A ready-to-sign transaction with OP_0 in the signature slot
     */
    static Transaction buildTestTransaction(ECKey fromKey, TransactionOutput fromOutput, LegacyAddress toAddress, LegacyAddress changeAddress, Coin toAmount, Coin changeAmount) {
        Transaction tx = new Transaction(toAddress.getParameters())
        tx.addInput(fromOutput)
        tx.addOutput(toAmount, toAddress)
        tx.addOutput(changeAmount, changeAddress)
        // Create ScriptSig with dummy (OP_0) signature
        tx.getInput(0).setScriptSig(ScriptBuilder.createInputScript(null, fromKey))
        return tx
    }
}
