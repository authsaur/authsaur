package com.deepoove.authsaur.advice;

import com.deepoove.authsaur.exception.AuthenticatorExistException;
import com.deepoove.authsaur.exception.AuthenticatorNotFoundException;
import com.deepoove.authsaur.response.Result;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

@ControllerAdvice
public class ExceptionAdvice {

    @ResponseBody
    @ExceptionHandler(value = AuthenticatorNotFoundException.class)
    public Result<Object> handleAuthenticatorNotFound(HttpServletRequest r, AuthenticatorNotFoundException ex) {
        return Result.fail("Authenticator not found: " + ex.getId());
    }

    @ResponseBody
    @ExceptionHandler(value = AuthenticatorExistException.class)
    public Result<Object> handleAuthenticatorExist(HttpServletRequest r, AuthenticatorNotFoundException ex) {
        return Result.fail("Authenticator has already added: " + ex.getId());
    }
}
