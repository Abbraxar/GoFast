package com.i3cnam.gofast.model;

import android.content.Context;
import android.content.SharedPreferences;

import com.i3cnam.gofast.R;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Created by Nestor on 18/07/2016.
 */
public class User implements Serializable{
    private String nickname; // it is the id
    private String phoneNumber;

    public User() {
        super();
    }

    public User(String nickname, String phoneNumber) {
        this.nickname = nickname;
        this.phoneNumber = phoneNumber;
    }

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

    public static User getMe(Context context){
        User me = new User();
        // recover user account
        SharedPreferences prefs = context.getSharedPreferences(context.getString(R.string.app_name), Context.MODE_PRIVATE);
        me.nickname = prefs.getString("userNickname",null);
        me.phoneNumber = prefs.getString("userPhoneNumber",null);
        if (me.nickname == null) {me = null;}
        return me;
    }


    private void readObject(final ObjectInputStream ois) throws IOException,
            ClassNotFoundException {
        this.nickname = (String) ois.readObject();
        this.phoneNumber = (String) ois.readObject();
    }

    private void writeObject(final ObjectOutputStream oos) throws IOException {
        oos.writeObject(this.nickname);
        oos.writeObject(this.phoneNumber);
    }
}
