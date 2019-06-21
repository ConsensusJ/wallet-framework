package com.blockchaincommons.airgap

import org.bitcoinj.crypto.ChildNumber
import spock.lang.Specification

/**
 *
 */
class HDPathTest extends Specification {


    def "can create simple public path"() {
        when:
        def path = HDPath.of([new ChildNumber(44, true)]);

        then:
        path.toString() == 'M/44H'
    }

    def "can create simple private path"() {
        when:
        def path = HDPath.of(true, [new ChildNumber(44, true)]);

        then:
        path.toString() == 'm/44H'
    }
}
