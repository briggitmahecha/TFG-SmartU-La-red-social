package com.smartu.vistas;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.smartu.R;
import com.smartu.adaptadores.AdapterIntegrante;
import com.smartu.almacenamiento.Almacen;
import com.smartu.hebras.HUsuarios;
import com.smartu.modelos.Proyecto;
import com.smartu.modelos.Status;
import com.smartu.modelos.Usuario;
import com.smartu.modelos.Vacante;
import com.smartu.utilidades.EndlessRecyclerViewScrollListener;

import java.util.ArrayList;

import java8.util.stream.StreamSupport;


/**
 * Una subclase simple {@link Fragment}.
 * Las Activities que lo contentgan deben de implementar
 * {@link OnIntegranteSelectedListener} interfaz
 * para manejar la interacción de eventos.
 * Usa el método factoría {@link FragmentIntegrantes#newInstance} para
 * crear una instancia de este fragmento.
 */
public class FragmentIntegrantes extends Fragment {
    //ArrayList de integrantes para cargar y pasar cuando se cambie de Fragment
    private ArrayList<Usuario> integrantes = new ArrayList<>();
    //ArrayList de  vacantes para añadir integrantes vacantes al ArrayList de integrantesConVacantes
    private ArrayList<Vacante> vacantesUsuarios = new ArrayList<>();
    //Lo necesito para comparar solitudes de unión a este proyecto
    private Proyecto proyecto;
    //El argumento que tienen que pasarme o que tengo que pasar en los Intent
    private static final String ARG_INTEGRANTES = "integrantes";
    private static final String ARG_VACANTES = "vacantes";
    private static final String ARG_PROYECTO = "proyecto";
    private RecyclerView recyclerViewUsuarios;
    private AdapterIntegrante adapterIntegrante;

    private OnIntegranteSelectedListener mListener;

    //Va hacer de listener para cuando llegue al final del RecyclerView
    //y necesite cargar más elementos
    private EndlessRecyclerViewScrollListener scrollListener;

    public FragmentIntegrantes() {
        // Constructor vacío es necesario
    }

    /**
     * Usar este constructor para crear una instancia de
     * este fragment con parámetros
     *
     * @param integrantes Parametro 1.
     * @return A devuelve una nueva instancia del fragment.
     */
    public static FragmentIntegrantes newInstance(ArrayList<Usuario> integrantes, ArrayList<Vacante> vacantesUsuarios, Proyecto proyecto) {
        FragmentIntegrantes fragment = new FragmentIntegrantes();
        Bundle args = new Bundle();
        args.putParcelableArrayList(ARG_INTEGRANTES, integrantes);
        args.putParcelableArrayList(ARG_VACANTES, vacantesUsuarios);
        args.putParcelable(ARG_PROYECTO,proyecto);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            integrantes = getArguments().getParcelableArrayList(ARG_INTEGRANTES);
            vacantesUsuarios = getArguments().getParcelableArrayList(ARG_VACANTES);
            proyecto = getArguments().getParcelable(ARG_PROYECTO);
        }
        if(proyecto.getIntegrantes()!=null)
            Almacen.buscarUsuarios(proyecto.getIntegrantes(),integrantes,getContext());
        Usuario u = new Usuario();
        Almacen.buscar(proyecto.getIdPropietario(),u,getContext());
        boolean esta = StreamSupport.stream(integrantes).filter(usuario -> usuario.getId()==u.getId()).findAny().isPresent();
        if(!esta)
            integrantes.add(u);
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View fragmen =inflater.inflate(R.layout.fragment_integrantes_proyecto, container, false);

        recyclerViewUsuarios = (RecyclerView) fragmen.findViewById(R.id.recyclerIntegrantes);
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerViewUsuarios.setLayoutManager(llm);

        // Guardo la instancia para poder llamar a `resetState()` para nuevas busquedas
        scrollListener = new EndlessRecyclerViewScrollListener(llm) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                // El evento sólo se provoca cuando necesito añadir más elementos
                cargarMasUsuarios(page);
            }
        };


        return fragmen;
    }
    /**
     * Carga hasta 10 usuarios si hubiese a partir del offset
     * que le ofrece el método LoadMore
     * @param offset
     */
    public void cargarMasUsuarios(int offset) {
        HUsuarios hUsuarios = new HUsuarios(adapterIntegrante,offset,getActivity());
        hUsuarios.sethUsuarios(hUsuarios);
        hUsuarios.setIdProyecto(proyecto.getId());
        hUsuarios.execute();
    }
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        adapterIntegrante = new AdapterIntegrante(getActivity(), mezclaIntegrantesConVacantes(),mListener,proyecto);
        recyclerViewUsuarios.setAdapter(adapterIntegrante);
        // Adds the scroll listener to RecyclerView
        recyclerViewUsuarios.addOnScrollListener(scrollListener);

    }

    /**
     * Mezcla los integrantes del proyecto con las vacantes, para las cuales crea usuarios vacantes.
     * @return
     */
    private ArrayList<Usuario> mezclaIntegrantesConVacantes(){
        ArrayList<Usuario> integrantesConVacantes = new ArrayList<>();
        //Paso los usuarios que tengo en el proyecto al array
        for(Usuario usuario: integrantes)
            integrantesConVacantes.add(usuario);
        //Por cada vacante cre un usuario ficticio para que sirva para unirse
        //con un id negativo para distinguirlo de los integrantes
        for (Vacante v:vacantesUsuarios){
            Usuario usuario = new Usuario();
            usuario.setId(-1);
            usuario.setNombre("Únete al proyecto");
            usuario.setMiStatus(new Status(-1,"Vacante",0,0));
            usuario.setMisEspecialidades(v.getEspecialidades());
            //Paso el id de la vacante a traves de los puntos
            usuario.setnPuntos(v.getId());
            integrantesConVacantes.add(usuario);
        }
        return integrantesConVacantes;
    }


    public void onButtonPressed(int idUsuario) {
        if (mListener != null) {
            mListener.onUsuarioSeleccionado(idUsuario);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnIntegranteSelectedListener) {
            mListener = (OnIntegranteSelectedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " debe implementar OnIntegranteSelectedListener");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onPause() {
        super.onPause();
    }



    /**
     * Esta interfaz debe ser implementada por las activities que contienen este
     * fragment para permitir una interacción con este fragment y comunicar a
     * a la activity y potencialmente otros fragments contenidos en esta
     * activity.
     * <p>
     * Referencias a la lección Android Training <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnIntegranteSelectedListener {
        void onUsuarioSeleccionado(int idUsuario);
    }
}
