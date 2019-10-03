/**
 *
 */
module org.consensusj.airgap {
    requires org.bitcoinj.core;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.core;
    requires jackson.annotations;
    requires org.slf4j;

    requires com.google.common;

    exports org.consensusj.airgap;
    exports org.consensusj.airgap.json;
    exports org.consensusj.airgap.keychain;
}