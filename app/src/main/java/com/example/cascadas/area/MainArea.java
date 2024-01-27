package com.example.cascadas.area;

import static com.firebase.ui.auth.ui.phone.SubmitConfirmationCodeFragment.TAG;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.cascadas.AdapterComandasCocina;
import com.example.cascadas.AdapterMensajes;
import com.example.cascadas.AvisosFragment;
import com.example.cascadas.Comanda;
import com.example.cascadas.MainActivity;
import com.example.cascadas.Mensaje;
import com.example.cascadas.PerfilFragment;
import com.example.cascadas.ProductoOrdenado;
import com.example.cascadas.R;
import com.example.cascadas.Usuario;
import com.example.cascadas.encargado.AdapterCategorias;
import com.example.cascadas.encargado.Categoria;
import com.example.cascadas.encargado.Platillo;
import com.example.cascadas.encargado.PlatillosFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.cascadas.databinding.ActivityHomeCocinaBinding;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Map;

public class MainArea extends AppCompatActivity {

    private ActivityHomeCocinaBinding binding;
    private MediaPlayer entrante,corregida;

    public FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();

    public AdapterComandasFinalizadas comandasFinalizadas;
    public AdapterCategorias adapterCategorias;

    public AdapterMensajes adapterMensajes;

    public  TTSManager ttsManager;

    public MyAdapter adapterCocinaPrueba;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ttsManager=new TTSManager();
        ttsManager.init(this);
        comandasFinalizadas = new AdapterComandasFinalizadas(this);
        ComandasFinalizadasFragment.adapterComandasFinalizadas = comandasFinalizadas;
        //adapterComandasCocina = new AdapterComandasCocina(this);
        adapterCategorias = new AdapterCategorias(this);
        //prueba
        adapterCocinaPrueba=new MyAdapter(this);

