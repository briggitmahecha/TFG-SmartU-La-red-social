package com.smartu.adaptadores;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.smartu.R;
import com.smartu.modelos.Notificacion;
import com.smartu.vistas.ProyectoActivity;
import com.smartu.vistas.UsuarioActivity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;


public class AdapterNotificacion extends RecyclerView.Adapter<AdapterNotificacion.ViewHolder> {
	private Context context;
	private ArrayList<Notificacion> notificaciones;
	private Notificacion notificacion;

	//Es el número total de elementos que hay en el server
	//tengo que recogerlo de las hebras de consulta
	private int totalElementosServer = -1;

	// Dos tipos de vistas para saber si es un ProgressBar lo que muestro o la vista normal
	public static final int VIEW_TYPE_LOADING = 0;
	public static final int VIEW_TYPE_ACTIVITY = 1;

	public void setTotalElementosServer(int totalElementosServer) {
		this.totalElementosServer = totalElementosServer;
	}

	public AdapterNotificacion(Context context, ArrayList<Notificacion> items) {
		super();
		this.context = context;
		this.notificaciones = items;
	}

	//Creating a ViewHolder which extends the RecyclerView View Holder
	// ViewHolder are used to to store the inflated views in order to recycle them

	public static class ViewHolder extends RecyclerView.ViewHolder {
		int tipoView;
		TextView nombreNotificacion;
		TextView descripcionNotificacion;
		TextView fechaNotificacion;
		Button nombreUsuarioOProyecto;

		public ViewHolder(View itemView, int viewType) {
			super(itemView);
			if(viewType==VIEW_TYPE_ACTIVITY) {
			nombreNotificacion = (TextView) itemView.findViewById(R.id.nombre_notificacion);
			descripcionNotificacion = (TextView) itemView.findViewById(R.id.descripcion_notificacion);
			fechaNotificacion = (TextView) itemView.findViewById(R.id.fecha_notificacion);
			nombreUsuarioOProyecto = (Button) itemView.findViewById(R.id.nombre_usuario_o_proyecto);
				tipoView=1;
			}else{
				tipoView=0;
			}
		}

	}

	@Override
	public AdapterNotificacion.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		if (viewType == VIEW_TYPE_LOADING) {
			View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.progress,parent,false);

			ViewHolder vhBottom = new ViewHolder(v,viewType);

			if (vhBottom.getAdapterPosition() >= totalElementosServer && totalElementosServer > 0)
			{
				// the ListView has reached the last row
				TextView tvLastRow = new TextView(context);
				tvLastRow.setHint("No hay más elementos.");
				tvLastRow.setGravity(Gravity.CENTER);
				ViewHolder vhUltimo = new ViewHolder(tvLastRow,viewType);
				return vhUltimo;
			}

			return vhBottom;
		}else {
			View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notificacion_recyclerview, parent, false); //Inflating the layout

			ViewHolder vhItem = new ViewHolder(v, viewType);

			return vhItem;
		}
	}

	@Override
	public void onBindViewHolder(AdapterNotificacion.ViewHolder holder, int position) {
		//Sino es el último elemento ni es un progress bar pues muestro el elemento que me toca
		if(holder.tipoView==1) {
			notificacion = (Notificacion) this.notificaciones.get(position);

			holder.nombreNotificacion.setText(notificacion.getNombre());
			holder.descripcionNotificacion.setText(notificacion.getDescripcion());
			Date fecha = notificacion.getFecha();
			if (fecha != null) {
				Date tiempo = new Date(fecha.getTime() - new Date().getTime());
				Calendar calendar = GregorianCalendar.getInstance();
				calendar.setTime(tiempo);
				int horas = calendar.get(Calendar.HOUR);
				String hace = "Hace " + horas + " horas";
				holder.fechaNotificacion.setText(hace);
			}
			String textoBoton = "";
			if (notificacion.getUsuario() != null)
				textoBoton = notificacion.getUsuario().getNombre();
			else if (notificacion.getProyecto() != null)
				textoBoton = notificacion.getProyecto().getNombre();
			holder.nombreUsuarioOProyecto.setText(textoBoton);


			holder.nombreNotificacion.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					cargaElemento();
				}
			});
			holder.descripcionNotificacion.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					cargaElemento();
				}
			});
			holder.nombreUsuarioOProyecto.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					cargaElemento();
				}
			});
		}
	}

	@Override
	public long getItemId(int position) {
		return (getItemViewType(position) == VIEW_TYPE_ACTIVITY) ? notificaciones.get(position).getId()
				: -1;
	}
	/**
	 * Devuelve el tipo de fila,
	 * El ultimo elemento es el de loading
	 */
	@Override
	public int getItemViewType(int position) {
		return (position >= notificaciones.size()) ? VIEW_TYPE_LOADING : VIEW_TYPE_ACTIVITY;
	}

	@Override
	public int getItemCount() {
		return notificaciones.size()+1;
	}


	private void cargaElemento(){
			if(notificacion.getUsuario()!=null){
				Intent intent = new Intent(context,UsuarioActivity.class);
				intent.putExtra("usuario",notificacion.getUsuario());
				context.startActivity(intent);
			}else if(notificacion.getProyecto()!=null) {
				Intent intent = new Intent(context,ProyectoActivity.class);
				intent.putExtra("proyecto",notificacion.getProyecto());
				context.startActivity(intent);
			}
	}

	public void replaceData(ArrayList<Notificacion> items) {
		setList(items);
		notifyDataSetChanged();
	}

	public void setList(ArrayList<Notificacion> list) {
		this.notificaciones = list;
	}

	public void addItem(Notificacion pushMessage) {
		notificaciones.add(0, pushMessage);
		notifyItemInserted(0);
	}

}