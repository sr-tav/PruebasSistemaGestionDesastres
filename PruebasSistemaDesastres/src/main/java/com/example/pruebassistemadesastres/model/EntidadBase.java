package com.example.pruebassistemadesastres.model;

import java.util.UUID;

public class EntidadBase {
    protected final String id;

    protected EntidadBase() {
        this.id = UUID.randomUUID().toString();
    }
    public String getId() { return id; }
}
