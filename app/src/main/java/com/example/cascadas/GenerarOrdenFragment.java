package com.example.cascadas;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cascadas.encargado.AdapterCategorias;
import com.example.cascadas.encargado.Categoria;
import com.example.cascadas.encargado.Platillo;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class GenerarOrdenFragment extends Fragment implements OnTaskCompleted {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;

    public static Comanda comanda;

    public static boolean docBarra;
    public static boolean docComal;
    private String mParam2;
    private View view;
    public static boolean modificacion;
    public static boolean correcion = false;
    private ArrayList<ProductoOrdenado> barra, cocina, comal;

    public static String mesa, mesero;
    private int cant = 1;
    private double totalComanda = 0;
    private FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    private ArrayList<ProductoOrdenado> listaProductosOrdenados;
    private LinearLayout linearLayoutProductosOrdenados;
    private Spinner categorias, productos;
    //private TextView cantidad;
    //private EditText descripcion;

    private AutoCompleteTextView buscador;

    private ArrayList<Platillo> resultados;
    private ArrayList<Platillo> milista;

    private ArrayAdapter<Platillo> adaptadorBusqueda;

    public static String cliente;
    public static AdapterCategorias adaptadorCategorias;

    private static SendNotification notification = new SendNotification();

    private Button realizarModificacion, generarOrden, addPlatillosCuenta, dividirTiempos;


    public GenerarOrdenFragment() {
        // Required empty public constructor
    }

    public static GenerarOrdenFragment newInstance(String param1, String param2) {
        GenerarOrdenFragment fragment = new GenerarOrdenFragment();
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
        OnBackPressedCallback callback = new OnBackPressedCallback(true /* enabled by default */) {
            @Override
            public void handleOnBackPressed() {
                if (comanda != null) {
                    AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
                    alert.setMessage("¿Desea descartar los cambios?").setCancelable(false).setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            comanda.getDocumentReference().update("estado", "en espera");
                            Toast.makeText(getContext(), "No se realizaron cambios", Toast.LENGTH_SHORT).show();
                            comanda = null;
                            Navigation.findNavController(view).popBackStack();
                        }
                    }).setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    });
                    AlertDialog titulo = alert.create();
                    titulo.setTitle("Comanda en Modificación");
                    titulo.show();
                } else if (correcion) {
                    AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
                    alert.setMessage("¿Desea salir?, la información se perderá").setCancelable(false).setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Toast.makeText(getContext(), "Correción descartada", Toast.LENGTH_SHORT).show();
                            Navigation.findNavController(view).popBackStack();
                        }
                    }).setNegativeButton("Volver", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    });
                    AlertDialog titulo = alert.create();
                    titulo.setTitle("Cancelar Acción");
                    titulo.show();
                } else {
                    AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
                    alert.setMessage("La Orden no se ha generado, ¿Desea salir?").setCancelable(false).setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Toast.makeText(getContext(), "Comanda descartada", Toast.LENGTH_SHORT).show();
                            Navigation.findNavController(view).popBackStack();
                        }
                    }).setNegativeButton("Volver", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    });
                    AlertDialog titulo = alert.create();
                    titulo.setTitle("Orden no Generada");
                    titulo.show();
                }

            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_generar_orden, container, false);
        buscador = view.findViewById(R.id.buscador);
        resultados = new ArrayList<>();
        milista = new ArrayList<>();
        for (Categoria categoria : adaptadorCategorias.getCategorias()) {
            milista.addAll(categoria.getPlatillos());
        }
        ;

        adaptadorBusqueda = new ArrayAdapter(getActivity(), android.R.layout.simple_dropdown_item_1line, resultados);
        buscador.setAdapter(adaptadorBusqueda);
        buscador.setThreshold(1);
        buscador.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                buscarCoincidencias(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        buscador.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Platillo platilloEncontrado = (Platillo) parent.getItemAtPosition(position);
                agregarProducto(new ProductoOrdenado(platilloEncontrado.getNombre(), platilloEncontrado.getPrecio(), platilloEncontrado.getArea(), 1, ""));
                buscador.setText("");
                buscador.clearFocus();
                ocultarTeclado();
            }
        });

        ((TextView) view.findViewById(R.id.tvTitleMesaGenerarOrden)).setText("Mesa " + mesa);
        ((TextView) view.findViewById(R.id.tvClienteGenerarOrden)).setText(cliente);
        realizarModificacion = view.findViewById(R.id.btnRealizarModificacion);
        addPlatillosCuenta = view.findViewById(R.id.btnAddToCuenta);
        generarOrden = view.findViewById(R.id.btnGenerarOrden);
        dividirTiempos=view.findViewById(R.id.divTiempos);
        listaProductosOrdenados = new ArrayList<>();
        linearLayoutProductosOrdenados = view.findViewById(R.id.contenedorProductosOrdenados);
        categorias = view.findViewById(R.id.spinnerCategoria);
        productos = view.findViewById(R.id.spinnerProducto);
        ArrayAdapter adapterCategorias = new ArrayAdapter(getContext(), android.R.layout.simple_spinner_item, adaptadorCategorias.getNombresCategoria());
        categorias.setAdapter(adapterCategorias);
        adapterCategorias.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        if (modificacion) {
            realizarModificacion.setVisibility(View.VISIBLE);
            generarOrden.setVisibility(View.GONE);
            dividirTiempos.setVisibility(View.GONE);
        }
        if (correcion) {
            addPlatillosCuenta.setVisibility(View.VISIBLE);
            generarOrden.setVisibility(View.GONE);
            dividirTiempos.setVisibility(View.GONE);
        }
        if (comanda != null) {
            for (ProductoOrdenado productoOrdenado : comanda.getProductoOrdenados()) {
                agregarProducto(productoOrdenado);
                totalComanda += productoOrdenado.getCantidad() * productoOrdenado.getPrecio();
            }
            dividirTiempos.setVisibility(View.GONE);
        }

        categorias.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @SuppressLint("MissingInflatedId")
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (categorias.getSelectedItem().toString().equals("+ Nuevo producto")) {
                    View dialogView = getLayoutInflater().inflate(R.layout.card_new_producto, null);
                    @SuppressLint({"MissingInflatedId", "LocalSuppress"}) EditText iProducto = dialogView.findViewById(R.id.iNewProducto);
                    @SuppressLint({"MissingInflatedId", "LocalSuppress"}) EditText iDescripcion = dialogView.findViewById(R.id.idescripcion);
                    CheckBox checkBarra=dialogView.findViewById(R.id.selectBarra);
                    CheckBox checkCocina=dialogView.findViewById(R.id.selectCocina);
                    CheckBox checkCocinaCh=dialogView.findViewById(R.id.selectCocinaCH);
                    @SuppressLint({"MissingInflatedId", "LocalSuppress"}) TextView cantidad = dialogView.findViewById(R.id.sCant);
                    ((Button) dialogView.findViewById(R.id.bdecrement)).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (!cantidad.getText().toString().equals("1")) {
                                cantidad.setText((Integer.parseInt(cantidad.getText().toString()) - 1) + "");
                            }
                        }
                    });
                    ((Button) dialogView.findViewById(R.id.bincrement)).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            cantidad.setText("" + (Integer.parseInt(cantidad.getText().toString()) + 1));
                        }
                    });
                    EditText precio = dialogView.findViewById(R.id.iPrecio);
                    ((Button) dialogView.findViewById(R.id.bAgregarNuevo)).setOnClickListener(new View.OnClickListener() {
                        @SuppressLint("NonConstantResourceId")
                        @Override
                        public void onClick(View v) {
                            if (iProducto.getText().toString().equals("")) {
                                Toast.makeText(getContext(), "Ingrese nombre del producto", Toast.LENGTH_SHORT).show();
                                iProducto.requestFocus();
                            } else if (precio.getText().toString().equals("")) {
                                Toast.makeText(getContext(), "Ingrese el costo del platillo", Toast.LENGTH_SHORT).show();
                                precio.requestFocus();
                            } else {
                                if(checkBarra.isChecked() || checkCocina.isChecked() || checkCocinaCh.isChecked()){
                                    if(checkBarra.isChecked()){
                                        agregarProducto(new ProductoOrdenado(iProducto.getText().toString(), Double.parseDouble(precio.getText().toString()), "barra", Integer.parseInt(cantidad.getText().toString()), iDescripcion.getText().toString()));
                                        if(checkCocina.isChecked()){
                                            agregarProducto(new ProductoOrdenado(iProducto.getText().toString(), 0, "cocina", Integer.parseInt(cantidad.getText().toString()), iDescripcion.getText().toString()));
                                        }
                                        if(checkCocinaCh.isChecked()){
                                            agregarProducto(new ProductoOrdenado(iProducto.getText().toString(), 0, "cocina chica", Integer.parseInt(cantidad.getText().toString()), iDescripcion.getText().toString()));
                                        }
                                    }
                                    else if(checkCocina.isChecked()){
                                        agregarProducto(new ProductoOrdenado(iProducto.getText().toString(), Double.parseDouble(precio.getText().toString()), "cocina", Integer.parseInt(cantidad.getText().toString()), iDescripcion.getText().toString()));
                                        if(checkCocinaCh.isChecked()){
                                            agregarProducto(new ProductoOrdenado(iProducto.getText().toString(), 0, "cocina chica", Integer.parseInt(cantidad.getText().toString()), iDescripcion.getText().toString()));
                                        }
                                    }
                                    else{
                                        agregarProducto(new ProductoOrdenado(iProducto.getText().toString(), Double.parseDouble(precio.getText().toString()), "cocina chica", Integer.parseInt(cantidad.getText().toString()), iDescripcion.getText().toString()));
                                    }
                                    //agregarProducto(new ProductoOrdenado(iProducto.getText().toString(), Double.parseDouble(precio.getText().toString()), area, Integer.parseInt(cantidad.getText().toString()), iDescripcion.getText().toString()));
                                    Toast.makeText(getContext(), "Producto añadido", Toast.LENGTH_SHORT).show();
                                    iProducto.setText("");
                                    iProducto.clearFocus();
                                    precio.setText("");
                                    precio.clearFocus();
                                    cantidad.setText("1");
                                    iDescripcion.setText("");
                                    iDescripcion.clearFocus();
                                }
                               else{
                                   Toast.makeText(getContext(),"Seleccionar el área",Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    });

                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setCancelable(false);
                    builder.setView(dialogView).setNegativeButton("Cerrar", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            categorias.setSelection(0);
                        }
                    });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }

                else {
                    ArrayList<Platillo> platillosList = new ArrayList<>();
                    platillosList.add(new Platillo("Seleccione un producto", "", 0, true, ""));
                    platillosList.addAll(adaptadorCategorias.getCategoria(categorias.getSelectedItemPosition()).getPlatillos());
                    ArrayAdapter adapterPlatillos = new ArrayAdapter(getContext(), android.R.layout.simple_spinner_item, platillosList);
                    adapterPlatillos.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    productos.setAdapter(adapterPlatillos);
                    productos.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            if (position != 0) {
                                Platillo platillo = (Platillo) adapterPlatillos.getItem(position);
                                if (!platillo.getExistencia()) {
                                    AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
                                    alert.setMessage("El producto seleccionado no se encuentra en existencia").setNegativeButton("Cerrar", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                        }
                                    });
                                    AlertDialog titulo = alert.create();
                                    titulo.setTitle("Producto sin existencia");
                                    titulo.show();
                                } else {
                                    agregarProducto(new ProductoOrdenado(platillo.getNombre(), platillo.getPrecio(), platillo.getArea(), 1, ""));
                                }
                                productos.setSelection(0);
                            }
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {
                        }
                    });
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        generarOrden.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertLoader alert = new AlertLoader(getActivity(), view);
                cocina = new ArrayList<>();
                barra = new ArrayList<>();
                comal = new ArrayList<>();
                cargarProductos();
                if (comanda != null) {
                    if (listaProductosOrdenados.size() == 0) {
                        Toast.makeText(getContext(), "No se puede ingresar una comanda vacía", Toast.LENGTH_SHORT).show();
                        comanda.getDocumentReference().update("estado", "en espera").addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    alert.dimiss("Comanda no modificada y actualizada en estado en espera");
                                } else {
                                    alert.showError(task.getException().toString());
                                }
                            }
                        });
                    } else {
                        if (comanda.getArea().equals("cocina")) {
                            if (cocina.size() == 0) {
                                comanda.getDocumentReference().delete();
                                if (barra.size() > 0) {
                                    firebaseFirestore.collection("Comandas").add(crearComanda("barra", barra)).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentReference> task) {
                                            if (task.isSuccessful()) {
                                                notificarBarra("Nueva comanda", mesero + ": Ingresó una comanda");
                                                if (comal.size() > 0) {
                                                    firebaseFirestore.collection("Comandas").add(crearComanda("cocina chica", comal)).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<DocumentReference> task) {
                                                            if (task.isSuccessful()) {
                                                                notificarCocinaChica("Nueva comanda", mesero + ": Ingresó una comanda");
                                                                alert.dimiss("Orden generada");
                                                            } else {
                                                                alert.showError(task.getException().toString());
                                                            }
                                                        }
                                                    });
                                                } else {
                                                    alert.dimiss("Orden generada");
                                                }
                                            } else {
                                                alert.showError(task.getException().toString());
                                            }
                                        }
                                    });
                                } else {
                                    firebaseFirestore.collection("Comandas").add(crearComanda("cocina chica", comal)).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentReference> task) {
                                            if (task.isSuccessful()) {
                                                notificarCocinaChica("Nueva comanda", mesero + ": Ingresó una comanda");
                                                alert.dimiss("Orden generada");
                                            } else {
                                                alert.showError(task.getException().toString());
                                            }
                                        }
                                    });
                                }

                            } else {
                                comanda.getDocumentReference().update("productos", cocina);
                                comanda.getDocumentReference().update("estado", "corregida").addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            notificarCocina("Comanda corregida", mesero + ": corrigió la comanda");
                                            if (barra.size() > 0) {
                                                firebaseFirestore.collection("Comandas").add(crearComanda("barra", barra)).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<DocumentReference> task) {
                                                        if (task.isSuccessful()) {
                                                            notificarBarra("Nueva comanda", mesero + ": Ingresó una comanda");
                                                            if (comal.size() > 0) {
                                                                firebaseFirestore.collection("Comandas").add(crearComanda("cocina chica", comal)).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<DocumentReference> task) {
                                                                        if (task.isSuccessful()) {
                                                                            notificarCocinaChica("Nueva comanda", mesero + ": Ingresó una comanda");
                                                                            alert.dimiss("Orden generada");
                                                                        } else {
                                                                            alert.showError(task.getException().toString());
                                                                        }
                                                                    }
                                                                });
                                                            } else {
                                                                alert.dimiss("Orden generada");
                                                            }
                                                        } else {
                                                            alert.showError(task.getException().toString());
                                                        }
                                                    }
                                                });
                                            } else if (comal.size() > 0) {
                                                firebaseFirestore.collection("Comandas").add(crearComanda("cocina chica", comal)).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<DocumentReference> task) {
                                                        if (task.isSuccessful()) {
                                                            notificarCocinaChica("Nueva comanda", mesero + ": Ingresó una comanda");
                                                            alert.dimiss("Orden generada");
                                                        } else {
                                                            alert.showError(task.getException().toString());
                                                        }
                                                    }
                                                });
                                            } else {
                                                alert.dimiss("Comanda corregida");
                                            }
                                        } else {
                                            alert.showError(task.getException().toString());
                                        }
                                    }
                                });
                            }
                        } else if (comanda.getArea().equals("barra")) {
                            if (barra.size() == 0) {
                                comanda.getDocumentReference().delete();
                                if (cocina.size() > 0) {
                                    firebaseFirestore.collection("Comandas").add(crearComanda("cocina", cocina)).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentReference> task) {
                                            if (task.isSuccessful()) {
                                                notificarCocina("Nueva comanda", mesero + ": Ingresó una comanda");
                                                if (comal.size() > 0) {
                                                    firebaseFirestore.collection("Comandas").add(crearComanda("cocina chica", comal)).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<DocumentReference> task) {
                                                            if (task.isSuccessful()) {
                                                                notificarCocinaChica("Nueva comanda", mesero + ": Ingresó una comanda");
                                                                alert.dimiss("Orden generada");
                                                            } else {
                                                                alert.showError(task.getException().toString());
                                                            }
                                                        }
                                                    });
                                                } else {
                                                    alert.dimiss("Orden generada");
                                                }
                                            } else {
                                                alert.showError("Ocurrio un error al ingresar la orden");
                                            }
                                        }
                                    });
                                } else {
                                    firebaseFirestore.collection("Comandas").add(crearComanda("cocina chica", comal)).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentReference> task) {
                                            if (task.isSuccessful()) {
                                                notificarCocinaChica("Nueva comanda", mesero + ": Ingresó una comanda");
                                                alert.dimiss("Orden generada");
                                            } else {
                                                alert.showError(task.getException().toString());
                                            }
                                        }
                                    });
                                }

                            } else {
                                comanda.getDocumentReference().update("productos", barra);
                                comanda.getDocumentReference().update("estado", "corregida").addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            notificarBarra("Comanda corregida", mesero + ": corrigió la comanda");
                                            if (cocina.size() > 0) {
                                                firebaseFirestore.collection("Comandas").add(crearComanda("cocina", cocina)).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<DocumentReference> task) {
                                                        if (task.isSuccessful()) {
                                                            notificarCocina("Nueva comanda", mesero + ": Ingresó una comanda");
                                                            if (comal.size() > 0) {
                                                                firebaseFirestore.collection("Comandas").add(crearComanda("cocina chica", comal)).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<DocumentReference> task) {
                                                                        if (task.isSuccessful()) {
                                                                            notificarCocinaChica("Nueva comanda", mesero + ": Ingresó una comanda");
                                                                            alert.dimiss("Orden generada");
                                                                        } else {
                                                                            alert.showError(task.getException().toString());
                                                                        }
                                                                    }
                                                                });
                                                            } else alert.dimiss("Orden generada");
                                                        } else {
                                                            alert.showError("Ocurrio un error al ingresar la orden");
                                                        }
                                                    }
                                                });
                                            } else if (comal.size() > 0) {
                                                firebaseFirestore.collection("Comandas").add(crearComanda("cocina chica", comal)).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<DocumentReference> task) {
                                                        if (task.isSuccessful()) {
                                                            notificarCocinaChica("Nueva comanda", mesero + ": Ingresó una comanda");
                                                            alert.dimiss("Orden generada");
                                                        } else {
                                                            alert.showError(task.getException().toString());
                                                        }
                                                    }
                                                });
                                            } else alert.dimiss("Orden generada");
                                        } else {
                                            alert.showError(task.getException().toString());
                                        }
                                    }
                                });
                            }
                        } else {
                            if (comal.size() == 0) {
                                comanda.getDocumentReference().delete();
                                if (cocina.size() > 0) {
                                    firebaseFirestore.collection("Comandas").add(crearComanda("cocina", cocina)).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentReference> task) {
                                            if (task.isSuccessful()) {
                                                notificarCocina("Nueva comanda", mesero + ": Ingresó una comanda");
                                                if (barra.size() > 0) {
                                                    firebaseFirestore.collection("Comandas").add(crearComanda("barra", barra)).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<DocumentReference> task) {
                                                            if (task.isSuccessful()) {
                                                                notificarBarra("Nueva comanda", mesero + ": Ingresó una comanda");
                                                                alert.dimiss("Orden generada");
                                                            } else {
                                                                alert.showError(task.getException().toString());
                                                            }
                                                        }
                                                    });
                                                } else {
                                                    alert.dimiss("Orden generada");
                                                }
                                            } else {
                                                alert.showError("Ocurrio un error al ingresar la orden");
                                            }
                                        }
                                    });
                                } else {
                                    firebaseFirestore.collection("Comandas").add(crearComanda("barra", barra)).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentReference> task) {
                                            if (task.isSuccessful()) {
                                                notificarBarra("Nueva comanda", mesero + ": Ingresó una comanda");
                                                alert.dimiss("Orden generada");
                                            } else {
                                                alert.showError(task.getException().toString());
                                            }
                                        }
                                    });
                                }

                            } else {
                                comanda.getDocumentReference().update("productos", comal);
                                comanda.getDocumentReference().update("estado", "corregida").addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            notificarCocinaChica("Comanda corregida", mesero + ": corrigió la comanda");
                                            if (cocina.size() > 0) {
                                                firebaseFirestore.collection("Comandas").add(crearComanda("cocina", cocina)).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<DocumentReference> task) {
                                                        if (task.isSuccessful()) {
                                                            notificarCocina("Nueva comanda", mesero + ": Ingresó una comanda");
                                                            if (barra.size() > 0) {
                                                                firebaseFirestore.collection("Comandas").add(crearComanda("barra", barra)).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<DocumentReference> task) {
                                                                        if (task.isSuccessful()) {
                                                                            notificarBarra("Nueva comanda", mesero + ": Ingresó una comanda");
                                                                            alert.dimiss("Orden generada");
                                                                        } else {
                                                                            alert.showError(task.getException().toString());
                                                                        }
                                                                    }
                                                                });
                                                            } else alert.dimiss("Orden generada");
                                                        } else {
                                                            alert.showError("Ocurrio un error al ingresar la orden");
                                                        }
                                                    }
                                                });
                                            } else if (barra.size() > 0) {
                                                firebaseFirestore.collection("Comandas").add(crearComanda("barra", barra)).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<DocumentReference> task) {
                                                        if (task.isSuccessful()) {
                                                            notificarBarra("Nueva comanda", mesero + ": Ingresó una comanda");
                                                            alert.dimiss("Orden generada");
                                                        } else {
                                                            alert.showError(task.getException().toString());
                                                        }
                                                    }
                                                });
                                            } else alert.dimiss("Orden generada");

                                        } else {
                                            alert.showError(task.getException().toString());
                                        }
                                    }
                                });
                            }
                        }
                    }
                    comanda = null;
                    totalComanda = 0;
                } else {
                    if (listaProductosOrdenados.size() > 0) {
                        if (cocina.size() > 0) {
                            firebaseFirestore.collection("Comandas").add(crearComanda("cocina", cocina)).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentReference> task) {
                                    if (task.isSuccessful()) {
                                        notificarCocina("Nueva comanda", mesero + ": Ingresó una comanda");
                                        if (barra.size() > 0) {
                                            firebaseFirestore.collection("Comandas").add(crearComanda("barra", barra)).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                                @Override
                                                public void onComplete(@NonNull Task<DocumentReference> task) {
                                                    if (task.isSuccessful()) {
                                                        notificarBarra("Nueva comanda", mesero + ": Ingresó una comanda");
                                                        if (comal.size() > 0) {
                                                            firebaseFirestore.collection("Comandas").add(crearComanda("cocina chica", comal)).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<DocumentReference> task) {
                                                                    if (task.isSuccessful()) {
                                                                        notificarCocinaChica("Nueva comanda", mesero + ": Ingresó una comanda");
                                                                        alert.dimiss("Orden generada");
                                                                    } else {
                                                                        alert.showError(task.getException().toString());
                                                                    }
                                                                }
                                                            });
                                                        } else {
                                                            alert.dimiss("Orden generada");
                                                        }
                                                    } else {
                                                        alert.showError("Ocurrio un error al ingresar la orden");
                                                    }
                                                }
                                            });

                                        } else if (comal.size() > 0) {
                                            firebaseFirestore.collection("Comandas").add(crearComanda("cocina chica", comal)).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                                @Override
                                                public void onComplete(@NonNull Task<DocumentReference> task) {
                                                    if (task.isSuccessful()) {
                                                        notificarCocinaChica("Nueva comanda", mesero + ": Ingresó una comanda");
                                                        alert.dimiss("Orden generada");
                                                    } else {
                                                        alert.showError(task.getException().toString());
                                                    }
                                                }
                                            });
                                        } else {
                                            alert.dimiss("Orden generada");
                                        }
                                    } else {
                                        alert.showError(task.getException().toString());
                                    }
                                }
                            });

                        } else if (barra.size() > 0) {
                            firebaseFirestore.collection("Comandas").add(crearComanda("barra", barra)).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentReference> task) {
                                    if (task.isSuccessful()) {
                                        notificarBarra("Nueva comanda", mesero + ": Ingresó una comanda");
                                        if (comal.size() > 0) {
                                            firebaseFirestore.collection("Comandas").add(crearComanda("cocina chica", comal)).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                                @Override
                                                public void onComplete(@NonNull Task<DocumentReference> task) {
                                                    if (task.isSuccessful()) {
                                                        notificarCocinaChica("Nueva comanda", mesero + ": Ingresó una comanda");
                                                        alert.dimiss("Orden generada");
                                                    } else {
                                                        alert.showError(task.getException().toString());
                                                    }
                                                }
                                            });
                                        } else {
                                            alert.dimiss("Orden generada");
                                        }
                                    } else {
                                        alert.showError("Ocurrio un error al ingresar la orden");
                                    }
                                }
                            });
                        } else {
                            firebaseFirestore.collection("Comandas").add(crearComanda("cocina chica", comal)).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentReference> task) {
                                    if (task.isSuccessful()) {
                                        notificarCocinaChica("Nueva comanda", mesero + ": Ingresó una comanda");
                                        alert.dimiss("Orden generada");
                                    } else {
                                        alert.showError(task.getException().toString());
                                    }
                                }
                            });
                        }
                    } else {
                        alert.showError("No se ordenaron productos");
                        Navigation.findNavController(view).popBackStack();
                    }
                }

            }
        });

