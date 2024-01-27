package com.example.cascadas;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Date;
import java.util.HashMap;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AvisosFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AvisosFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private View view;

    private GridView contenedorMensajes;

    public static AdapterMensajes adapterMensajes;

    private static SendNotification notification=new SendNotification();


    private FirebaseFirestore firebaseFirestore=FirebaseFirestore.getInstance();

    private EditText mensaje;

    public AvisosFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AvisosFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AvisosFragment newInstance(String param1, String param2) {
        AvisosFragment fragment = new AvisosFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view= inflater.inflate(R.layout.fragment_avisos, container, false);
        contenedorMensajes=view.findViewById(R.id.contenedorMensajes);
        contenedorMensajes.setAdapter(adapterMensajes);
        mensaje=view.findViewById(R.id.etWriteMessage);
        ((ImageButton)view.findViewById(R.id.btnSendMessage)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mensaje.getText().toString().equals("")){
                    Toast.makeText(getContext(),"Texto vacío",Toast.LENGTH_SHORT).show();
                }
                else {
                    sendMensaje();
                }
            }
        });
        contenedorMensajes.setSelection(adapterMensajes.getCount()-1);
        return view;
    }

    public void sendMensaje(){
        AlertLoader alertLoader=new AlertLoader(getActivity());
        HashMap mensajeData=new HashMap();
        mensajeData.put("usuario",PerfilFragment.usuario.getNombre());
        mensajeData.put("mensaje",mensaje.getText().toString());
        mensajeData.put("fecha",new Date());
        firebaseFirestore.collection("Avisos").add(mensajeData).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
            @Override
            public void onComplete(@NonNull Task<DocumentReference> task) {
                if(task.isSuccessful()){
                    alertLoader.minimizar("Aviso enviado");
                    notificar();
                    mensaje.setText("");
                    contenedorMensajes.setSelection(adapterMensajes.getCount()-1);
                }
                else {
                    alertLoader.showError("Error al enviar el aviso");
                }

            }
        });
    }

    public void notificar(){
        String contenido=PerfilFragment.usuario.getNombre()+": "+mensaje.getText().toString();
        firebaseFirestore.collection("Usuarios").whereNotEqualTo("nombre",PerfilFragment.usuario.getNombre()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    for (QueryDocumentSnapshot dc:task.getResult()){
                        if(!dc.getString("token").equals("")){
                            notification.sendMessage(dc.getString("token"),"Nuevo aviso",contenido);
                        }
                    }
                }
            }
        });
    }
}