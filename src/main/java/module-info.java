module dev.discscout {
  requires java.net.http;
  requires jdk.httpserver;
  requires javafx.controls;
  requires javafx.web;
  requires javafx.media;
  requires com.fasterxml.jackson.databind;
  requires com.fasterxml.jackson.datatype.jsr310;
  requires com.google.zxing;
  requires com.google.zxing.javase;
  requires org.bytedeco.javacv;

  exports dev.discscout.app;
  exports dev.discscout.course;
  exports dev.discscout.domain;
  exports dev.discscout.geodesy;
  exports dev.discscout.mapping;
  exports dev.discscout.mobile;
  exports dev.discscout.physics;
  exports dev.discscout.simulation;
  exports dev.discscout.search;
  exports dev.discscout.triangulation;
  exports dev.discscout.weather;
  exports dev.discscout.video;

  opens dev.discscout.domain to com.fasterxml.jackson.databind;
  opens dev.discscout.persistence to com.fasterxml.jackson.databind;
}
