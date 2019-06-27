package com.blockchaincommons.airgap.keychain;

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

/**
 * Deterministic Keychain for BIP44 and related BIPs
 * <p>
 * This class really has two things we need long-term: (1) A constructor that builds the keychain
 * using the correct paths -- this could be a factory method or added to the existing builder, and
 * (2) useful methods to get keys and addresses -- functionality could/should also work with BIP 32
 */
public class BipStandardDeterministicKeyChain extends DeterministicKeyChain {
    private static final BipStandardKeyChainGroupStructure bip44KeyChainGroupStructure = new BipStandardKeyChainGroupStructure(TestNet3Params.get());

    private final NetworkParameters netParams = TestNet3Params.get();
    private final HDPath pathAccount;
    private final HDPath pathReceiving;
    private final HDPath pathChange;


    /**
     * Constructor for a BIP44-family compliant DeterministicKeyChain
     * TODO: Add coin type parameter to constructor
     * @param seed Seed to use
     * @param outputScriptType script type for determining the purpose child
     * @param accountIndex account index to use for the account child
     */
    public BipStandardDeterministicKeyChain(DeterministicSeed seed, Script.ScriptType outputScriptType, int accountIndex) {
        super(seed, null, outputScriptType, ImmutableList.copyOf(bip44KeyChainGroupStructure.accountHDPathFor(outputScriptType, accountIndex)));
        pathAccount = bip44KeyChainGroupStructure.accountHDPathFor(outputScriptType, accountIndex);
        pathReceiving = pathAccount.extend(BipStandardKeyChainGroupStructure.CHANGE_RECEIVING);
        pathChange = pathAccount.extend(BipStandardKeyChainGroupStructure.CHANGE_CHANGE);
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
        return getKeyByPath(indexPath,true);
    }

    public LegacyAddress address(HDPath indexPath) {
        DeterministicKey key = key(indexPath);
        return LegacyAddress.fromKey(netParams, key);
    }
}
