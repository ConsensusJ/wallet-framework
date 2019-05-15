/**
 *
 */
module com.blockchaincommons.airgap.fx {
    requires java.desktop;

    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.swing;

    requires com.google.zxing;
    requires com.google.zxing.javase;

    requires webcam.capture;
    requires org.slf4j;

    opens com.blockchaincommons.airgap.fx.demoapp to javafx.fxml;
    exports com.blockchaincommons.airgap.fx.demoapp;
    exports com.blockchaincommons.airgap.fx.camera;
    exports com.blockchaincommons.airgap.fx.components;
}