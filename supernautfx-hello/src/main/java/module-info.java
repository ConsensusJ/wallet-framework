/**
 *
 */
module org.consensusj.supernautfx.hello {
    requires org.consensusj.supernautfx;
    requires javafx.controls;
    requires javafx.fxml;

    requires org.slf4j;
    //requires org.slf4j.jul;
    requires javax.inject;


    opens org.consensusj.supernautfx.sample.hello to javafx.fxml;
    exports org.consensusj.supernautfx.sample.hello;
}