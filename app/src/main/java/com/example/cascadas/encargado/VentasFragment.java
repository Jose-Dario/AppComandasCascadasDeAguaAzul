package com.example.cascadas.encargado;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cascadas.AlertLoader;
import com.example.cascadas.R;
import com.example.cascadas.Utils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link VentasFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class VentasFragment extends Fragment{

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private Date date;

    private AlertLoader alertLoader;

    private View view;

    private TextView ventaTotal,ventaEfectivo,ventaTransferencia;
    private Button imprimirReporte;

    private TextView tvFechaCorte;

    private LinearLayout contendorVentas;

    private double venta=0,efectivo=0,transferencia=0;

    private ArrayList<Venta> ventas;

    public VentasFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment VentasFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static VentasFragment newInstance(String param1, String param2) {
        VentasFragment fragment = new VentasFragment();
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
        view= inflater.inflate(R.layout.fragment_ventas, container, false);
        contendorVentas=view.findViewById(R.id.contendorVentas);
        ventaTotal=view.findViewById(R.id.tvVentaTotal);
        ventaEfectivo=view.findViewById(R.id.tvEfectivo);
        ventaTransferencia=view.findViewById(R.id.tvTransferencia);
        imprimirReporte=view.findViewById(R.id.btnImprimirReporte);
        ventas=new ArrayList<>();
        tvFechaCorte = view.findViewById(R.id.tvFechaVentas);
        Calendar calendar = Calendar.getInstance();
        tvFechaCorte.setText(calendar.get(Calendar.DAY_OF_MONTH) + "/" + calendar.get(Calendar.MONTH) + "/" + calendar.get(Calendar.YEAR));
        consultarVentas(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        ((ImageButton) view.findViewById(R.id.btnDia)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater inflater = getActivity().getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.card_calendar, null);
                @SuppressLint({"MissingInflatedId", "LocalSuppress"}) CalendarView calendarView = dialogView.findViewById(R.id.calendarView);

                // Crea el AlertDialog
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setView(dialogView)
                        //.setTitle("AlertDialog con Diseño Personalizado")
                        .setNegativeButton("Cerrar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Acciones a realizar cuando se hace clic en Aceptar
                            }
                        });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
                calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
                    @Override
                    public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                        tvFechaCorte.setText(dayOfMonth + "/" + (month + 1) + "/" + year);
                        date = new Date(year, month, dayOfMonth);
                        consultarVentas(year, month, dayOfMonth);
                        alertDialog.dismiss();
                    }
                });
            }
        });

        imprimirReporte.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ventas.size()>0){

                }
                else {
                    Toast.makeText(getContext(),"No hay ventas registradas",Toast.LENGTH_SHORT).show();
                }
            }
        });
//        imprimirReporte.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if(ventas.size()>0){
//                    Date fecha=new Date();
//                    String textoImprimir="";
//                    double total=0;
//                    textoImprimir += "[C]Corte\n";
//                    textoImprimir += "[C]Fecha: "+Utils.imprimirFecha(fecha)+"\n\n";
//                    for(Venta aux:ventas){
//                        textoImprimir += "[L]Cliente: "+aux.getCliente() +"\n";
//                        textoImprimir += "[L]Atendió: "+aux.getMesero() +"\n";
//                        textoImprimir += "[L]Mesa: "+aux.getMesa() +"\n";
//                        textoImprimir += "[L]Folio: "+aux.getFolio() +"\n";
//                        textoImprimir += "[L]Consumo: "+String.format("%.2f",aux.getTotal()) +"\n";
//                        textoImprimir += "[L]Forma de pago: "+aux.getFormaPago() +"\n\n";
//                        total+=aux.getTotal();
//                    }
//                    textoImprimir += "[L][L][R]Total: $" + total + "\n";
//                    textoImprimir += "[L]\n";
//                    textoImprimir += "[L]\n";
//                    textoImprimir += "[L]\n";
//                    textoImprimir += "[L]\n";
//                    //System.out.println(textoImprimir);
//                    printTcp(textoImprimir,fecha);
//                }
//                else {
//                    Toast.makeText(getContext(),"No hay ventas registradas",Toast.LENGTH_SHORT).show();
//                }
//            }
//        });
        return view;

    }

    public void removeVenta(DocumentReference documentReference){
        documentReference.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(!task.isSuccessful()){
                    removeVenta(documentReference);
                }
            }
        });
    }



    public void consultarVentas(int year, int month, int day){
        AlertLoader alertLoader=new AlertLoader(getActivity());
        ventas=new ArrayList<>();
        contendorVentas.removeAllViews();
        venta=0;
        efectivo=0;
        transferencia=0;
        ventaTotal.setText("");
        Timestamp inicioDelDia = new Timestamp(new Date(year - 1900, month, day, 0, 0)); // Año, Mes (0-11), Día, Hora, Minuto
        Timestamp finDelDia = new Timestamp(new Date(year - 1900, month, day, 23, 59));
        FirebaseFirestore.getInstance().collection("Ventas")
                .whereGreaterThanOrEqualTo("fecha", inicioDelDia)  // Documentos con fecha mayor o igual al inicio del día
                .whereLessThanOrEqualTo("fecha", finDelDia)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    for (QueryDocumentSnapshot doc:task.getResult()){
                        View v;
                        LayoutInflater layoutInflater=LayoutInflater.from(getContext());
                        v= layoutInflater.inflate(R.layout.card_venta,null);
                        Venta detalleVenta=new Venta(doc.getReference(), doc.getString("mesa"),doc.getString("mesero"),doc.getString("cliente"),doc.getDate("fecha"), doc.getDouble("total"), doc.getString("folio"),(ArrayList<HashMap>) doc.get("productosOrdenados"),doc.getString("modoPago"));
                        ventas.add(detalleVenta);
                        ((TextView)v.findViewById(R.id.tvFechaCorte)).setText(doc.getString("cliente"));
                        ((TextView)v.findViewById(R.id.tvTotalCorte)).setText(doc.getString("mesero"));
                        ((TextView)v.findViewById(R.id.tvMesaCuenta)).setText(doc.getString("mesa"));
                        ((TextView)v.findViewById(R.id.tvTotalMesa)).setText("$ "+doc.getDouble("total"));
                        v.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                DetalleVentaFragment.venta=detalleVenta;
                                Navigation.findNavController(view).navigate(R.id.nav_detalle_venta);
                            }
                        });
                        contendorVentas.addView(v);
                        venta+=doc.getDouble("total");
                        if(detalleVenta.getFormaPago().equalsIgnoreCase("Efectivo")){
                            efectivo+=detalleVenta.getTotal();
                        }
                        else{
                            transferencia+=detalleVenta.getTotal();
                        }
                    }
                    alertLoader.finalizarLoader();
                }
                else{
                    alertLoader.showError(task.getException().toString());
                }
                ventaEfectivo.setText("Efectivo: $ "+efectivo);
                ventaTransferencia.setText("Transferencia: $ "+transferencia);
                ventaTotal.setText("Venta total: $ "+venta);
            }
        });
    }




}