package com.blockchaincommons.airgap;

import org.bitcoinj.crypto.ChildNumber;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Immutable structure for HDPath. A class like this is being added to bitcoinj.
 */
public class HDPath extends AbstractList<ChildNumber> {
    static char PREFIX_PRIVATE = 'm';
    static char PREFIX_PUBLIC = 'M';
    static char SEPARATOR = '/';
    protected boolean hasPrivateKey;
    protected List<ChildNumber> unmodifiableList;

    public HDPath(boolean hasPrivateKey, List<ChildNumber> list) {
        this.hasPrivateKey = hasPrivateKey;
        this.unmodifiableList = Collections.unmodifiableList(list);
    }

    public HDPath(List<ChildNumber> list) {
        this(false, list);
    }
    
    static HDPath of(boolean hasPrivateKey, List<ChildNumber> list) {
        return new HDPath(hasPrivateKey, list);
    }

    static public HDPath of(List<ChildNumber> list) {
        return new HDPath(list);
    }
    
    public boolean hasPrivateKey() {
        return hasPrivateKey;
    }

    public HDPath extend(ChildNumber child) {
        List<ChildNumber> mutable = new ArrayList<>(this.unmodifiableList); // Mutable copy
        mutable.add(child);
        return new HDPath(mutable);
    }


    public HDPath extend(ChildNumber child1, ChildNumber child2) {
        List<ChildNumber> mutable = new ArrayList<>(this.unmodifiableList); // Mutable copy
        mutable.add(child1);
        mutable.add(child2);
        return new HDPath(mutable);
    }

    @Override
    public ChildNumber get(int index) {
        return unmodifiableList.get(index);
    }

    @Override
    public int size() {
        return unmodifiableList.size();
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append(hasPrivateKey ? HDPath.PREFIX_PRIVATE : HDPath.PREFIX_PUBLIC);
        for (ChildNumber segment : unmodifiableList) {
            b.append(HDPath.SEPARATOR);
            b.append(segment.toString());
        }
        return b.toString();
    }
}
