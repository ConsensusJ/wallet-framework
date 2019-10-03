/**
 *
 */
module netwalletfx {
    requires java.logging;
    requires java.desktop;

    requires javafx.graphics;
    requires javafx.controls;
    requires javafx.fxml;

    requires org.bitcoinj.walletfx;
    requires org.consensusj.supernautfx;
    requires org.consensusj.airgap.fx;
    requires org.consensusj.airgap;
    requires javax.inject;
    /*
     * Although the classes in a SupernautFX app (like this one, hopefully) use annotations
     * from javax.inject and do not import any Micronaut classes,
     * the Bean Definition classes generated by the Micronaut annotation process
     * run inside this module and do require Micronaut classes.
     */
    requires io.micronaut.inject;
    //requires jsr305;  // This is only needed for IntelliJ because IntelliJ doesn't know about the patch-module command apparently
    requires org.slf4j;
    requires org.slf4j.jul;

    requires org.bitcoinj.core;     // Automatic module

    requires org.bouncycastle.provider;
    requires com.google.common;

    requires protobuf.java;
    requires com.google.zxing;
    requires com.google.zxing.javase;
    requires fontawesomefx;
    requires webcam.capture;         // Filename-based automatic module name


    /* Export/Open the App */
    exports netwalletfx;

    opens netwalletfx to org.bitcoinj.walletfx, javafx.fxml, java.base;

}