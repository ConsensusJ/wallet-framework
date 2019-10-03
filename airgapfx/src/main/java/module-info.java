/**
 *
 */
module org.consensusj.airgap.fx {
    requires java.desktop;

    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.swing;

    requires com.google.zxing;
    requires com.google.zxing.javase;

    requires webcam.capture;
    requires org.slf4j;

    opens org.consensusj.airgap.fx.demoapp to javafx.fxml;
    exports org.consensusj.airgap.fx.demoapp;
    exports org.consensusj.airgap.fx.camera;
    exports org.consensusj.airgap.fx.components;
}