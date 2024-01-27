package com.example.cascadas.mesero;

import static com.firebase.ui.auth.ui.phone.SubmitConfirmationCodeFragment.TAG;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.cascadas.AdapterComandas;
import com.example.cascadas.AdapterMensajes;
import com.example.cascadas.AvisosFragment;
import com.example.cascadas.Comanda;
import com.example.cascadas.ComandasFragment;
import com.example.cascadas.GenerarOrdenFragment;
import com.example.cascadas.MainActivity;
import com.example.cascadas.Mensaje;
import com.example.cascadas.Mesa;
import com.example.cascadas.MesasFragment;
import com.example.cascadas.PerfilFragment;
import com.example.cascadas.R;
import com.example.cascadas.Usuario;
import com.example.cascadas.encargado.AdapterCategorias;
import com.example.cascadas.encargado.AdapterMesas;
import com.example.cascadas.encargado.Categoria;
import com.example.cascadas.encargado.Platillo;
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

import com.example.cascadas.databinding.ActivityHomeMeseroBinding;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainMesero extends AppCompatActivity {

    private ActivityHomeMeseroBinding binding;

    private static final int CODIGO_PERMISO = 123;


    public FirebaseFirestore firebaseFirestore=FirebaseFirestore.getInstance();

    public AdapterComandas adapterComandas;

    public AdapterMesas adapterMesas;
    public AdapterCategorias adapterCategorias;

    public AdapterMensajes adapterMensajes;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        adapterComandas=new AdapterComandas(this);
        ComandasFragment.adapterComandas=adapterComandas;
        adapterMesas=new AdapterMesas(this);
        MesasFragment.adapterMesas=adapterMesas;
        adapterCategorias=new AdapterCategorias(this);
        GenerarOrdenFragment.adaptadorCategorias=adapterCategorias;
        adapterMensajes=new AdapterMensajes(this);
        AvisosFragment.adapterMensajes=adapterMensajes;
        binding = ActivityHomeMeseroBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_avisos,R.id.navigation_notifications)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_home_mesero);
        //NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);
        consultarGeneral();
        consultarMesas();
        consultarComandas();
        consultarCategorias();
        consultarMensajes();
        consultarUsuarios();
        ((FloatingActionButton)findViewById(R.id.btnRestart)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recreate();
            }
        });
    }

    public void consultarComandas(){
        firebaseFirestore.collection("Comandas").
                orderBy("fecha", Query.Direction.ASCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @SuppressLint("RestrictedApi")
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshots,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            showError("Error al consultar las Comandas");
                            Log.w(TAG, "listen:error", e);
                            return;
                        }
                        for (DocumentChange dc : snapshots.getDocumentChanges()) {
                            switch (dc.getType()) {
                                case ADDED:
                                        adapterComandas.add(new Comanda(dc.getDocument().getString("mesero"),dc.getDocument().getString("cliente"),dc.getDocument().getString("mesa"),dc.getDocument().getString("estado"),(ArrayList) dc.getDocument().get("productos"),dc.getDocument().getReference(),dc.getDocument().getString("area"),dc.getDocument().getString("mensaje")));
                                    break;
                                case MODIFIED:
                                    adapterComandas.actualizar(dc);
                                    break;
                                case REMOVED:
                                    adapterComandas.eliminar(dc);
                                    break;
                            }
                        }
                    }
                });
    }

    public void consultarMesas(){
        firebaseFirestore.collection("Mesas").orderBy("idDoc", Query.Direction.ASCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @SuppressLint("RestrictedApi")
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshots,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            showError("Error al consultar la tabla Mesas");
                            return;
                        }
                        for (DocumentChange dc : snapshots.getDocumentChanges()) {
                            switch (dc.getType()) {
                                case ADDED:
                                    adapterMesas.add(new Mesa(dc.getDocument().getString("id"),dc.getDocument().getString("mesero")
                                            ,(ArrayList<HashMap>) dc.getDocument().get("clientes"),dc.getDocument().getString("estado"),dc.getDocument().getReference(),dc.getDocument().getString("cupo")));
                                    break;
                                case MODIFIED:
                                    adapterMesas.actualizar(dc);
                                    break;
                                case REMOVED:
                                    break;
                            }
                        }
                    }
                });
    }

    public void consultarCategorias(){
        firebaseFirestore.collection("Categorias").orderBy("id", Query.Direction.ASCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @SuppressLint("RestrictedApi")
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshots,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            showError("Error al consultar la información de Platillos");
                            Log.w(TAG, "listen:error", e);
                            return;
                        }
                        for (DocumentChange dc : snapshots.getDocumentChanges()) {
                            switch (dc.getType()) {
                                case ADDED:
                                    ArrayList<Platillo> arrayList=new ArrayList<>();
                                    ArrayList<String> nombrePlatillos=new ArrayList<>();
                                    String areaCat=dc.getDocument().getString("area");
                                    for (Map map:(ArrayList<Map>)dc.getDocument().get("productos")){
                                        arrayList.add(new Platillo(map.get("nombre").toString(),map.get("descripcion").toString(),Double.parseDouble(map.get("precio").toString()),(boolean) map.get("existencia"),areaCat));
                                        nombrePlatillos.add(map.get("nombre").toString());
                                    }
                                    adapterCategorias.add(new Categoria(dc.getDocument().getString("nombre"),arrayList, dc.getDocument().getReference(), dc.getDocument().getString("area"),nombrePlatillos,dc.getDocument().getDouble("id").intValue()));
                                    break;
                                case MODIFIED:
                                    adapterCategorias.actualizar(dc);
                                    break;
                                case REMOVED:
                                    break;
                            }
                        }
                    }
                });
    }
    public void consultarGeneral(){
        firebaseFirestore.collection("General").document("barra").addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                GenerarOrdenFragment.docBarra=value.getBoolean("estado");
            }
        });
        firebaseFirestore.collection("General").document("cocinachica").addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                GenerarOrdenFragment.docComal=value.getBoolean("estado");
            }
        });
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

    public void consultarUsuarios(){
        firebaseFirestore.collection("Usuarios").whereNotEqualTo("correo", PerfilFragment.usuario.getCorreo()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
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

    public void reiniciar(){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}