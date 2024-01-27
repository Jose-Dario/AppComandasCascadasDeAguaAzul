package com.example.cascadas.encargado;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.cascadas.R;
import com.example.cascadas.AlertLoader;
import com.example.cascadas.Utils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AddCategoriaFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AddCategoriaFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private Uri selectedImageUri;
    private ImageView imgCategoria;

    private static final int GALLERY_REQUEST_CODE = 123;

    private View view;
    private EditText nombreCategoria;

    private Spinner spinnerArea;
    private ArrayAdapter arrayAdapter;

    public AddCategoriaFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AddCategoriaFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AddCategoriaFragment newInstance(String param1, String param2) {
        AddCategoriaFragment fragment = new AddCategoriaFragment();
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
        view= inflater.inflate(R.layout.fragment_add_categoria, container, false);
        nombreCategoria=view.findViewById(R.id.etNombreCategoria);
        imgCategoria=view.findViewById(R.id.imgCargarCategoria);
        spinnerArea=view.findViewById(R.id.spinnerArea);
        arrayAdapter= new ArrayAdapter(getContext(), android.R.layout.simple_spinner_item, MainEncargado.getAreas());

        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerArea.setAdapter(arrayAdapter);

        ((ImageButton)view.findViewById(R.id.imgBtnSelectCategoria)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent, GALLERY_REQUEST_CODE);
            }
        });

        ((Button)view.findViewById(R.id.btnRegistrarCategoria)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(nombreCategoria.getText().toString().equals("")){
                    Toast.makeText(getContext(),"Ingrese un nombre de categoria",Toast.LENGTH_SHORT).show();
                }
                else {
                    AlertLoader alertLoader=new AlertLoader(getActivity(),view);
                    if(selectedImageUri!=null){
                        Utils.uploadImageCategoria(nombreCategoria.getText().toString(),selectedImageUri,getContext());
                    }
                    Map categoria=new HashMap();
                    categoria.put("nombre",nombreCategoria.getText().toString());
                    if(PlatillosFragment.adapterCategorias.getUltimaCategoria()!=null)
                    categoria.put("id",PlatillosFragment.adapterCategorias.getUltimaCategoria().getId()+1);
                    else{
                        categoria.put("id",1);
                    }
                    categoria.put("area",spinnerArea.getSelectedItem().toString());
                    categoria.put("productos",new ArrayList<>());
                   FirebaseFirestore.getInstance().collection("Categorias").add(categoria).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentReference> task) {
                            if (task.isSuccessful()){
                                //Toast.makeText(getContext(),"Categoría registrada exitosamente",Toast.LENGTH_SHORT).show();
                               // MainEncargado.notificar("Nueva categoría añadida","Reiniciar aplicación para obtener la nueva información");
                                //PlatillosFragment.adapterCategorias.add(new Categoria(categoria.get("nombre").toString(),new ArrayList<>(), task.getResult(), categoria.get("area").toString(),new ArrayList<>(),PlatillosFragment.adapterCategorias.getUltimo().getId()+1));
                                alertLoader.dimiss("Categoría "+nombreCategoria.getText().toString()+" añadida");
                            }
                            else {
                                alertLoader.showError("Ocurrió un error al registrar la categoría");
                                //Toast.makeText(getContext(),"Ocurrio un error al registrar la categoría",Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });



        return view;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            imgCategoria.setImageURI(selectedImageUri);
        }
    }

}