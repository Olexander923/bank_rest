package com.example.bankcards.exception;

public class CardAlreadyExistException extends RuntimeException {
    public CardAlreadyExistException(String message) {
        super(message);
    }
}
