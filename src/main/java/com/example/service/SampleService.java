package com.example.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SampleService {

    @Value("${app.name}")
    public String name;

    public String getMessage() {
        String appName = name;
        return "Spring Framework 6 + JavaFX 21 성공!["+appName+"]";
    }

}

