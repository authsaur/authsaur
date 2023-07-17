package com.deepoove.authsaur.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TokenCacheModel {

    private CasingUser user;
    private String token;
    private String st;
}
