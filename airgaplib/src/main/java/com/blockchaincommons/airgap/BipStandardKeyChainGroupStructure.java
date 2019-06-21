package com.blockchaincommons.airgap;

import com.google.common.collect.ImmutableList;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.crypto.ChildNumber;
import org.bitcoinj.script.Script;
import org.bitcoinj.wallet.KeyChainGroupStructure;

import java.util.Collections;

/**
 * KeyChainGroupStructure that supports BIP44, etc. This should be part of bitcoinj.
 */
public class BipStandardKeyChainGroupStructure implements KeyChainGroupStructure  {
    private final ChildNumber coinType;
    
    private final ImmutableList<ChildNumber> BIP44_ACCOUNT_ZERO_PATH = ImmutableList.of(new ChildNumber(44, true),
            ChildNumber.ZERO_HARDENED, ChildNumber.ZERO_HARDENED);

    private final ImmutableList<ChildNumber> BIP44_ACCOUNT_ONE_PATH = ImmutableList.of(new ChildNumber(44, true),
            ChildNumber.ZERO_HARDENED, ChildNumber.ZERO_HARDENED);

    private static ChildNumber PURPOSE_BIP44 = new ChildNumber(44, true);  // P2PKH
    private static ChildNumber PURPOSE_BIP49 = new ChildNumber(49, true);  // P2WPKH-nested-in-P2SH
    private static ChildNumber PURPOSE_BIP84 = new ChildNumber(84, true);  // P2WPKH

    private ChildNumber COINTYPE_BTC = new ChildNumber(0, true);
    private ChildNumber COINTYPE_TBTC = new ChildNumber(1, true);
    private ChildNumber COINTYPE_LBTC = new ChildNumber(2, true);

    private ChildNumber CHANGE_RECEIVING = new ChildNumber(0, false);
    private ChildNumber CHANGE_CHANGE = new ChildNumber(1, false);

    private static final HDPath BIP44_PARENT = HDPath.of(true, Collections.singletonList(PURPOSE_BIP44));
    private static final HDPath BIP84_PARENT = HDPath.of(true, Collections.singletonList(PURPOSE_BIP84));

    public BipStandardKeyChainGroupStructure(NetworkParameters networkParameters) {
        if (networkParameters.getId().equals(NetworkParameters.ID_MAINNET)) {
            coinType = COINTYPE_BTC;
        } else {
            coinType = COINTYPE_TBTC;
        }
    }

    @Override
    public ImmutableList<ChildNumber> accountPathFor(Script.ScriptType outputScriptType) {
        return ImmutableList.copyOf(accountHDPathFor(outputScriptType));
    }

    public HDPath accountHDPathFor(Script.ScriptType outputScriptType) {
        if (outputScriptType == null || outputScriptType == Script.ScriptType.P2PKH)
            return BIP44_PARENT
                    .extend(coinType)
                    .extend(ChildNumber.ZERO_HARDENED);
        else if (outputScriptType == Script.ScriptType.P2WPKH)
            return BIP84_PARENT
                    .extend(coinType)
                    .extend(ChildNumber.ZERO_HARDENED);
        else
            throw new IllegalArgumentException(outputScriptType.toString());
    }

}
