package com.deepoove.authsaur.model;

import lombok.Data;

@Data
public class CasingUser {
//    name?: string;
//    avatar?: string;
//    userid?: string;
//    email?: string;
//    signature?: string;
//    title?: string;
//    group?: string;
//    tags?: { key?: string; label?: string }[];
//    notifyCount?: number;
//    unreadCount?: number;
//    country?: string;
//    access?: string;
//    geographic?: {
//      province?: { label?: string; key?: string };
//      city?: { label?: string; key?: string };
//    };
//    address?: string;
//    phone?: string;
    private String userid;
    private String name;
    private String avatar;
    private String email;
    private String phone;
}
