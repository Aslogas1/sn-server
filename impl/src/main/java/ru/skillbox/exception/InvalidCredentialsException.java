package ru.skillbox.exception;

public class InvalidCredentialsException extends RuntimeException{
    public InvalidCredentialsException() {
        super("Неверные учётнные данные");
    }
}
