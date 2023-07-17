package com.deepoove.authsaur.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class AuthenticatorExistException extends RuntimeException {

    private String id;

}
