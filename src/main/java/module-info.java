module com.example.beetechdesktopapp {
    requires javafx.controls;
    requires javafx.fxml;
    requires de.jensd.fx.glyphs.fontawesome;
    requires de.jensd.fx.glyphs.commons;
    requires org.controlsfx.controls;
    requires org.json;


    opens com.example.beetechdesktopapp to javafx.fxml;
    exports com.example.beetechdesktopapp;
    exports com.example.beetechdesktopapp.Models;
    exports com.example.beetechdesktopapp.Controllers;
    exports com.example.beetechdesktopapp.Views;
    opens com.example.beetechdesktopapp.Controllers to javafx.fxml;
}