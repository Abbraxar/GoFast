package com.i3cnam.gofast.management.carpooling;

import com.i3cnam.gofast.model.Carpooling;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nestor on 04/08/2016.
 */
public class CarpoolListEncapsulated implements Serializable {
    public List<Carpooling> list;

    public CarpoolListEncapsulated(List<Carpooling> list) {
        this.list = list;
    }


    private void readObject(final ObjectInputStream ois) throws IOException,
            ClassNotFoundException {

        int carpoolNr = ois.readInt();
        list = new ArrayList<>();
        for (int i = 0 ; i < carpoolNr ; i++) {
            list.add((Carpooling) ois.readObject());
        }
    }

    private void writeObject(final ObjectOutputStream oos) throws IOException {

        if (list == null) {
            oos.writeInt(0);
        }
        else {
            oos.writeInt(list.size());
            for (Carpooling c : list) {
                oos.writeObject(c);
            }
        }

    }





}
