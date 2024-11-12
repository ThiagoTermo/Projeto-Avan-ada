package com.example.automationlibrary;

import android.util.Log;
import android.widget.ImageView;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Semaphore;

public class BancoDeDados {

    public static FirebaseFirestore db = FirebaseFirestore.getInstance();
    //public static HashMap<String, Object> carBD = new HashMap<>();
    public static ArrayList<HashMap> allCarsBD = new ArrayList();
    public static int qtdCars;
    public static String name;
    public static double idImage;
    public static double posX;
    public static double posY;
    public static double speed;
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
                // Aqui você pode usar o valor de qtdcar
                Log.d("MainActivity", "Quantidade de carros: " + qtdcar);

                qtdCars = (int)qtdcar;

                db.collection("Carros")
                        .orderBy("IdImage")
                        .limit(qtdcar)  // Limita a quantidade de carros
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                int carCount = task.getResult().size();
                                Log.d("MainActivity", "Número total de carros: " + carCount);

                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    name = document.getString("Name");
                                    idImage = document.getDouble("IdImage");
                                    posX = document.getDouble("PosX");
                                    posY = document.getDouble("PosY");
                                    speed = document.getDouble("Speed");
                                    laps = document.getLong("Laps");
                                    distance = document.getDouble("Distance");
                                    rotation = document.getDouble("Rotation");

                                    // Exibindo no log (ou faça outra manipulação conforme necessário)
                                    Log.d("MainActivity", "Carro: " + name);
                                    Log.d("MainActivity", "IdImage: " + idImage);
                                    Log.d("MainActivity", "Posição X: " + posX + ", Posição Y: " + posY);
                                    Log.d("MainActivity", "Velocidade: " + speed);
                                    Log.d("MainActivity", "Voltas: " + laps);
                                    Log.d("MainActivity", "Distância: " + distance);
                                    Log.d("MainActivity", "Rotação: " + rotation);



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
        return allCarsBD;
    }

}
