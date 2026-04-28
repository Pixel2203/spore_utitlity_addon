package com.example.errors;

public class BlockLimitExceededException extends Exception {
    public BlockLimitExceededException(String message) {
        super(message);
    }
}
