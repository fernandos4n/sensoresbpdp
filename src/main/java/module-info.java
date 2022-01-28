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
    requires org.jetbrains.annotations;
    requires opencsv;
    requires java.sql;
    requires jfreechart;
    requires jfreechart.fx;
    requires PanamaHitek.Arduino;
    requires jssc;
    requires software.amazon.awssdk.core;
    //requires software.amazon.awssdk.services.s3;
    requires software.amazon.awssdk.services.polly;
    requires software.amazon.awssdk.auth;
    requires software.amazon.awssdk.regions;
    requires org.knowm.xchart;
    requires de.jensd.fx.glyphs.fontawesome;
    requires log4j;

    opens mx.com.cuatronetworks.sensoresbpdp to javafx.fxml;
    exports mx.com.cuatronetworks.sensoresbpdp;
    exports mx.com.cuatronetworks.sensoresbpdp.model;
}