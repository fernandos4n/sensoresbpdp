module mx.com.cuatronetworks.sensoresbpdp {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires jlayer;
    requires java.desktop;
    requires aws.java.sdk.core;
    requires org.jetbrains.annotations;
    requires aws.java.sdk.polly;
    requires opencsv;
    requires java.sql;

    opens mx.com.cuatronetworks.sensoresbpdp to javafx.fxml;
    exports mx.com.cuatronetworks.sensoresbpdp;
    exports mx.com.cuatronetworks.sensoresbpdp.model;
}