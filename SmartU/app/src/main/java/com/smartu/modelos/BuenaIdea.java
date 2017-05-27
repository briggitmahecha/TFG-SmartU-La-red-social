package com.smartu.modelos;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Emilio Chica Jiménez on 25/05/2017.
 */

public class BuenaIdea implements Parcelable{

    private int idUsuario;

    public BuenaIdea(int idUsuario) {
        this.idUsuario = idUsuario;
    }


    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }


    protected BuenaIdea(Parcel in) {
        idUsuario = in.readInt();
    }

    public static final Creator<BuenaIdea> CREATOR = new Creator<BuenaIdea>() {
        @Override
        public BuenaIdea createFromParcel(Parcel in) {
            return new BuenaIdea(in);
        }

        @Override
        public BuenaIdea[] newArray(int size) {
            return new BuenaIdea[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(idUsuario);
    }
}