package com.deepoove.authsaur.response;

import lombok.Data;

@Data
public class UserDirectoryDTO {

    private String id;
    private String name;
    private String source;
    private String type;
    private String authnName;
    private String related;
    private int count;

}
