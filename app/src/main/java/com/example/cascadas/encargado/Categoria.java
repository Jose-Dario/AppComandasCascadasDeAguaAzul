package com.example.cascadas.encargado;

import android.net.Uri;

import androidx.annotation.NonNull;

import com.example.cascadas.AlertLoader;
import com.example.cascadas.R;
import com.example.cascadas.Utils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;

import java.util.ArrayList;

public class Categoria{
    private String nombre, area;
    private DocumentReference documentReference;

    private ArrayList<Platillo> productoArrayList;

    private ArrayList<String> nombrePlatillos;
    private Uri direccion;
    private int id;

    public Categoria(String nombre, ArrayList<Platillo> productos, DocumentReference documentReference, String area, ArrayList nombrePlatillos, int id) {
        this.nombre = nombre;
        this.documentReference=documentReference;
        this.area=area;
        productoArrayList=productos;
        productoArrayList.sort(Platillo::compareTo);
        this.nombrePlatillos=nombrePlatillos;
        this.nombrePlatillos.sort(String::compareTo);
        this.id =id;
    }

    public ArrayList<String> getNombrePlatillos(){
        return nombrePlatillos;
    }

    public void setPlatillos(ArrayList<Platillo> platillos){
        this.productoArrayList=platillos;
    }

    public Platillo getPlatillo(int position){
        return productoArrayList.get(position);
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public DocumentReference getDocumentReference(){
        return documentReference;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public int getId(){
        return id;
    }

    public void addProducto(Platillo platillo, AlertLoader alertLoader){
        productoArrayList.add(platillo);
        documentReference.update("productos",productoArrayList).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    alertLoader.dimiss("Producto agregado");
                }
                else {
                    alertLoader.showError("Error al agregar el producto");
                }
            }
        });
    }

    public ArrayList getPlatillos(){
        return productoArrayList;
    }

    public void remove(Platillo platillo){
        productoArrayList.remove(platillo);
    }

    public void updateProductos(AlertLoader alertLoader){
        documentReference.update("productos", productoArrayList).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    alertLoader.dimiss("Información del producto actualizada");
                }
                else {
                    alertLoader.showError("Error al actualizar la información del platillo");
                }
            }
        });
    }

    public String toString(){
        return nombre;
    }

    public Uri getUri(){
        if(direccion!=null){
            return direccion;
        }
        else{
            direccion= Utils.getUri(nombre);
            return direccion;
        }
    }
}
