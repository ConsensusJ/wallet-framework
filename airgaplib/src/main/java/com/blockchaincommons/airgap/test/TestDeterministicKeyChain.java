package com.blockchaincommons.airgap.test;

import com.blockchaincommons.airgap.BipStandardKeyChainGroupStructure;
import com.blockchaincommons.airgap.HDPath;
import com.google.common.collect.ImmutableList;
import org.bitcoinj.core.LegacyAddress;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.crypto.ChildNumber;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.script.Script;
import org.bitcoinj.wallet.DeterministicKeyChain;
import org.bitcoinj.wallet.DeterministicSeed;
import org.bitcoinj.wallet.UnreadableWalletException;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

/**
 * Test Seed and defining constants for integration testing, etc.
 */
public class TestDeterministicKeyChain {
    static final String mnemonicString = "panda diary marriage suffer basic glare surge auto scissors describe sell unique";
    static final Instant creationInstant = LocalDate.of(2019, 4, 10).atStartOfDay().toInstant(ZoneOffset.UTC);
//    static final long creationTimeSeconds = creationInstant.getEpochSecond();
//    public static final DeterministicSeed seed;
//    private static final DeterministicKeyChain keyChain;
    public static final BipStandardKeyChainGroupStructure bip44KeyChainGroupStructure = new BipStandardKeyChainGroupStructure(TestNet3Params.get());
    public static final HDPath BIP44_PATH_TBTC_ACCOUNT_ZERO = bip44KeyChainGroupStructure.accountHDPathFor(Script.ScriptType.P2PKH);
    static final HDPath pathReceiving = BIP44_PATH_TBTC_ACCOUNT_ZERO.extend(ChildNumber.ZERO);  // 44' / 1' / 0' / 0
    static final HDPath pathChange = BIP44_PATH_TBTC_ACCOUNT_ZERO.extend(ChildNumber.ONE);      // 44' / 1' / 0' / 1

    private final NetworkParameters netParams = TestNet3Params.get();
    private final DeterministicSeed deterministicSeed;
    public final DeterministicKeyChain keyChain;

//    static {
//        DeterministicSeed tempSeed;
//        try {
//            tempSeed = new DeterministicSeed(mnemonicString, null, "", creationTimeSeconds);
//        } catch (UnreadableWalletException e) {
//            tempSeed = null;
//        }
//        seed = tempSeed;
//        keyChain = TestDeterministicKeyChain.keyChainForPath(BIP44_PATH_TBTC_ACCOUNT_ZERO);
//    }

    public TestDeterministicKeyChain() {
        this(mnemonicString, creationInstant);
    }
    
    public TestDeterministicKeyChain(String mnemonicString, Instant creationInstant) {
        long creationTimeSeconds = creationInstant.getEpochSecond();
        try {
            deterministicSeed = new DeterministicSeed(mnemonicString, null, "", creationTimeSeconds);
        } catch (UnreadableWalletException e) {
            throw new RuntimeException(e);
        }
        keyChain = TestDeterministicKeyChain.keyChainForPath(deterministicSeed, BIP44_PATH_TBTC_ACCOUNT_ZERO);
    }

    public LegacyAddress receivingAddr(int index) {
        HDPath indexPath = pathReceiving.extend(new ChildNumber(index));
        return address(indexPath);
    }

    public DeterministicKey receivingKey(int index) {
        return key(pathReceiving.extend(new ChildNumber(index)));
    }

    public LegacyAddress changeAddr(int index) {
        HDPath indexPath = pathChange.extend(new ChildNumber(index));
        return address(indexPath);
    }

    public DeterministicKey changeKey(int index) {
        return key(pathChange.extend(new ChildNumber(index)));
    }

    public DeterministicKey key(HDPath indexPath) {
        return keyChain.getKeyByPath(indexPath,true);
    }

    public LegacyAddress address(HDPath indexPath) {
        DeterministicKey key = key(indexPath);
        return LegacyAddress.fromKey(netParams, key);
    }
    
    /**
     * Hardcoded for our test seed, perhaps should take seed as parameter
     * @param accountPath
     * @return
     */
    private static DeterministicKeyChain keyChainForPath(DeterministicSeed seed, HDPath accountPath) {
        DeterministicKeyChain chain = DeterministicKeyChain.builder()
                    .seed(seed)
                    .outputScriptType(Script.ScriptType.P2PKH)
                    .accountPath(ImmutableList.copyOf(accountPath))
                    .build();
        return chain;
    }

}
