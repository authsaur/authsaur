package com.deepoove.authsaur.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class AuthenticatorNotFoundException extends RuntimeException {

    private String id;

}