//        ((ImageButton) view.findViewById(R.id.btnShowPlatillo)).setOnClickListener(new View.OnClickListener() {
//            @SuppressLint("MissingInflatedId")
//            @Override
//            public void onClick(View v) {
//                if (productos.getCount() == 0 || categorias.getSelectedItemPosition() == categorias.getCount() - 1) {
//                    Toast.makeText(getContext(), "No se ha seleccionado ningún producto", Toast.LENGTH_SHORT).show();
//                } else {
//                    AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
//                    LayoutInflater inflater = getActivity().getLayoutInflater();
//                    View dialogView = inflater.inflate(R.layout.card_detalle_platillo, null);
//                    Platillo aux = adaptadorCategorias.getCategoria(categorias.getSelectedItemPosition()).getPlatillo(productos.getSelectedItemPosition() - 1);
//                    ((ImageView) dialogView.findViewById(R.id.imgPlatilloDetalle)).setImageURI(aux.getUri());
//                    ((TextView) dialogView.findViewById(R.id.platilloDetalle)).setText(aux.getNombre());
//                    ((TextView) dialogView.findViewById(R.id.descripcionPlatilloDetalle)).setText(aux.getDescripcion());
//
//                    // Crea el AlertDialog
//                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
//                    builder.setView(dialogView)
//                            .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialog, int which) {
//                                    // Acciones a realizar cuando se hace clic en Aceptar
//                                }
//                            });
//                    AlertDialog alertDialog = builder.create();
//                    alertDialog.show();
//                }
//            }
//        });

        ((Button) view.findViewById(R.id.btnRealizarModificacion)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertLoader alertLoader1 = new AlertLoader(getActivity(), view);
                if (MesasFragment.adapterMesas.getMesa(mesa).getCliente(cliente) != null) {
                    comanda.getDocumentReference().update("productos", listaProductosOrdenados).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                comanda = null;
                                alertLoader1.dimiss("Modificación realizada");
                            } else {
                                alertLoader1.showError("Error al realizar la modificación");
                            }
                        }
                    });
                } else {
                    alertLoader1.dimiss("El cliente no se encuentra registrado");
                }
            }
        });

        addPlatillosCuenta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertLoader alertLoader1 = new AlertLoader(getActivity(), view);
                if (MesasFragment.adapterMesas.getMesa(mesa).getCliente(cliente) != null) {
                    Map comanda = new HashMap();
                    comanda.put("cliente", cliente);
                    comanda.put("mesa", mesa);
                    comanda.put("mesero", mesero);
                    comanda.put("productos", listaProductosOrdenados);
                    comanda.put("area", "correciones");
                    comanda.put("estado", "entregado");
                    comanda.put("fecha", new Date());
                    comanda.put("mensaje", "mensaje");
                    firebaseFirestore.collection("Comandas").add(comanda).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentReference> task) {
                            if (task.isSuccessful()) {
                                alertLoader1.dimiss("Productos añadidos a la cuenta");
                            } else {
                                alertLoader1.showError(task.getException().toString());
                            }
                        }
                    });

                } else {
                    alertLoader1.dimiss("El cliente no se encuentra registrado");
                }
            }
        });

        dividirTiempos.setOnClickListener(new View.OnClickListener() {
            @SuppressLint({"MissingInflatedId", "SuspiciousIndentation"})
            @Override
            public void onClick(View v) {
                if(listaProductosOrdenados.size()>0){
                    View dialogView = getLayoutInflater().inflate(R.layout.card_dividir_tiempos, null);
                    @SuppressLint({"MissingInflatedId", "LocalSuppress"}) LinearLayout contenedorDividir=dialogView.findViewById(R.id.contenedorDividir);
                    @SuppressLint({"MissingInflatedId", "LocalSuppress"}) Button generarDivision=dialogView.findViewById(R.id.generarOrdenDiv);

                    for (ProductoOrdenado productoOrdenado:listaProductosOrdenados){
                        View cardP=getLayoutInflater().inflate(R.layout.card_prod_div_tiempo, null);
                        ((TextView)cardP.findViewById(R.id.tvCantProductDiv)).setText(productoOrdenado.getCantidad()+"");
                        ((TextView)cardP.findViewById(R.id.tvProductOrdenDiv)).setText(productoOrdenado.getProducto());
                        if(!productoOrdenado.getDescripcion().equals(""))
                        ((TextView)cardP.findViewById(R.id.tvDescripProductDiv)).setText(productoOrdenado.getDescripcion());
                        contenedorDividir.addView(cardP);
                        productoOrdenado.setTiempo("primero");
                        if(productoOrdenado.getArea().equals("barrra")){
                            ((RadioGroup)cardP.findViewById(R.id.radioTiempo)).setVisibility(View.GONE);
                        }
                        ((RadioButton)cardP.findViewById(R.id.radioPrimero)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                if(isChecked){
                                    productoOrdenado.setTiempo("primero");
                                }
                                else{
                                    productoOrdenado.setTiempo("segundo");
                                }
                            }
                        });
                    }
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setCancelable(true);
                    builder.setView(dialogView);
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                    generarDivision.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            AlertLoader alertGen=new AlertLoader(getActivity(),view);
                            ArrayList<ProductoOrdenado> primerTiempo=new ArrayList<>();
                            ArrayList<ProductoOrdenado> segundoTiempo=new ArrayList<>();
                            ArrayList<ProductoOrdenado> newBarra=new ArrayList<>();
                            for (ProductoOrdenado productoOrdenado:listaProductosOrdenados){
                                if(productoOrdenado.getArea().equals("barra")){
                                    newBarra.add(productoOrdenado);
                                }
                                else if(productoOrdenado.getTiempo().equals("primero")){
                                    primerTiempo.add(productoOrdenado);
                                }
                                else{
                                    segundoTiempo.add(productoOrdenado);
                                }
                            }
                            if(primerTiempo.size()==0 || segundoTiempo.size()==0){
                                Toast.makeText(getContext(),"Genere la orden sin dividir tiempos",Toast.LENGTH_SHORT).show();
                                alertDialog.dismiss();
                                alertGen.finalizarLoader();
                            }else{
                                listaProductosOrdenados=new ArrayList<>();
                                linearLayoutProductosOrdenados.removeAllViews();
                                firebaseFirestore.collection("Comandas").add(crearComanda("barra",newBarra));
                                if(!docComal){
                                    primerTiempo.get(primerTiempo.size()-1).setDescripcion(primerTiempo.get(primerTiempo.size()-1).getDescripcion()+". primer tiempo");
                                    segundoTiempo.get(segundoTiempo.size()-1).setDescripcion(segundoTiempo.get(segundoTiempo.size()-1).getDescripcion()+". segundo tiempo");
                                    firebaseFirestore.collection("Comandas").add(crearComanda("cocina",primerTiempo)).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentReference> task) {
                                            if(task.isSuccessful()){
                                                firebaseFirestore.collection("Comandas").add(crearComanda("cocina",segundoTiempo)).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<DocumentReference> task) {
                                                        if(task.isSuccessful()){
                                                            alertGen.dimiss("Orden generada");
                                                        }
                                                        else {
                                                            alertGen.showError(task.getException().toString());
                                                        }
                                                    }
                                                });
                                            }
                                            else {
                                                alertGen.showError(task.getException().toString());
                                            }
                                        }
                                    });
                                }
                                else{
                                    ArrayList<ProductoOrdenado> newCocina=new ArrayList<>();
                                    ArrayList<ProductoOrdenado> newCocinaChica=new ArrayList<>();
                                    for(ProductoOrdenado productoOrdenado:primerTiempo){
                                        if(productoOrdenado.getArea().equals("cocina")){
                                            newCocina.add(productoOrdenado);
                                        }
                                        else{
                                            newCocinaChica.add(productoOrdenado);
                                        }
                                    }
                                    if(newCocina.size()>0){
                                        newCocina.get(newCocina.size()-1).setDescripcion(newCocina.get(newCocina.size()-1).getDescripcion()+". primer tiempo");
                                        firebaseFirestore.collection("Comandas").add(crearComanda("cocina",newCocina));
                                    }
                                    if(newCocinaChica.size()>0){
                                        newCocinaChica.get(newCocinaChica.size()-1).setDescripcion(newCocinaChica.get(newCocinaChica.size()-1).getDescripcion()+". primer tiempo");
                                        firebaseFirestore.collection("Comandas").add(crearComanda("cocina chica",newCocinaChica));

                                    }
                                    newCocina=new ArrayList<>();
                                    newCocinaChica=new ArrayList<>();

                                    for(ProductoOrdenado productoOrdenado:segundoTiempo){
                                        if(productoOrdenado.getArea().equals("cocina")){
                                            newCocina.add(productoOrdenado);
                                        }
                                        else{
                                            newCocinaChica.add(productoOrdenado);
                                        }
                                    }
                                    if(cocina.size()>0){
                                        newCocina.get(newCocina.size()-1).setDescripcion(newCocina.get(newCocina.size()-1).getDescripcion()+". segundo tiempo");
                                        ArrayList<ProductoOrdenado> finalNewCocinaChica = newCocinaChica;
                                        firebaseFirestore.collection("Comandas").add(crearComanda("cocina",newCocina)).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentReference> task) {
                                                if(task.isSuccessful()){
                                                    if(finalNewCocinaChica.size()>0){
                                                        finalNewCocinaChica.get(finalNewCocinaChica.size()-1).setDescripcion(finalNewCocinaChica.get(finalNewCocinaChica.size()-1).getDescripcion()+". segundo tiempo");
                                                        firebaseFirestore.collection("Comandas").add(crearComanda("cocina chica",finalNewCocinaChica)).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<DocumentReference> task) {
                                                                if(task.isSuccessful()){
                                                                    alertGen.dimiss("Orden generada");
                                                                }
                                                                else{
                                                                    alertGen.showError(task.getException().toString());
                                                                }
                                                            }
                                                        });
                                                    }
                                                    else{
                                                        alertGen.dimiss("Orden generada");
                                                    }
                                                }
                                                else{
                                                    alertGen.showError(task.getException().toString());
                                                }
                                            }
                                        });

                                    }
                                    else{
                                        newCocinaChica.get(newCocinaChica.size()-1).setDescripcion(newCocinaChica.get(newCocinaChica.size()-1).getDescripcion()+". segundo tiempo");
                                        firebaseFirestore.collection("Comandas").add(crearComanda("cocina chica",newCocinaChica)).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentReference> task) {
                                                if(task.isSuccessful()){
                                                    alertGen.dimiss("Orden generada");
                                                }
                                                else{
                                                    alertGen.showError(task.getException().toString());
                                                }
                                            }
                                        });
                                    }
                                }
                                alertDialog.dismiss();
                            }
                        }
                    });


                }
                else{
                    Toast.makeText(getContext(),"No hay productos ordenados",Toast.LENGTH_SHORT).show();
                }
            }
        });
        return view;
    }

    @SuppressLint("MissingInflatedId")
    public void agregarProducto(ProductoOrdenado productoOrdenado) {
        listaProductosOrdenados.add(productoOrdenado);
        if(productoOrdenado.getPrecio()==0){
            return;
        }
        View cardProductoOrdenado = getLayoutInflater().inflate(R.layout.card_producto_ordenado, null);
        TextView tvCantidadProducto = cardProductoOrdenado.findViewById(R.id.tvCantProduct);
        tvCantidadProducto.setText("" + productoOrdenado.getCantidad());
        ((TextView) cardProductoOrdenado.findViewById(R.id.tvProductOrden)).setText(productoOrdenado.getProducto());
        TextView descrip = cardProductoOrdenado.findViewById(R.id.tvDescripProduct);
        //Toast.makeText(getContext(),productoOrdenado.getDescripcion(),Toast.LENGTH_SHORT).show();
        if (!productoOrdenado.getDescripcion().equals("")) {
            descrip.setText(productoOrdenado.getDescripcion());
            descrip.setVisibility(View.VISIBLE);
        }
        linearLayoutProductosOrdenados.addView(cardProductoOrdenado);
        ((ImageButton) cardProductoOrdenado.findViewById(R.id.btnMenosProducto)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Integer.parseInt(tvCantidadProducto.getText().toString()) != 1) {
                    productoOrdenado.setCantidad(productoOrdenado.getCantidad() - 1);
                    tvCantidadProducto.setText("" + productoOrdenado.getCantidad());
                } else {
                    Toast.makeText(getContext(), "Ya no se puede disminuir, en caso contrario eliminie", Toast.LENGTH_SHORT).show();
                }
            }
        });
        ((ImageButton) cardProductoOrdenado.findViewById(R.id.btnMasProducto)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                productoOrdenado.setCantidad(productoOrdenado.getCantidad() + 1);
                tvCantidadProducto.setText("" + productoOrdenado.getCantidad());
            }
        });

        ((ImageButton) cardProductoOrdenado.findViewById(R.id.btnRemoveProducto)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
                alert.setMessage("¿Está seguro de eliminar el producto de la orden?").
                        setCancelable(false).setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                linearLayoutProductosOrdenados.removeView(cardProductoOrdenado);
                                listaProductosOrdenados.remove(productoOrdenado);
                                Toast.makeText(getContext(), "Producto eliminado", Toast.LENGTH_SHORT).show();
                            }
                        }).setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        });
                AlertDialog titulo = alert.create();
                titulo.setTitle("Eliminar producto");
                titulo.show();
            }
        });

        cardProductoOrdenado.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View dialogView = getLayoutInflater().inflate(R.layout.card_edit_producto_ordenado, null);
                @SuppressLint({"MissingInflatedId", "LocalSuppress"}) TextView producto = dialogView.findViewById(R.id.productEdit);
                @SuppressLint({"MissingInflatedId", "LocalSuppress"}) EditText iDescripcion = dialogView.findViewById(R.id.etCorreccionProducto);
                @SuppressLint({"MissingInflatedId", "LocalSuppress"}) TextView iPrecio = dialogView.findViewById(R.id.etCorrecionPrecio);

                producto.setText(productoOrdenado.getProducto());
                iDescripcion.setText(productoOrdenado.getDescripcion());
                iPrecio.setHint("" + productoOrdenado.getPrecio());

                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setCancelable(false);

                builder.setView(dialogView).setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                productoOrdenado.setDescripcion(iDescripcion.getText().toString());
                                descrip.setText(productoOrdenado.getDescripcion());
                                descrip.setVisibility(View.VISIBLE);
                                if (!iPrecio.getText().toString().equals("")) {
                                    productoOrdenado.setPrecio(Integer.parseInt(iPrecio.getText().toString()));
                                }
                            }
                        }).
                        setNegativeButton("Cerrar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });
        cant = 1;
    }

    public Map crearComanda(String area, ArrayList<ProductoOrdenado> lista) {
        Map comanda = new HashMap();
        comanda.put("cliente", cliente);
        comanda.put("mesa", mesa);
        comanda.put("mesero", mesero);
        comanda.put("productos", lista);
        comanda.put("area", area);
        comanda.put("estado", "en espera");
        comanda.put("fecha", new Date());
        comanda.put("mensaje", "mensaje");
        return comanda;
    }

    public void notificarCocina(String titulo, String mensaje) {
        firebaseFirestore.collection("Usuarios").whereEqualTo("rol", "Cocinero").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot dc : task.getResult()) {
                        notification.sendMessage(dc.getString("token"), titulo, mensaje);
                    }
                }
            }
        });
    }

    public void notificarCocinaChica(String titulo, String mensaje) {
        firebaseFirestore.collection("Usuarios").whereEqualTo("rol", "Cocinero Aux").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot dc : task.getResult()) {
                        notification.sendMessage(dc.getString("token"), titulo, mensaje);
                    }
                }
            }
        });
    }

    public void notificarBarra(String titulo, String mensaje) {
        firebaseFirestore.collection("Usuarios").whereEqualTo("rol", "Barman").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot dc : task.getResult()) {
                        notification.sendMessage(dc.getString("token"), titulo, mensaje);
                    }
                }
            }
        });
    }

    public boolean endVocal(String palabra) {
        return (palabra.charAt(palabra.length() - 1) == 'a' ||
                palabra.charAt(palabra.length() - 1) == 'e' ||
                palabra.charAt(palabra.length() - 1) == 'i' ||
                palabra.charAt(palabra.length() - 1) == 'o' ||
                palabra.charAt(palabra.length() - 1) == 'u' ||
                palabra.charAt(palabra.length() - 1) == 'A' ||
                palabra.charAt(palabra.length() - 1) == 'E' ||
                palabra.charAt(palabra.length() - 1) == 'I' ||
                palabra.charAt(palabra.length() - 1) == 'O' ||
                palabra.charAt(palabra.length() - 1) == 'U');
    }

    public void cargarProductos() {
        for (ProductoOrdenado producto : listaProductosOrdenados) {
            if (producto.getArea().equalsIgnoreCase("cocina")) {
                cocina.add(producto);
            } else if (producto.getArea().equalsIgnoreCase("barra")) {
                barra.add(producto);
            } else {
                if (docComal) {
                    comal.add(producto);
                } else {
                    cocina.add(producto);
                }
            }
        }
    }

    public void onDestroy() {
        if (comanda != null && !modificacion) {
            Toast.makeText(getContext(), "Comanda no modificada", Toast.LENGTH_SHORT).show();
            comanda.getDocumentReference().update("estado", "en espera");
        }
        correcion = false;
        modificacion = false;
        super.onDestroy();
    }

    private void ocultarTeclado() {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(getContext().INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(buscador.getWindowToken(), 0);
        }
    }

    private void buscarCoincidencias(String filtro) {
        ArrayList<Platillo> coincidencias = new ArrayList<>();
        for (Platillo item : milista) {
            if (item.getNombre().toLowerCase().contains(filtro.toLowerCase())) {
                coincidencias.add(item);
            }
        }
        adaptadorBusqueda.clear();
        adaptadorBusqueda.addAll(coincidencias);
        adaptadorBusqueda.notifyDataSetChanged();
    }


    @Override
    public void onTaskCompleted(String result) {

    }
}