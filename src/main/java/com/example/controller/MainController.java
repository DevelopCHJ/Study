package com.example.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.springframework.stereotype.Component;
import com.example.service.SampleService;

@Component
public class MainController {

    private final SampleService service;

    public MainController(SampleService service) {
        this.service = service;
    }

    @FXML
    private Label label;

    @FXML
    public void initialize() {
        label.setText(service.getMessage());
    }
}