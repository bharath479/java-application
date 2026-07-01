package com.example.demo.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public static ResourceNotFoundException forProduct(Long id) {
        return new ResourceNotFoundException("Product not found with id: " + id);
    }
}
