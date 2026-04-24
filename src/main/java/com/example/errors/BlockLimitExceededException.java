package com.example.errors;

public class BlockLimitExceededException extends RuntimeException {
    public BlockLimitExceededException(String message) {
        super(message);
    }
}