        PlatillosFragment.adapterCategorias = adapterCategorias;
        adapterMensajes=new AdapterMensajes(this);
        AvisosFragment.adapterMensajes=adapterMensajes;
        //ComandasCocinaFragment.adapterComandasCocina = adapterComandasCocina;
        binding = ActivityHomeCocinaBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        BottomNavigationView navView = findViewById(R.id.nav_view_cocina);
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navig_inicio, R.id.navig_comandas_finalizadas, R.id.navig_platillos, R.id.navig_avisos,R.id.navig_perfil)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_home_cocina);
        NavigationUI.setupWithNavController(binding.navViewCocina, navController);
        entrante = MediaPlayer.create(getApplicationContext(), R.raw.entrante);
        corregida =MediaPlayer.create(getApplicationContext(),R.raw.corregida);
        consultarComandas();
        consultarCategorias();
        consultarMensajes();
        consultarUsuarios();
    }

    public void consultarComandas() {
        String area;
        if (PerfilFragment.usuario.getRol().equals("Barman")) {
            area = "barra";
        } else if (PerfilFragment.usuario.getRol().equals("Cocinero")) {
            area = "cocina";
        } else {
            area = "cocina chica";
        }
        firebaseFirestore.collection("Comandas").
                orderBy("fecha", Query.Direction.ASCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @SuppressLint("RestrictedApi")
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshots,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            showError("Error al consultar la tabla Comandas");
                            Log.w(TAG, "listen:error", e);
                            return;
                        }
                        for (DocumentChange dc : snapshots.getDocumentChanges()) {
                            switch (dc.getType()) {
                                case ADDED:
                                    if (dc.getDocument().getString("area").equals(area) || dc.getDocument().getString("area").equals("ambos")) {
                                        if (dc.getDocument().getString("estado").equals("entregado") || dc.getDocument().getString("estado").equals("finalizado")) {
                                            comandasFinalizadas.add(new Comanda(dc.getDocument().getString("mesero"), dc.getDocument().getString("cliente"), dc.getDocument().getString("mesa"), dc.getDocument().getString("estado"), (ArrayList) dc.getDocument().get("productos"), dc.getDocument().getReference(), dc.getDocument().getString("area"), dc.getDocument().getString("mensaje")));
                                        } else {
                                            Comanda comanda=new Comanda(dc.getDocument().getString("mesero"), dc.getDocument().getString("cliente"), dc.getDocument().getString("mesa"), dc.getDocument().getString("estado"), (ArrayList) dc.getDocument().get("productos"), dc.getDocument().getReference(), dc.getDocument().getString("area"), dc.getDocument().getString("mensaje"));
                                            //adapterComandasCocina.add(comanda);
                                            adapterCocinaPrueba.add(comanda);
                                            if (ComandasPruebaFragment.lecturaAuto) {
                                                String texto="Comanda entrante... Mesa "+comanda.getMesa()+"... ";
                                                for(ProductoOrdenado producto:comanda.getProductoOrdenados()){
                                                        texto+=getLectura(producto);
                                                }
                                                if(ttsManager.getTextToSpeech().isSpeaking()){
                                                    ttsManager.addQueue(texto);
                                                }
                                                else{
                                                    ttsManager.initQueue(texto);
                                                }
                                            }
                                            else {
                                                entrante.start();
                                            }
                                        }
                                    }
                                    break;
                                case MODIFIED:
                                    if (dc.getDocument().getString("area").equals(area)) {
                                        adapterCocinaPrueba.actualizar(dc);
                                        Comanda comandaAuxiliar=new Comanda(dc.getDocument().getString("mesero"), dc.getDocument().getString("cliente"), dc.getDocument().getString("mesa"), dc.getDocument().getString("estado"), (ArrayList) dc.getDocument().get("productos"), dc.getDocument().getReference(), dc.getDocument().getString("area"), dc.getDocument().getString("mensaje"));
                                        if(dc.getDocument().getString("estado").equals("corregida")){
                                            if(ComandasPruebaFragment.lecturaAuto){
                                                String texto="Comanda corregida... Mesa "+comandaAuxiliar.getMesa()+"... ";
                                                for(ProductoOrdenado producto:comandaAuxiliar.getProductoOrdenados()){
                                                    texto+=getLectura(producto);
                                                }
                                                if(ttsManager.getTextToSpeech().isSpeaking()){
                                                    ttsManager.addQueue(texto);
                                                }
                                                else{
                                                    ttsManager.initQueue(texto);
                                                }
                                            }
                                            else{
                                                String texto="Comanda corregida... de la Mesa "+comandaAuxiliar.getMesa();
                                                if(ttsManager.getTextToSpeech().isSpeaking()){
                                                    ttsManager.addQueue(texto);
                                                }
                                                else{
                                                    ttsManager.initQueue(texto);
                                                }
                                            }
                                        }
                                    }
                                    break;
                                case REMOVED:
                                    if (dc.getDocument().getString("area").equals(area)) {
                                        adapterCocinaPrueba.remove(dc);
                                    }
                                    break;
                            }
                        }
                    }
                });
    }

    public void consultarCategorias() {
        firebaseFirestore.collection("Categorias").orderBy("id", Query.Direction.ASCENDING).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                        ArrayList<Platillo> arrayList = new ArrayList<>();
                        ArrayList<String> nombrePlatillos = new ArrayList<>();
                        String areaCat=documentSnapshot.getString("area");
                        for (Map map : (ArrayList<Map>) documentSnapshot.get("productos")) {
                            arrayList.add(new Platillo(map.get("nombre").toString(), map.get("descripcion").toString(), Double.parseDouble(map.get("precio").toString()), (boolean) map.get("existencia"),areaCat));
                            nombrePlatillos.add(map.get("nombre").toString());
                        }
                        adapterCategorias.add(new Categoria(documentSnapshot.getString("nombre"), arrayList, documentSnapshot.getReference(), documentSnapshot.getString("area"), nombrePlatillos, documentSnapshot.getDouble("id").intValue()));
                    }
                } else {
                    showError("Error al consultar la tabla Platillos");
                }
            }
        });
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

    public void showError(String error){
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setMessage(error)
                .setCancelable(false)
                .setNegativeButton("Aceptar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        reiniciar();
                    }
                });
        AlertDialog titulo = alert.create();
        titulo.setTitle("Reiniciar Aplicación");
        titulo.show();
    }

    public void reiniciar(){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
    public MyAdapter getAdapterCocinaPrueba(){
        return adapterCocinaPrueba;
    }

    public void consultarUsuarios(){
        firebaseFirestore.collection("Usuarios").whereNotEqualTo("correo",PerfilFragment.usuario.getCorreo()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    for (QueryDocumentSnapshot documentSnapshot:task.getResult()){
                        AdapterMensajes.users.add(new Usuario(documentSnapshot.getString("nombre"),documentSnapshot.getString("correo"),documentSnapshot.getString("rol"),documentSnapshot.getReference(),documentSnapshot.getString("profileReference")));
                    }
                }
                else {
                    Toast.makeText(getApplicationContext(),"Ocurrio un error al realizar la consulta",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    public void consultarMensajes(){
        firebaseFirestore.collection("Avisos").
                orderBy("fecha", Query.Direction.ASCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @SuppressLint("RestrictedApi")
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshots,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "listen:error", e);
                            return;
                        }
                        for (DocumentChange dc : snapshots.getDocumentChanges()) {
                            switch (dc.getType()) {
                                case ADDED:
                                    adapterMensajes.add(new Mensaje(dc.getDocument().getString("usuario"),dc.getDocument().getString("mensaje"),dc.getDocument().getDate("fecha"),dc.getDocument().getReference()));
                                    break;
                                case MODIFIED:
                                    break;
                                case REMOVED:
                                    adapterMensajes.remove(dc.getDocument().getReference());
                                    break;
                            }
                        }
                    }
                });
    }

    public TTSManager getTtsManager(){
        return ttsManager;
    }

    @Override
    protected void onDestroy() {
        ttsManager.shutDown();
        super.onDestroy();
    }
}