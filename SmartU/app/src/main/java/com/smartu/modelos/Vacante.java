package com.smartu.modelos;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Emilio Chica Jiménez on 25/05/2017.
 */

public class Vacante implements Parcelable,Serializable {

    private int id;
    private ArrayList<Especialidad> especialidades=null;

    public Vacante(ArrayList<Especialidad> especialidades) {

        this.especialidades = especialidades;
    }
    public Vacante(){
        this.especialidades = new ArrayList<>();
    }

    protected Vacante(Parcel in) {
        especialidades = in.createTypedArrayList(Especialidad.CREATOR);
        id = in.readInt();
    }

    public static final Creator<Vacante> CREATOR = new Creator<Vacante>() {
        @Override
        public Vacante createFromParcel(Parcel in) {
            return new Vacante(in);
        }

        @Override
        public Vacante[] newArray(int size) {
            return new Vacante[size];
        }
    };

    public ArrayList<Especialidad> getEspecialidades() {
        return especialidades;
    }

    public void setEspecialidades(ArrayList<Especialidad> especialidades) {
        this.especialidades = especialidades;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeTypedList(especialidades);
        dest.writeInt(id);
    }
}
