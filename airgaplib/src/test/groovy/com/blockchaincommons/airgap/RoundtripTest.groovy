package com.blockchaincommons.airgap

import org.bitcoinj.core.Address
import org.bitcoinj.core.Base58
import org.bitcoinj.core.Coin
import org.bitcoinj.core.ECKey
import org.bitcoinj.core.Sha256Hash
import org.bitcoinj.core.Transaction
import org.bitcoinj.core.TransactionInput
import org.bitcoinj.core.TransactionOutput
import org.bitcoinj.crypto.TransactionSignature
import org.bitcoinj.params.TestNet3Params
import org.bitcoinj.script.Script
import org.bitcoinj.script.ScriptBuilder
import spock.lang.Ignore
import spock.lang.Specification

/**
 *
 */
class RoundtripTest extends DeterministicKeychainBaseSpec {
    static final netParams = TestNet3Params.get()
    // Panda Diary Testnet wallet, first BIP44 receiving address
    static final Address fromAddr = Address.fromString(netParams, "muuZ2RXkePUsx9Y6cWt3TCSbQyetD6nKak")
    static final String fromPrivKeyWiF = "cNt5K8XYeTVwcgdYn49tuKZtvihZhDhZJmcBPxC9MYVfhCznfz3h"
    static final ECKey fromKey = keyFromWiF(fromPrivKeyWiF)
    // 2nd receiving address
    static final Address toAddr = Address.fromString(netParams, "mzNv8DXUzkxmKR8n6597yEozDtNz1VRyar")
    // Initial funding faucet transaction
    static final byte[] initial_tx_bytes = "0100000001c1cec989779e472890b61d999875e5117e117e4309f1f90ea39dfbe64811f21f000000006a47304402207cf5f2bcd6fdd3fb5aa233e69d73d85e6d461bd328743c6d7216b0c1b869773f022078f07144d8fbb435c86ae69dfaeb8e5ed1b94662b7abddbbabbdcfb0498a28cd0121037a553015a55093c5169500c27b3080091e934794fa69705d1d0fbc8150ae1f17ffffffff019b7e5f01000000001976a9149dd7e75927da960fea20ff2e52183dd2122e698288ac00000000".decodeHex()
    static final Transaction initial_tx = new Transaction(netParams, initial_tx_bytes)
    static final byte[] change_tx_bytes = "0100000001cc652689b217db0cec03cab18a629437a0f1e308db9ee30b934b6989be50641f000000006b4830450221008f9abcda51669dc501a68d2778e2fc33f25d62d74ec791b2776733e14c39aba502201f79445d6eb4364ae83a0579ee0414abe285df034a5e42d0e15938f3f4861c91012102878641346f6ccfa4ed0a50f1786bfbd1891ff200b4c040040a804abc2c5ad69affffffff0240420f00000000001976a9145ab93563a289b74c355a9b9258b86f12bb84affb88acafe34f01000000001976a9149b1077b9d102fcc105e99a906cfd34285928b03e88ac00000000".decodeHex()
    static final Transaction change_tx = new Transaction(netParams, change_tx_bytes)
    static final Sha256Hash utxo_id = Sha256Hash.wrap("1f6450be89694b930be39edb08e3f1a03794628ab1ca03ec0cdb17b2892665cc")
    static final long utxo_index = 0
    static final byte[] utxo_script_bytes = "76a9149dd7e75927da960fea20ff2e52183dd2122e698288ac".decodeHex()
    static final Script utxo_script = new Script(utxo_script_bytes)
    static final Coin utxo_value = 0.23035547.btc
    static final Coin txAmount = 0.01.btc
    static final Address changeAddress = Address.fromString(netParams, "muerkyvAYxuDRwvodNXmjg8UFP8wFaUWB8")
    static final Coin changeAmount = 0.22012847.btc
    static final Coin changeAmount2 = 0.20990147.btc

    static final String airgapSigBase64 = "MEUCIQDi+KSPdIIVlfFP2c8AmYqu3y3Fa+Y2Mm73Z37k1Or0HAIgKTdJLWYx4ohRnrGcj16ICyNr5/OkFVPSPNEC25VubUYB"
    static final byte[] airgapSigBytes = Base64.getDecoder().decode(airgapSigBase64)
    static final String airgapSigBase64_2 = "MEQCID7PgbvuTSjL6+xgpzqT2C4r4FRgmvNP1kJrfMq1ttdAAiBTBsYXxcgrIxlDf04G1foi+GiiJRZOMN3OERn5VVe3ygE="
    static final byte[] airgapSigBytes2 = Base64.getDecoder().decode(airgapSigBase64_2)
    static final TransactionSignature airgapSig1 = TransactionSignature.decodeFromBitcoin(airgapSigBytes, true, true)
    static final TransactionSignature airgapSig2 = TransactionSignature.decodeFromBitcoin(airgapSigBytes2, true, true)



    @Ignore
    def "Roundtrip using signature from Wolf"() {
        given: "an unsigned 1-input, 2-output transaction"
        def tx = buildTestTransaction()

        when: "we use Wolf's provided signature to sign it"
        def input = tx.getInput(0)
        input.setScriptSig(ScriptBuilder.createInputScript(airgapSig1, fromKey))
        input.setWitness(null)

        then: "it verifies"
        tx.verify()

        and: "We can validate the signature on the input"
        TransactionInput curInput = tx.getInputs().get(0);
        Script scriptSig = curInput.getScriptSig();
        Script scriptPubKey = ScriptBuilder.createOutputScript(fromAddr);
        scriptSig.correctlySpends(tx, 0, scriptPubKey, Script.ALL_VERIFY_FLAGS);
    }


    @Ignore
    def "Roundtrip using bitcoinj (no wallet) on both ends"() {
        given: "an unsigned 1-input, 2-output transaction"
        def tx = buildTestTransaction()

        when: "we use a signature from bitcoinj to sign it"
        def bitcoinjSig = signWithKey(tx, fromKey)
        def input = tx.getInput(0)
        input.setScriptSig(ScriptBuilder.createInputScript(airgapSig1, fromKey))
        input.setWitness(null)

        then:
        tx.verify()

        and: "We validate the signature on the input"
        TransactionInput curInput = tx.getInputs().get(0);
        Script scriptSig = curInput.getScriptSig();
        Script scriptPubKey = ScriptBuilder.createOutputScript(fromAddr);
        scriptSig.correctlySpends(tx, 0, scriptPubKey, Script.ALL_VERIFY_FLAGS);
    }

    Transaction buildTestTransaction() {
        Transaction tx = new Transaction(netParams)
        TransactionOutput output = new TransactionOutput(netParams, null, utxo_value, utxo_script_bytes)
        //tx.addInput(output)
        tx.addInput(utxo_id, utxo_index, utxo_script)
        tx.addOutput(txAmount, toAddr)
        tx.addOutput(changeAmount, changeAddress)
        return tx
    }

    private TransactionSignature signWithKey(Transaction tx, ECKey key) {

    }


    private static keyFromWiF(String wif) {
        byte[] wifRaw = Base58.decodeChecked(wif)
        // Remove header (first byte) and checksum (4 bytes after byte 33)
        byte[] privKey = Arrays.copyOfRange(wifRaw, 1, 33)
        return ECKey.fromPrivate(privKey)
    }
}
