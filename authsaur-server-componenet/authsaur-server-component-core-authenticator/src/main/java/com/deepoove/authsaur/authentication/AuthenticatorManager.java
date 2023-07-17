package com.deepoove.authsaur.authentication;

public interface AuthenticatorManager {

    void load();

    void unload() throws IllegalAccessException;

}
