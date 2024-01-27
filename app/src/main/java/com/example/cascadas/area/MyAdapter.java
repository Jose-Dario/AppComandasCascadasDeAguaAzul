package com.example.cascadas.area;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cascadas.AlertLoader;
import com.example.cascadas.Comanda;
import com.example.cascadas.ProductoOrdenado;
import com.example.cascadas.R;
import com.example.cascadas.SendNotification;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Map;


public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
    private ArrayList<Comanda> dataList;

    private TTSManager ttsManager;

    private SendNotification notification=new SendNotification();


    public MyAdapter(Context context) {
        this.dataList = new ArrayList<>();
        ttsManager=((MainArea)context).getTtsManager();
    }

    public void add(Comanda comanda){
        dataList.add(comanda);
        notifyItemChanged(dataList.size()-1);
    }

    public void actualizar(DocumentChange documentChange){
        for(int i=0;i<dataList.size();i++){
            Comanda comanda=dataList.get(i);
            if(comanda.getDocumentReference().equals(documentChange.getDocument().getReference())){
                comanda.setMesa(documentChange.getDocument().getString("mesa"));
                comanda.setEstado(documentChange.getDocument().getString("estado"));
                comanda.setMensaje(documentChange.getDocument().getString("mensaje"));
                ArrayList<ProductoOrdenado> listaAuxiliarProductos=new ArrayList<>();
                for (Map map:(ArrayList< Map >)documentChange.getDocument().get("productos")){
                    listaAuxiliarProductos.add(new ProductoOrdenado(map.get("producto").toString(),Double.parseDouble(map.get("precio").toString()),
                            map.get("area").toString(),Integer.parseInt(map.get("cantidad").toString()),map.get("descripcion").toString()));
                }
                comanda.setProductoOrdenados(listaAuxiliarProductos);
                notifyItemChanged(i);
                break;
            }
        }
    }

    public void remove(DocumentChange documentChange){
        for (int i=0;i<dataList.size();i++){
            if(dataList.get(i).getDocumentReference().equals(documentChange.getDocument().getReference())){
                notifyItemRemoved(i);
                dataList.remove(i);
                return;
            }
        }
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Infla el diseño del elemento de la lista
        View view = inflater.inflate(R.layout.card_comandas_cocina, parent, false);

        // Retorna una nueva instancia del ViewHolder
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        // Obtiene el elemento actual en la posición
        Comanda item = dataList.get(position);

        // Establece los datos en el ViewHolder
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    // Clase ViewHolder
    public class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView titulo, estado, mensaje;
        //private TextView estado;
        private ImageButton listen,verificar;
        private LinearLayout pedidos;

        private ImageButton imgButtonIni, imgButtonFin;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            // Encuentra la vista dentro del diseño del elemento
            pedidos = itemView.findViewById(R.id.contPedidosCocina);
            titulo=itemView.findViewById(R.id.tituloComandaCocina);
            estado=itemView.findViewById(R.id.estadoComandaCocina);
            mensaje=itemView.findViewById(R.id.mensajeComandaCocina);
            listen=itemView.findViewById(R.id.btnListenComanda);
            verificar=itemView.findViewById(R.id.btnVerificar);
            imgButtonIni =itemView.findViewById(R.id.imgButttonIni);
            imgButtonFin=itemView.findViewById(R.id.imgButtonFin);
        }

        public void bind(Comanda item) {
            // Establece los datos en la vista
            titulo.setText("Mesa "+item.getMesa()+": "+item.getMesero());
            estado.setText("Estado: "+item.getEstado());
            if(item.getMensaje().equalsIgnoreCase("mensaje")){
                mensaje.setVisibility(View.GONE);
            }
            else{
                mensaje.setText("Mensaje: "+item.getMensaje());
                mensaje.setVisibility(View.VISIBLE);
            }
            switch (item.getEstado()){
                case "en espera":
                    imgButtonIni.setEnabled(true);
                    verificar.setEnabled(true);
                    imgButtonIni.setVisibility(View.VISIBLE);
                    imgButtonFin.setVisibility(View.GONE);
                    break;
                case "corregida":
                    imgButtonIni.setEnabled(true);
                    verificar.setEnabled(true);
                    imgButtonIni.setVisibility(View.VISIBLE);
                    imgButtonFin.setVisibility(View.GONE);
                    break;
                case "en preparacion":
                    imgButtonIni.setEnabled(true);
                    verificar.setEnabled(false);
                    imgButtonIni.setVisibility(View.GONE);
                    imgButtonFin.setVisibility(View.VISIBLE);
                    break;
                case "en verificacion":
                    imgButtonIni.setEnabled(false);
                    verificar.setEnabled(false);
                    mensaje.setVisibility(View.VISIBLE);
                    imgButtonIni.setVisibility(View.VISIBLE);
                    imgButtonFin.setVisibility(View.GONE);
                    break;
                case "en modificacion":
                    imgButtonIni.setEnabled(false);
                    verificar.setEnabled(false);
                    imgButtonIni.setVisibility(View.VISIBLE);

                    imgButtonFin.setVisibility(View.GONE);
                    break;
            }
            pedidos.removeAllViews();
            for (ProductoOrdenado aux:item.getProductoOrdenados()){
                final boolean[] hecho = {false};
                View view1=LayoutInflater.from(itemView.getContext()).inflate(R.layout.card_producto_ord_cocina,null);
                ((TextView)view1.findViewById(R.id.cantP)).setText(""+aux.getCantidad());
                ((TextView)view1.findViewById(R.id.pOrden)).setText(aux.getProducto());
                TextView descripcion=view1.findViewById(R.id.tvDescP);
                view1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(!hecho[0]){
                            view1.setBackgroundColor(Color.LTGRAY);
                            hecho[0] =true;
                        }
                        else {
                            view1.setBackgroundColor(Color.TRANSPARENT);
                            hecho[0] =false;
                        }
                    }
                });
                if(!aux.getDescripcion().equals("")){
                    descripcion.setText(aux.getDescripcion());
                    descripcion.setVisibility(View.VISIBLE);
                }else{
                    descripcion.setVisibility(View.GONE);
                }
                pedidos.addView(view1);
            }

            verificar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(itemView.getContext());
                    LayoutInflater infla = ((MainArea)itemView.getContext()).getLayoutInflater();
                    View dialogView = infla.inflate(R.layout.card_enviar_mensaje, null);
                    EditText mensaje=dialogView.findViewById(R.id.inputMensaje);

                    // Crea el AlertDialog
                    builder.setView(dialogView).setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Toast.makeText(itemView.getContext(), "Mensaje no enviado",Toast.LENGTH_SHORT).show();
                                }
                            })
                            //.setTitle("AlertDialog con Diseño Personalizado")
                            .setPositiveButton("Enviar", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if(mensaje.getText().toString().equals("")){
                                        Toast.makeText(itemView.getContext(), "Verificación anulada",Toast.LENGTH_SHORT).show();
                                    }
                                    else {
                                        AlertLoader alertLoader=new AlertLoader((Activity) itemView.getContext());
                                        item.getDocumentReference().update("estado","en verificacion").addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()){
                                                    item.getDocumentReference().update("mensaje",mensaje.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if(task.isSuccessful()){
                                                                notificar("Verificación de comanda","Comanda del cliente "+item.getCliente()+", Mesa "+item.getMesa()+", necesita ser verificada",item.getMesero());
                                                                alertLoader.minimizar("Verificación enviada");
                                                            }
                                                            else {
                                                                alertLoader.showError("Error al enviar la verificación");
                                                            }
                                                        }
                                                    });
                                                }
                                                else {
                                                    alertLoader.showError("Error al enviar la verificación");
                                                }
                                            }
                                        });

                                    }
                                    // Acciones a realizar cuando se hace clic en Aceptar
                                }
                            });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }
            });

            listen.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String texto="Mesa "+item.getMesa()+"... ";
                    for(ProductoOrdenado producto:item.getProductoOrdenados()){
                            texto+=getLectura(producto);

                    }
                    ttsManager.initQueue(texto);
                }
            });

            imgButtonIni.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    item.getDocumentReference().update("estado","en preparacion");
                    imgButtonIni.setEnabled(true);
                    verificar.setEnabled(false);

                }
            });

            imgButtonFin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    notifyItemRemoved(dataList.indexOf(item));
                    dataList.remove(item);
                    ComandasFinalizadasFragment.adapterComandasFinalizadas.add(item);
                    item.getDocumentReference().update("estado","finalizado");
                    notificar("Comanda finalizada","Comanda del cliente "+item.getCliente()+", Mesa "+item.getMesa()+", ha sido elaborada",item.getMesero());
                }
            });

        }
    }

    public boolean isPalabraFemino(String palabra){
        if(palabra.charAt(palabra.length()-1)=='a' || palabra.charAt(palabra.length()-1)=='A'){
            return true;
        }
        else if(palabra.equalsIgnoreCase("orden")){
            return true;
        }
        else if(palabra.length()>1){
            if(palabra.substring(palabra.length()-1,palabra.length()).equalsIgnoreCase("as")){
                return true;
            }
        }
        return false;
    }

    @SuppressLint("SuspiciousIndentation")
    public String getLectura(ProductoOrdenado producto){
        String[] palabras = producto.getProducto().split(" ");
        String palabra = palabras[0];
        String aux="";
        if(palabra.charAt(palabra.length()-1)=='a' || isPalabraFemino(palabra)){
            if(producto.getCantidad()==1){
                aux+="una "+producto.getProducto()+" "+producto.getDescripcion()+",";
            }
            else{
                aux+=producto.getCantidad()+" "+producto.getProducto()+" "+producto.getDescripcion()+",";
            }
        }
        else if (palabra.charAt(palabra.length()-1)=='s' || palabra.charAt(palabra.length()-1)=='s'){
            if(producto.getCantidad()==1){
                if(palabra.charAt(palabra.length()-2)=='a'||palabra.charAt(palabra.length()-2)=='a')
                {
                    aux+="unas "+producto.getProducto()+" "+producto.getDescripcion()+",";

                }
                else
                    aux+="unos "+producto.getProducto()+" "+producto.getDescripcion()+",";
            }
            else{
                aux+=producto.getCantidad()+" "+producto.getProducto()+" "+producto.getDescripcion()+",";
            }
        }
        else{
            if(producto.getCantidad()==1){
                aux+="un "+producto.getProducto()+" "+producto.getDescripcion()+",";
            }
            else{
                aux+=producto.getCantidad()+" "+producto.getProducto()+" "+producto.getDescripcion()+",";
            }
        }
        return aux;
    }

    public void notificar(String titulo, String mensaje, String mesero){
        FirebaseFirestore.getInstance().collection("Usuarios").whereEqualTo("nombre",mesero).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    for (QueryDocumentSnapshot dc:task.getResult()){
                        notification.sendMessage(dc.getString("token"),titulo,mensaje);
                    }
                }
            }
        });
    }

}
