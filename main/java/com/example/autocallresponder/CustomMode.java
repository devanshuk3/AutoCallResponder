package com.example.autocallresponder;

import java.util.UUID;

public class CustomMode {
    public String id;
    public String name;
    public String message;

    public CustomMode(String name, String message) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.message = message;
    }
}
