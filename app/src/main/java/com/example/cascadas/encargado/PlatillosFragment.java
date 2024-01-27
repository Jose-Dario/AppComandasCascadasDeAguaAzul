package com.example.cascadas.encargado;

import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;

import com.example.cascadas.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PlatillosFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PlatillosFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private View view;
    private GridView gridView;

    public static AdapterCategorias adapterCategorias;

    public PlatillosFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment PlatillosFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PlatillosFragment newInstance(String param1, String param2) {
        PlatillosFragment fragment = new PlatillosFragment();
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
        view= inflater.inflate(R.layout.fragment_platillos, container, false);
        gridView=view.findViewById(R.id.contenedorCategorias);
        gridView.setAdapter(adapterCategorias);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                PlatillosCategoriaFragment.categoria=adapterCategorias.getCategoria(i);
                Navigation.findNavController(view).navigate(R.id.nav_platillos_categoria);
            }
        });

        ((Button)view.findViewById(R.id.btnAddCategoria)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(view).navigate(R.id.nav_add_categoria);
            }
        });
        return view;
    }

}