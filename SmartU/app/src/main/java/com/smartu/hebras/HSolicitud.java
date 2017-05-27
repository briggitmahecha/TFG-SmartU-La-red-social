package com.smartu.hebras;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Button;
import android.widget.Toast;

import com.smartu.R;
import com.smartu.modelos.Especialidad;
import com.smartu.modelos.Proyecto;
import com.smartu.modelos.SolicitudUnion;
import com.smartu.modelos.Usuario;
import com.smartu.utilidades.ConsultasBBDD;
import com.smartu.utilidades.Sesion;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Emilio Chica Jiménez on 27/05/2017.
 */

public class HSolicitud extends AsyncTask<Void, Void, String> {

    private Proyecto proyecto;
    private int idUsuario = 0;
    private ArrayList<Especialidad> especialidades;
    private Context context;
    private Button solicitarUnion;
    private HSolicitud hSolicitud;
    private boolean eliminar;
    private Date fechaActual;


    public HSolicitud(boolean eliminar, Proyecto proyecto, int idUsuario, ArrayList<Especialidad> especialidades, Context context, Button solicitarUnion) {
        this.proyecto = proyecto;
        this.idUsuario = idUsuario;
        this.especialidades = especialidades;
        this.context = context;
        this.solicitarUnion = solicitarUnion;
        this.eliminar = eliminar;
        this.fechaActual = new Date();
    }

    /**
     * Este método está para poder asignar a null la referencia de la hebra
     * para que el recolector de basura la elimine
     *
     * @param hSolicitud
     */
    public void sethSolicitud(HSolicitud hSolicitud) {
        this.hSolicitud = hSolicitud;
    }

    @Override
    protected String doInBackground(Void... params) {
        String descripcion = context.getString(R.string.descripcion_union);
        for (Especialidad e : especialidades) {
            descripcion += e.getNombre() + ", ";
        }
        String resultado = null;
        //Construyo el JSON
        String unirse = "";
        if (eliminar)
            unirse = "\"solicitudunion\":{\"idUsuario\":\"" + idUsuario + "\",\"idProyecto\":\"" + proyecto.getId() + "\"}";
        else
            unirse = "\"solicitudunion\":{\"idUsuario\":\"" + idUsuario + "\",\"idProyecto\":\"" + proyecto.getId() + "\",\"fecha\":\"" + fechaActual + "\"" +
                    ",\"descripcion\":\"" + descripcion + "\"}";

        if (eliminar)
            //Recojo el resultado en un String
            resultado = ConsultasBBDD.hacerConsulta(ConsultasBBDD.insertaUnion, unirse, "POST");
        else
            resultado = ConsultasBBDD.hacerConsulta(ConsultasBBDD.eliminarUnion, unirse, "POST");

        return resultado;
    }

    @Override
    protected void onPostExecute(String resultado) {
        super.onPostExecute(resultado);
        //Elimino la referencia a la hebra para que el recolector de basura la elimine de la memoria
        hSolicitud = null;
        //Obtengo el objeto JSON con el resultado
        JSONObject res = null;
        try {
            res = new JSONObject(resultado);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //Si tengo objeto compruebo el resultado y si es ok cambio el texto al botón
        //Sino muestro mensaje por pantalla
        if (res != null) {
            try {
                if (res.has("resultado") && res.getString("resutlado").compareToIgnoreCase("ok") != 0)
                    reestablecerEstado();
                else {
                    Usuario usuario = Sesion.getUsuario(context);
                    if (eliminar)
                        usuario.getMisSolicitudes().remove(usuario.getMisSolicitudes().stream().filter(solicitudUnion -> solicitudUnion.getProyecto().getId() == proyecto.getId()).findFirst().get());
                    else
                        usuario.getMisSolicitudes().add(new SolicitudUnion(fechaActual, proyecto));
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            reestablecerEstado();
        }
    }

    @Override
    protected void onCancelled(String resultado) {
        super.onCancelled(resultado);
        //Elimino la referencia a la hebra para que el recolector de basura la elimine de la memoria
        hSolicitud = null;
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        //Elimino la referencia a la hebra para que el recolector de basura la elimine de la memoria
        hSolicitud = null;
    }

    /**
     * Método que invierte los cambios realizados cuando se ha hecho la acción de seguir
     * a un usuario
     */
    private void reestablecerEstado() {
        Toast.makeText(context, "No se ha podido realizar la operacion, problemas de conexión?", Toast.LENGTH_SHORT).show();
        solicitarUnion.setText(R.string.unirse);
        solicitarUnion.setPressed(false);
        solicitarUnion.setEnabled(true);
    }
}
