package com.i3cnam.gofast.model;

/**
 * Created by Nestor on 18/07/2016.
 */
public class User {
    private String nickname;
    private String phoneNumber;

    public static User getMe(){
        User me = new User();
        me.nickname = "titi42";
        me.phoneNumber = "+33629386194";
        return me;
    }
}
