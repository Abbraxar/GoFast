package com.i3cnam.gofast.model;

import com.google.android.gms.maps.model.LatLng;

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
