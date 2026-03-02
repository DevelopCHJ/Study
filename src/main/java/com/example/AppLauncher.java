package com.example;

import javafx.application.Application;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import com.example.config.AppConfig;

public class AppLauncher {

    public static void main(String[] args) {
        Application.launch(MainApp.class, args);
    }
}