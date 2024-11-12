package com.example.automationlibrary;

import android.util.Log;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;

public class BancoDeDados {

    private static ArrayList<CarsAttributes> cars = new ArrayList<>();

    public static FirebaseFirestore db = FirebaseFirestore.getInstance();
    public static int qtdCars;
    public static String name;
    public static double idImage;
    public static double posX;
    public static double posY;
    public static long speed;
    public static long laps;
    public static double distance;
    public static double rotation;

    public static void record(HashMap carsDB, String name) {

        db.collection("Carros").document(name).set(carsDB).addOnSuccessListener(aVoid -> {
            Log.d("MainActivity", "Dados gravados com sucesso");
        }).addOnFailureListener(e -> {
            Log.d("MainActivity", "Erro ao gravar os dados");

        });
    }

    public interface OnQtdCarsReadListener {
        void onQtdCarsRead(long qtdcar);  // Método que será chamado quando os dados forem lidos
    }

    public static void readQtdCars(OnQtdCarsReadListener listener) {
        DocumentReference docRef = db.collection("Carros").document("Carro 1");

        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    // Extraindo os dados do documento específico
                    long qtdcar = (document.getLong("Quantidade total de carros"));
                    listener.onQtdCarsRead(qtdcar);
                } else {
                    Log.d("MainActivity", "Documento não encontrado!");
                }
            } else {
                Log.w("MainActivity", "Erro ao ler o documento.", task.getException());
            }
        });
    }

    public static void read(){
        readQtdCars(new OnQtdCarsReadListener() {
            @Override
            public void onQtdCarsRead(long qtdcar) {

                qtdCars = (int)qtdcar;

                db.collection("Carros")
                        .orderBy("IdImage")
                        .limit(qtdcar)  // Limita a quantidade de carros
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {

                                    name = document.getString("Name");
                                    idImage = document.getDouble("IdImage");
                                    posX = document.getDouble("PosX");
                                    posY = document.getDouble("PosY");
                                    speed = document.getLong("Speed");
                                    laps = document.getLong("Laps");
                                    distance = document.getDouble("Distance");
                                    rotation = document.getDouble("Rotation");

                                    CarsAttributes car = new CarsAttributes(name, (int) idImage, (int) posX, (int) posY,
                                            speed, (int) laps, (float) distance, (float) rotation);

                                    cars.add(car);

                                }
                            } else {
                                Log.w("MainActivity", "Erro ao ler documentos.", task.getException());
                            }
                        });
            }
        });
    }

    public static int getQtdCars(){
        return qtdCars;
    }

    public static ArrayList getArray(){
        return cars;
    }

    public static void clearArray(){
        cars.clear();
    }

}
