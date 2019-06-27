package com.blockchaincommons.airgap;

import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.crypto.HDPath;
import org.bitcoinj.crypto.ChildNumber;
import org.bitcoinj.script.Script;
import org.bitcoinj.wallet.KeyChainGroupStructure;

import java.util.Collections;

/**
 * KeyChainGroupStructure that supports BIP44, etc. This should be part of bitcoinj.
 */
public class BipStandardKeyChainGroupStructure implements KeyChainGroupStructure  {
    private final ChildNumber coinType;
    
    private static ChildNumber PURPOSE_BIP44 = new ChildNumber(44, true);  // P2PKH
    private static ChildNumber PURPOSE_BIP49 = new ChildNumber(49, true);  // P2WPKH-nested-in-P2SH
    private static ChildNumber PURPOSE_BIP84 = new ChildNumber(84, true);  // P2WPKH

    private static ChildNumber COINTYPE_BTC = new ChildNumber(0, true);
    private static ChildNumber COINTYPE_TBTC = new ChildNumber(1, true);
    private static ChildNumber COINTYPE_LBTC = new ChildNumber(2, true);

    public static ChildNumber CHANGE_RECEIVING = new ChildNumber(0, false);
    public static ChildNumber CHANGE_CHANGE = new ChildNumber(1, false);

    private static final HDPath BIP44_PARENT = new HDPath(true, Collections.singletonList(PURPOSE_BIP44));
    private static final HDPath BIP84_PARENT = new HDPath(true, Collections.singletonList(PURPOSE_BIP84));

    public BipStandardKeyChainGroupStructure(NetworkParameters networkParameters) {
        if (networkParameters.getId().equals(NetworkParameters.ID_MAINNET)) {
            coinType = COINTYPE_BTC;
        } else {
            coinType = COINTYPE_TBTC;
        }
    }

    @Override
    public HDPath accountPathFor(Script.ScriptType outputScriptType) {
        return accountHDPathFor(outputScriptType, 0);
    }
    
    public HDPath accountHDPathFor(Script.ScriptType outputScriptType, int accountIndex) {
        ChildNumber accountChild = new ChildNumber(accountIndex, true);
        if (outputScriptType == null || outputScriptType == Script.ScriptType.P2PKH)
            return BIP44_PARENT
                    .extend(coinType)
                    .extend(accountChild);
        else if (outputScriptType == Script.ScriptType.P2WPKH)
            return BIP84_PARENT
                    .extend(coinType)
                    .extend(accountChild);
        else
            throw new IllegalArgumentException(outputScriptType.toString());
    }

}
