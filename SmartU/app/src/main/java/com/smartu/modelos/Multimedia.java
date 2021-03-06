package com.smartu.modelos;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * Created by Emilio Chica Jiménez on 18/05/2017.
 */

public class Multimedia implements Parcelable,Serializable {
    private int id;
    private String nombre;
    private String url;
    private String tipo;
    private String urlPreview;
    private String urlSubtitulos;

    public Multimedia(){}
    public Multimedia(int id, String nombre, String url, String tipo, String urlPreview, String urlSubtitulos) {
        this.id = id;
        this.nombre = nombre;
        this.url = url;
        this.tipo = tipo;
        this.urlPreview = urlPreview;
        this.urlSubtitulos = urlSubtitulos;
    }

    protected Multimedia(Parcel in) {
        id = in.readInt();
        nombre = in.readString();
        url = in.readString();
        tipo = in.readString();
        urlPreview = in.readString();
        urlSubtitulos = in.readString();
    }

    public static final Creator<Multimedia> CREATOR = new Creator<Multimedia>() {
        @Override
        public Multimedia createFromParcel(Parcel in) {
            return new Multimedia(in);
        }

        @Override
        public Multimedia[] newArray(int size) {
            return new Multimedia[size];
        }
    };

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getUrlPreview() {
        return urlPreview;
    }

    public void setUrlPreview(String urlPreview) {
        this.urlPreview = urlPreview;
    }

    public String getUrlSubtitulos() {
        return urlSubtitulos;
    }

    public void setUrlSubtitulos(String urlSubtitulos) {
        this.urlSubtitulos = urlSubtitulos;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(nombre);
        dest.writeString(url);
        dest.writeString(tipo);
        dest.writeString(urlPreview);
        dest.writeString(urlSubtitulos);
    }
}
