package com.blockchaincommons.airgap

import com.blockchaincommons.airgap.keychain.BipStandardDeterministicKeyChain
import org.bitcoinj.core.Address
import org.bitcoinj.core.Coin
import org.bitcoinj.core.ECKey
import org.bitcoinj.core.LegacyAddress
import org.bitcoinj.core.NetworkParameters
import org.bitcoinj.core.Transaction
import org.bitcoinj.core.TransactionOutPoint
import org.bitcoinj.core.TransactionOutput
import org.bitcoinj.crypto.DeterministicKey
import org.bitcoinj.crypto.HDPath
import org.bitcoinj.params.TestNet3Params
import org.bitcoinj.script.Script
import org.bitcoinj.script.ScriptBuilder
import org.bitcoinj.script.ScriptException
import org.bitcoinj.wallet.DeterministicSeed
import org.bitcoinj.wallet.KeyChain
import org.bitcoinj.wallet.KeyChainGroup
import spock.lang.Shared
import spock.lang.Specification

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

/**
 * Base Specification for testing with a DeterministicKeyChain
 */
abstract class DeterministicKeychainBaseSpec extends Specification {
    public static final String mnemonicString = "panda diary marriage suffer basic glare surge auto scissors describe sell unique"
    public static final Instant creationInstant = LocalDate.of(2019, 4, 10).atStartOfDay().toInstant(ZoneOffset.UTC)
    public static final BipStandardKeyChainGroupStructure bip44KeyChainGroupStructure = new BipStandardKeyChainGroupStructure(TestNet3Params.get())
    public static final int signingAccountIndex = 0
    public static final Script.ScriptType outputScriptType = Script.ScriptType.P2PKH
    public static final HDPath signingAccountPath = bip44KeyChainGroupStructure.accountHDPathFor(outputScriptType, signingAccountIndex)
    protected static final NetworkParameters netParams = TestNet3Params.get()
    
    @Shared
    BipStandardDeterministicKeyChain signingKeychain

    @Shared
    KeyChainGroup keyChainGroup

    def setupSpec() {
        DeterministicSeed seed =  new DeterministicSeed(mnemonicString, null, "", creationInstant.getEpochSecond())
        signingKeychain = new BipStandardDeterministicKeyChain(seed, outputScriptType, signingAccountIndex);
        // We need to create some leaf keys in the HD keychain so that they can be found for verifying transactions
        signingKeychain.getKeys(KeyChain.KeyPurpose.RECEIVE_FUNDS, 2)  // Generate first 2 receiving address
        signingKeychain.getKeys(KeyChain.KeyPurpose.CHANGE, 2)         // Generate first 2 change address
        keyChainGroup = KeyChainGroup
                .builder(netParams)
                .addChain(signingKeychain)
                .build()
    }

    /**
     * Verify that a transaction correctly spends the input specified by index. Throws {@link ScriptException}
     * if verification fails.
     *
     * @param tx The transaction to verify
     * @param inputIndex The input to verify
     * @param fromAddr The address we are trying to spend funds from
     * @throws ScriptException If {@code scriptSig#correctlySpends} fails with exception
     * @deprecated Use {@link SignedResponseHandler#correctlySpendsInput}
     */
    @Deprecated
    protected static void correctlySpendsInput(Transaction tx, int inputIndex, Address fromAddr) throws ScriptException {
        SignedResponseHandler.correctlySpendsInput(tx, inputIndex, fromAddr);
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

    static Transaction buildSignedTestTransaction(ECKey fromKey, TransactionOutput fromOutput, LegacyAddress toAddress, LegacyAddress changeAddress, Coin toAmount, Coin changeAmount) {
        LegacyAddress fromAddress = LegacyAddress.fromKey(toAddress.getParameters(), fromKey)
        Transaction tx = new Transaction(toAddress.getParameters())
        tx.addOutput(toAmount, toAddress)
        tx.addOutput(changeAmount, changeAddress)
        TransactionOutPoint outPoint = fromOutput.getOutPointFor();
        tx.addSignedInput(outPoint, ScriptBuilder.createOutputScript(fromAddress), fromKey);
        return tx
    }

    protected Transaction originalFundingTransaction() {
        ECKey fromKey = signingKeychain.receivingKey(0)
        TransactionOutput utxo = RoundtripTest.initial_tx.getOutput(0)
        LegacyAddress toAddr = signingKeychain.receivingAddr(1)
        Coin toAmount = 0.01.btc
        Coin changeAmount = RoundtripTest.changeAmount
        LegacyAddress changeAddr = signingKeychain.changeAddr(0)
        def tx = buildTestTransaction(fromKey, utxo, toAddr, changeAddr, toAmount, changeAmount)
        return tx
    }

//    protected Transaction firstChangeTransaction() {
//        ECKey fromKey = keyChain.changeKey(0)
//        TransactionOutput utxo = RoundtripTest.change_tx.getOutput(1)
//        LegacyAddress toAddr = keyChain.receivingAddr(0)
//        Coin toAmount = 0.01.btc
//        Coin changeAmount = RoundtripTest.changeAmount2
//        LegacyAddress changeAddr = keyChain.changeAddr(1)
//        def tx = buildTestTransaction(fromKey, utxo, toAddr, changeAddr, toAmount, changeAmount)
//        return tx
//    }
    // change_tx
    
    protected Transaction firstChangeTransaction() {
        return RoundtripTest.change_tx
    }

    static LegacyAddress addressFromKey(DeterministicKey key) {
        return LegacyAddress.fromKey(netParams, key);
    }
}
