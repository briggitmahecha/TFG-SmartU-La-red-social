package com.smartu.adaptadores;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ns.developer.tagview.entity.Tag;
import com.ns.developer.tagview.widget.TagCloudLinkView;
import com.smartu.R;
import com.smartu.hebras.HSeguir;
import com.smartu.modelos.Especialidad;
import com.smartu.modelos.Usuario;
import com.smartu.utilidades.ConsultasBBDD;
import com.smartu.utilidades.Sesion;
import com.smartu.vistas.FragmentUsuarios;
import com.smartu.vistas.LoginActivity;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;


public class AdapterUsuario extends RecyclerView.Adapter<AdapterUsuario.ViewHolder> {
    private Context context;
    private ArrayList<Usuario> usuarios;
    private FragmentUsuarios.OnUsuarioSelectedListener onUsuarioSelectedListener;
    private Usuario usuario;
    private HSeguir hSeguir;
    private Usuario usuarioSesion;


    public AdapterUsuario(Context context, ArrayList<Usuario> items, FragmentUsuarios.OnUsuarioSelectedListener onUsuarioSelectedListener) {
        super();
        this.context = context;
        this.usuarios = items;
        this.onUsuarioSelectedListener = onUsuarioSelectedListener;
    }

    //Creating a ViewHolder which extends the RecyclerView View Holder
    // ViewHolder are used to to store the inflated views in order to recycle them
    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView nombreUsuario;
        TagCloudLinkView especialidadUsuario;
        TextView statusUsuario;
        TextView seguidoresUsuario;
        ImageView imgUsuario;
        Button seguirUsuario;

        public ViewHolder(View itemView, int viewType) {
            super(itemView);
            nombreUsuario = (TextView) itemView.findViewById(R.id.nombre_usuario);
            statusUsuario = (TextView) itemView.findViewById(R.id.status_usuario);
            seguidoresUsuario = (TextView) itemView.findViewById(R.id.seguidores_usuario);
            especialidadUsuario = (TagCloudLinkView) itemView.findViewById(R.id.especialidad_usuario);
            imgUsuario = (ImageView) itemView.findViewById(R.id.img_usuario);
            seguirUsuario = (Button) itemView.findViewById(R.id.nombre_usuario_o_proyecto);

        }

    }

    @Override
    public AdapterUsuario.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_usuario_recyclerview, parent, false); //Inflating the layout

        ViewHolder vhItem = new ViewHolder(v, viewType);

        return vhItem;
    }

    @Override
    public void onBindViewHolder(final AdapterUsuario.ViewHolder holder, int position) {
        usuario = (Usuario) this.usuarios.get(position);
        Picasso.with(context).load(ConsultasBBDD.server+usuario.getImagenPerfil()).into(holder.imgUsuario);
        holder.nombreUsuario.setText(usuario.getNombre());
        int numEspecialidades=3;
        //Pongo de previsualización 3 especialidades como mucho
        if(usuario.getMisEspecialidades().size()<3)
            numEspecialidades = usuario.getMisEspecialidades().size();
        //Añado las especialidades al TagCloud
        for(int i=0;i<numEspecialidades;++i){
            Especialidad e =usuario.getMisEspecialidades().get(i);
            holder.especialidadUsuario.add(new Tag(e.getId(), e.getNombre()));
        }

        holder.seguidoresUsuario.setText(String.valueOf(usuario.getMisSeguidos().size()));
        holder.statusUsuario.setText(usuario.getMiStatus().getNombre());

        holder.imgUsuario.setOnClickListener(cargaUsuario());
        holder.statusUsuario.setOnClickListener(cargaUsuario());
        holder.nombreUsuario.setOnClickListener(cargaUsuario());


        //Cargo las preferencias del usuario si tuviese sesión
        cargarPreferenciasUsuario(holder.seguirUsuario);
        holder.seguirUsuario.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                usuarioSesion = Sesion.getUsuario(context);
                //Si el usuario ha iniciado sesión
                if (usuarioSesion != null) {
                    //Actualizo el botón
                    holder.seguirUsuario.setPressed(!holder.seguirUsuario.isPressed());
                    //Compruebo como ha quedado su estado después de hacer click
                    if (holder.seguirUsuario.isPressed()) {
                        holder.seguirUsuario.setText(R.string.no_seguir);
                        //Añado al contador 1 para decir que eres seguidor
                        int cont = Integer.parseInt(holder.seguidoresUsuario.getText().toString())+1;
                        holder.seguidoresUsuario.setText(String.valueOf(cont));
                        Toast.makeText(context, "Genial!,sigues a este usuario!", Toast.LENGTH_SHORT).show();
                        //Inicializo la hebra con 0 pues voy a añadir una nueva idea
                        hSeguir = new HSeguir(context,holder.seguirUsuario,holder.seguidoresUsuario);
                        //Para poder poner la referencia a null cuando termine la hebra
                        hSeguir.sethSeguir(hSeguir);
                    }
                    else {
                        holder.seguirUsuario.setText(R.string.seguir);
                        //Añado al contador 1 para decir que eres seguidor
                        int cont = Integer.parseInt(holder.seguidoresUsuario.getText().toString())-1;
                        holder.seguidoresUsuario.setText(String.valueOf(cont));
                        Toast.makeText(context,"¿Ya no te interesa el usuario?",Toast.LENGTH_SHORT).show();
                        //Inicializo la hebra con el id de la buena idea que encontré
                        hSeguir = new HSeguir(usuarioSesion.getId(),usuario.getId(),context,holder.seguirUsuario,holder.seguidoresUsuario);
                        //Para poder poner la referencia a null cuando termine la hebra
                        hSeguir.sethSeguir(hSeguir);
                    }
                    hSeguir.execute();
                }
                else
                {
                    Intent intent = new Intent(context, LoginActivity.class);
                    context.startActivity(intent);
                }
            }
        });
    }

    @Override
    public long getItemId(int position) {
        return ((Usuario) usuarios.get(position)).getId();
    }

    @Override
    public int getItemCount() {
        return usuarios.size();
    }


    private View.OnClickListener cargaUsuario() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onUsuarioSelectedListener.onUsuarioSeleccionado(usuario);
            }
        };
    }
    /**
     * Comprueba si el usuario ha dado buena idea al proyecto
     */
    private void cargarPreferenciasUsuario(Button seguirUsuario){
        //Cargo las preferencias del usuario
        if(usuarioSesion!=null) {
            //Compruebo si el usuario le ha dado antes a seguir a este usuario
            boolean usuarioSigue =  usuarioSesion.getMisSeguidos().stream().anyMatch(usuario1 -> usuario1.getId() == usuario.getId());
            //Si es así lo dejo presionado
            seguirUsuario.setPressed(usuarioSigue);
            if(usuarioSigue)
                seguirUsuario.setText(R.string.no_seguir);
        }
    }

}