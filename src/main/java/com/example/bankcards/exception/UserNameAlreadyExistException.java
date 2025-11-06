package com.example.bankcards.exception;

public class UserNameAlreadyExistException extends Exception {
    public UserNameAlreadyExistException(String message) {
        super(message);
    }
}
