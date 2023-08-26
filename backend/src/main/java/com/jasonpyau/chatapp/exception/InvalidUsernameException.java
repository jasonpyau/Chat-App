package com.jasonpyau.chatapp.exception;

import lombok.Getter;

@Getter
public class InvalidUsernameException extends InvalidInputException {

    private String username;

    public InvalidUsernameException(String msg, String username) {
        super(msg);
        this.username = username;
    }
}
