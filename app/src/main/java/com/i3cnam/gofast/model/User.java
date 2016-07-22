package com.i3cnam.gofast.model;

/**
 * Created by Nestor on 18/07/2016.
 */
public class User {
    private String nickname; // it is the id
    private String phoneNumber;

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public static User getMe(){
        User me = new User();
        me.nickname = "titi42";
        me.phoneNumber = "+33629386194";
        return me;
    }
}
