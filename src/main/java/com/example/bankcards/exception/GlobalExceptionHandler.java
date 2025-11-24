package com.example.bankcards.exception;

import com.example.bankcards.dto.ErrorResponseDTO;
import org.apache.tomcat.websocket.AuthenticationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(TransferFailedException.class)
    public ResponseEntity<ErrorResponseDTO> handleTransferFailed(TransferFailedException e){
        return ResponseEntity.badRequest().body(new ErrorResponseDTO(e.getMessage()));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponseDTO> handleAuthExceptions(AuthenticationException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponseDTO(e.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponseDTO> handleIllegalState(IllegalStateException e){
       return ResponseEntity.badRequest().body(new ErrorResponseDTO(e.getMessage()));
    }

    @ExceptionHandler(com.example.bankcards.exception.ValidationException.class)
    public ResponseEntity<ErrorResponseDTO> validationException(ValidationException e){
        return ResponseEntity.badRequest().body(new ErrorResponseDTO(e.getMessage()));
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleUserExistException(UserNotFoundException e){
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponseDTO(e.getMessage()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponseDTO> handleMessageNotReadableException(){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponseDTO("Invalid date format. Use YYYY-MM-DD."));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleMethodArgumentNotValidException(MethodArgumentNotValidException e){
        String message = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(FieldError::getDefaultMessage) //тут будет сообщение ошибки из @size в дто
                .orElse("Validation failed");

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponseDTO(message));
    }
    @ExceptionHandler(CardAlreadyExistException.class)
    public ResponseEntity<ErrorResponseDTO> handleCardAlreadyExistException(CardAlreadyExistException e){
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponseDTO(e.getMessage()));
    }

    @ExceptionHandler(CardNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleCardNotFoundException(CardNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponseDTO(e.getMessage()));
    }



}
