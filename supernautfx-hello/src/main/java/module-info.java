/**
 *
 */
module org.consensusj.supernautfx.hello {
    requires org.consensusj.supernautfx;
    requires javafx.graphics;
    requires javafx.controls;

    requires org.slf4j;
    requires org.slf4j.jul;
    requires javax.inject;
    requires io.micronaut.inject;

    opens org.consensusj.supernautfx.sample.hello to javafx.graphics;
    exports org.consensusj.supernautfx.sample.hello;
    exports org.consensusj.supernautfx.sample.hello.service;
}