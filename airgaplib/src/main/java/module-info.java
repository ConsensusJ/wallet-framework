/**
 * Java Platform Module System dependencies and exports for airgaplib.
 * Note: We may backport this library to Java 8 and eliminate this file.
 */
module org.consensusj.airgap {
    requires org.bitcoinj.core;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.core;
    requires org.slf4j;
    
    exports org.consensusj.airgap;
    exports org.consensusj.airgap.json;
    exports org.consensusj.airgap.keychain;
}