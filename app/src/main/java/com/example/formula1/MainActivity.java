package com.example.formula1;

import com.example.calculationslibrary.Calculations;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicLong;

import android.animation.ObjectAnimator;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.RelativeLayout;
import android.util.Log;

public class MainActivity extends AppCompatActivity {

    private Button startButton;
    private Button pauseButton;
    private Button finishButton;
    private Button recordButton;
    private Button readButton;
    private EditText qtdeCar;
    private ArrayList<Car> cars;
    private RelativeLayout carsContainer;
    private RelativeLayout.LayoutParams params;
    private Bitmap trackBitmap;
    private ImageView trackImage;
    private Handler handler;
    private Boolean paused;
    private Boolean finish;
    private Boolean started;
    private ArrayList<Thread> threads;
    private FirebaseFirestore db;
    private Map<String, Object> carsDB;
    private int qtdCars;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        try {
            setContentView(R.layout.activity_main);

            qtdeCar = findViewById(R.id.QtdCar);
            startButton = findViewById(R.id.bStart);
            pauseButton = findViewById(R.id.bPause);
            finishButton = findViewById(R.id.bFinish);
            recordButton = findViewById(R.id.bRecord);
            readButton = findViewById(R.id.bRead);
            carsContainer = findViewById(R.id.carsContainer);
            trackBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.img_1).copy(Bitmap.Config.ARGB_8888, true);
            handler = new Handler();
            trackImage = findViewById(R.id.track);
            cars = new ArrayList<>();
            started = false;
            paused = false;
            finish = false;
            threads = new ArrayList<>();
            db = FirebaseFirestore.getInstance();
            carsDB = new HashMap<>();

            ControlButton();

        } catch (Exception e) {
            Log.e("MainActivity", "Erro na inicialização: ", e);
        }

    }

    public void startRace() {
        try {
            Log.d("MainActivity", "To tentando criar as tred ");
            for (int i = 0; i < cars.size(); i++) {
                Car car = cars.get(i);
                Thread carThread = new Thread(car);  // Cria a Thread para o Car específico
                threads.add(carThread);

                // Ajuste da prioridade com base em uma condição
                if (i == 0) {
                    carThread.setPriority(10); // Maior prioridade para o primeiro carro
                } else {
                    carThread.setPriority(5); // Prioridade normal para os outros
                }
                Log.d("MainActivity", "To criando as tred ");
                carThread.start();  // Inicia a Thread com a prioridade definida
            }

        } catch (Exception e) {
            Log.e("MainActivity", "Erro ao iniciar a corrida: ", e);
        }

    }

    public void racing(Car car) {
        try {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {

                    //Log.d("MainActivity","Numero do carro: " + car.getName());
                    //Log.d("MainActivity","Numero de voltas: " + car.getLaps());

                    Sensor sensorE = car.getSensor(0);
                    Sensor sensorD = car.getSensor(1);
                    Sensor sensorC = car.getSensor(2);

                    int id = car.getIdImage();
                    ImageView vehicle = findViewById(id);

                    isCarOnTrack(sensorC.getX(), sensorC.getY(), car, sensorC);

                    boolean isColliding = checkCollision(car, sensorC);

                    if (isColliding) {
                        car.setSpeed(0);
                        car.setPenalty(car.getPenalty() + 1);
                    } else {

                        boolean onTrackE = isCarOnTrack(sensorE.getX(), sensorE.getY(), car, sensorE);
                        boolean onTrackD = isCarOnTrack(sensorD.getX(), sensorD.getY(), car, sensorD);

                        if (onTrackD && onTrackE) {
                            car.setSpeed(15);
                        } else if (!onTrackE && onTrackD) {
                            car.setSpeed(7);
                            turnRight(vehicle, car);
                            car.setPenalty(car.getPenalty() + 1);
                        } else if (!onTrackD && onTrackE) {
                            car.setSpeed(7);
                            turnLeft(vehicle, car);
                            car.setPenalty(car.getPenalty() + 1);
                        } else {
                            car.setSpeed(-30);
                        }
                    }
                    moveCarForward(vehicle, car, sensorE, sensorD);
                    updateSensorPositions(car, sensorE, sensorD, sensorC, vehicle.getRotation());


                    // Re-agendar o movimento após um pequeno delay
                    handler.postDelayed(this, 1000000000);  // Ajuste o delay conforme necessário
                }
            }, 100);
        } catch (Exception e) {
            Log.e("MainActivity", "Erro durante a corrida: ", e);
        }
    }

    public void QtdsCars() {
        try {
            String qtdeCarString = qtdeCar.getText().toString();
            int qtdCar;

            if (!qtdeCarString.isEmpty()) {
                qtdCar = Integer.parseInt(qtdeCarString);
            } else {
                Toast.makeText(getApplicationContext(), "Por favor, insira um valor", Toast.LENGTH_SHORT).show();
                qtdCar = 0;
            }
            qtdCars = qtdCar+1;
        } catch (Exception e) {
            Log.e("MainActivity", "Erro ao obter a quantidade de carros: ", e);
        }
    }

    public void AddCars() {
        Semaphore semaphore = new Semaphore(1);

        try {
            for (int i = 0; i < qtdCars; i++) {
                if (i == 0) {
                    ImageView vehicle = new ImageView(this);

                    // Definindo a imagem do carro
                    vehicle.setImageResource(R.drawable.safetycar);

                    vehicle.setId(i + 1);

                    // Definindo os parâmetros de layout
                    RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(120, 50);

                    // Definindo margens (ajuste as margens conforme necessário)
                    layoutParams.setMargins(650, 710, 0, 0);

                    // Aplicando os parâmetros ao ImageView
                    vehicle.setLayoutParams(layoutParams);

                    vehicle.setRotation(0);

                    // Adicionando o ImageView ao layout
                    carsContainer.addView(vehicle);

                    Car car = new SafetyCar("SafetyCar ", i + 1, 650, 710, 1000, 10, 0, 0, 0, this, semaphore);
                    car.addSensors(new Sensor());
                    car.addSensors(new Sensor());
                    car.addSensors(new Sensor());
                    cars.add(car);

                } else {

                    ImageView vehicle = new ImageView(this);

                    // Definindo a imagem do carro
                    vehicle.setImageResource(R.drawable.car1);

                    vehicle.setId(i + 1);

                    // Definindo os parâmetros de layout
                    RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(120, 50);

                    // Definindo margens (ajuste as margens conforme necessário)
                    layoutParams.setMargins(650 - (i * 120), 710, 0, 0);

                    // Aplicando os parâmetros ao ImageView
                    vehicle.setLayoutParams(layoutParams);

                    vehicle.setRotation(0);

                    // Adicionando o ImageView ao layout
                    carsContainer.addView(vehicle);

                    Car car = new RaceCar("Carro " + (i), i + 1, 650 - (i * 120), 710, 1000, 10, 0, 0, 0, this, semaphore);
                    car.addSensors(new Sensor());
                    car.addSensors(new Sensor());
                    car.addSensors(new Sensor());
                    cars.add(car);


                }
            }
        } catch (Exception e) {
            Log.e("MainActivity", "Erro ao adicionar carros: ", e);
        }
    }

    private boolean checkCollision(Car car, Sensor sensor) {
        try {
            for (Car otherCar : cars) {
                if (otherCar != car && otherCar.isCollidingWith(otherCar, sensor)) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            Log.e("MainActivity", "Erro ao checar colisao: ", e);
            return false;
        }
    }

    public Point adjustCoordinates(float layoutX, float layoutY) {
        try {
            // Dimensões da ImageView (onde a pista está sendo exibida)
            float imageViewWidth = trackImage.getWidth();       // 1080px
            float imageViewHeight = trackImage.getHeight();     // 860px

            // Dimensões originais do bitmap da pista
            float bitmapWidth = trackBitmap.getWidth();         // 2560px
            float bitmapHeight = trackBitmap.getHeight();       // 2043px

            return Calculations.adjustCoordinates(layoutX, layoutY, imageViewWidth, imageViewHeight, bitmapWidth, bitmapHeight);

        } catch (Exception e) {
            Log.e("MainActivity", "Erro ao ajustar coordenadas da pista: ", e);
            return null;
        }
    }

    public boolean isCarOnTrack(float x, float y, Car car, Sensor sensor) {
        try {
            Point adjustedPoint = adjustCoordinates(x, y);

            if (adjustedPoint.x < 0 || adjustedPoint.x >= trackBitmap.getWidth() ||
                    adjustedPoint.y < 0 || adjustedPoint.y >= trackBitmap.getHeight()) {
                finish();
                return false;
            }

            int pixelColor = trackBitmap.getPixel(adjustedPoint.x, adjustedPoint.y);

            int green = -14967284;          // -14967284 = Verde em Hexdecimal #FF192D

            if (pixelColor != Color.BLACK) {
                if (pixelColor == green && car.getSensor(2) == sensor) {
                    car.setLaps(car.getLaps() + 1);
                }
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            Log.e("MainActivity", "Erro ao verificar se o carro esta na pista: ", e);
            return false;
        }
    }

    public int verifySemaphore(Car car) {
        try {
            for (int i = 0; i < car.getSizeSensors(); i++) {
                Sensor sensor = car.getSensor(i);
                Point adjustedPoint = adjustCoordinates(sensor.getX(), sensor.getY());

                if (adjustedPoint.x < 0 || adjustedPoint.x >= trackBitmap.getWidth() ||
                        adjustedPoint.y < 0 || adjustedPoint.y >= trackBitmap.getHeight()) {
                    finish();
                    return 0;
                }

                int pixelColor = trackBitmap.getPixel(adjustedPoint.x, adjustedPoint.y);

                int red = -59091;               // -14967284 = Verde em Hexadecimal #FF1B9E0C
                int blue = -16251188;           // -16251188 = Azul em Hexadecimal #FF0806CC

                if (pixelColor == red) {
                    Log.d("MainActivity", "Detectou vermelho: " + car.getIdImage());
                    return 1;
                } else if (pixelColor == blue) {
                    Log.d("MainActivity", "Detectou azul: " + car.getIdImage());
                    return 2;
                }
            }
            return 0;
        } catch (Exception e) {
            Log.e("MainActivity", "Erro ao verificar se o carro esta na zona de semaforo ", e);
            return 0;
        }
    }

    public void updateSensorPositions(Car car, Sensor sensorE, Sensor sensorD, Sensor sensorC, float angle) {
        try {
            // Calcula o centro do carro
            float carCenterX = car.getX() + (60);
            float carCenterY = car.getY() + (25);

            float sensorOffsetX = 65;
            float sensorOffsetY = -30;

            // Calcula a posição do sensor central
            sensorC.setX(Calculations.updateXsensorC(carCenterX, sensorOffsetX, angle));
            sensorC.setY(Calculations.updateYsensorC(carCenterY, sensorOffsetX, angle));

            // Calcula a posição do sensor esquerdo
            sensorE.setX(Calculations.updateXsensorE(carCenterX, sensorOffsetX, sensorOffsetY, angle));
            sensorE.setY(Calculations.updateYsensorE(carCenterY, sensorOffsetX, sensorOffsetY, angle));

            // Calcula a posição do sensor direito
            sensorD.setX(Calculations.updateXsensorD(carCenterX, sensorOffsetX, sensorOffsetY, angle));
            sensorD.setY(Calculations.updateYsensorD(carCenterY, sensorOffsetX, sensorOffsetY, angle));

        } catch (Exception e) {
            Log.e("MainActivity", "Erro ao atualizar a posição dos sensores: ", e);
        }
    }

    private void moveCarForward(ImageView vehicle, Car car, Sensor sensorE, Sensor sensorD) {
        try {
            // Atualiza a posição do carro de forma suave

            float speed = car.getSpeed();

            float angle = vehicle.getRotation(); // Ângulo de rotação do carro em graus

            // Calcula o deslocamento em X e Y baseado na rotação do carro
            float deltaX = Calculations.deltaX(speed, angle);
            float deltaY = Calculations.deltaY(speed, angle);

            // Atualiza a posição do carro de forma suave no eixo X e Y
            ObjectAnimator moveCarX = ObjectAnimator.ofFloat(vehicle, "x", car.getX(), (float) (car.getX() + deltaX));
            ObjectAnimator moveCarY = ObjectAnimator.ofFloat(vehicle, "y", car.getY(), (float) (car.getY() + deltaY));
            moveCarX.setDuration(10);  // Define a duração de cada passo do movimento
            moveCarY.setDuration(10);
            moveCarX.setInterpolator(new LinearInterpolator());
            moveCarY.setInterpolator(new LinearInterpolator());
            moveCarX.start();
            moveCarY.start();

            car.setX(car.getX() + deltaX);
            car.setY(car.getY() + deltaY);
            car.setDistance(car.getDistance() + deltaX + deltaY);

        } catch (Exception e) {
            Log.e("MainActivity", "Erro ao atualizar a posição do carro: ", e);
        }
    }

    private void turnRight(ImageView vehicle, Car car) {
        // Aqui você pode definir a lógica para fazer o carro curvar para a direita
        // Isso pode incluir mover o carro para a direita e ajustar a posição do sensor
        ObjectAnimator turnCar = ObjectAnimator.ofFloat(vehicle, "rotation", vehicle.getRotation(), vehicle.getRotation() + car.getSpeed() + 7);
        turnCar.setDuration(100);
        turnCar.start();

    }

    private void turnLeft(ImageView vehicle, Car car) {
        // Aqui você pode definir a lógica para fazer o carro curvar para a esquerda
        // Isso pode incluir mover o carro para a esquerda e ajustar a posição do sensor
        ObjectAnimator turnCar = ObjectAnimator.ofFloat(vehicle, "rotation", vehicle.getRotation(), vehicle.getRotation() - car.getSpeed() - 7);
        turnCar.setDuration(100);
        turnCar.start();

    }

    public boolean pause() {
        return paused;
    }

    public void finish() {
        try {
            for (Car car : cars) {
                car.setFinish(false);
            }

            for (Thread thread : threads) {
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            handler.removeCallbacksAndMessages(null);
            cars.clear();
            carsContainer.removeViews(11, qtdCars);
            finish = true;
            started = false;

        } catch (Exception e) {
            Log.e("MainActivity", "Erro ao finalizar a corrida: ", e);
        }
    }

    public void record() {

        carsDB.put("Quantidade total de carros", qtdCars);

        for (Car car : cars) {

            ImageView vehicle = findViewById(car.getIdImage());
            carsDB.put("Rotation", vehicle.getRotation());
            carsDB.put("Name", car.getName());
            carsDB.put("IdImage", car.getIdImage());
            carsDB.put("PosX", car.getX());
            carsDB.put("PosY", car.getY());
            carsDB.put("Speed", car.getSpeed());
            carsDB.put("Laps", car.getLaps());
            carsDB.put("Distance", car.getDistance());

            db.collection("Carros").document(car.getName()).set(carsDB).addOnSuccessListener(aVoid -> {
                Log.d("MainActivity", "Dados gravados com sucesso");
            }).addOnFailureListener(e -> {
                Log.d("MainActivity", "Erro ao gravar os dados");

            });
        }
    }

    public interface OnQtdCarsReadListener {
        void onQtdCarsRead(long qtdcar);  // Método que será chamado quando os dados forem lidos
    }

    public void readQtdCars(OnQtdCarsReadListener listener) {
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

    public void read(){
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

                                Semaphore semaphore = new Semaphore(1);

                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    String name = document.getString("Name");
                                    double idImage = document.getDouble("IdImage");
                                    double posX = document.getDouble("PosX");
                                    double posY = document.getDouble("PosY");
                                    double speed = document.getDouble("Speed");
                                    long laps = document.getLong("Laps");
                                    double distance = document.getDouble("Distance");
                                    double rotation = document.getDouble("Rotation");

                                    // Exibindo no log (ou faça outra manipulação conforme necessário)
                                    Log.d("MainActivity", "Carro: " + name);
                                    Log.d("MainActivity", "IdImage: " + idImage);
                                    Log.d("MainActivity", "Posição X: " + posX + ", Posição Y: " + posY);
                                    Log.d("MainActivity", "Velocidade: " + speed);
                                    Log.d("MainActivity", "Voltas: " + laps);
                                    Log.d("MainActivity", "Distância: " + distance);
                                    Log.d("MainActivity", "Rotação: " + rotation);

                                    AddCarsBD((int) posX, (int) posY, (int) rotation, (int) speed, (int) laps, (int) idImage, semaphore);

                                }
                            } else {
                                Log.w("MainActivity", "Erro ao ler documentos.", task.getException());
                            }
                        });
            }
        });
    }

    public void AddCarsBD(int x, int y, int rotation, int speed, int laps, int idImage, Semaphore semaphore) {

        try {
            if (idImage == 1) {
                    ImageView vehicle = new ImageView(this);

                    // Definindo a imagem do carro
                    vehicle.setImageResource(R.drawable.safetycar);

                    vehicle.setId(idImage);

                    // Definindo os parâmetros de layout
                    RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(120, 50);

                    // Definindo margens (ajuste as margens conforme necessário)
                    layoutParams.setMargins(x, y, 0, 0);

                    // Aplicando os parâmetros ao ImageView
                    vehicle.setLayoutParams(layoutParams);

                    vehicle.setRotation(rotation);

                    // Adicionando o ImageView ao layout
                    carsContainer.addView(vehicle);

                    Car car = new SafetyCar("SafetyCar ", idImage, x, y, 1000, speed, laps, 0, 0, this, semaphore);
                    car.addSensors(new Sensor());
                    car.addSensors(new Sensor());
                    car.addSensors(new Sensor());
                    cars.add(car);

                } else {

                ImageView vehicle = new ImageView(this);

                // Definindo a imagem do carro
                vehicle.setImageResource(R.drawable.car1);

                vehicle.setId(idImage);

                // Definindo os parâmetros de layout
                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(120, 50);

                // Definindo margens (ajuste as margens conforme necessário)
                layoutParams.setMargins(x, y, 0, 0);

                // Aplicando os parâmetros ao ImageView
                vehicle.setLayoutParams(layoutParams);

                vehicle.setRotation(rotation);

                // Adicionando o ImageView ao layout
                carsContainer.addView(vehicle);

                Car car = new RaceCar("Carro " + (idImage), idImage, x, y, 1000, speed, laps, 0, 0, this, semaphore);
                car.addSensors(new Sensor());
                car.addSensors(new Sensor());
                car.addSensors(new Sensor());
                cars.add(car);

            }
        } catch (Exception e) {
            Log.e("MainActivity", "Erro ao adicionar carros: ", e);
        }
    }

    public void ControlButton (){
        startButton.setEnabled(true);
        pauseButton.setEnabled(true);
        finishButton.setEnabled(true);

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), "Start", Toast.LENGTH_SHORT).show();

                if(!paused || finish) {
                    QtdsCars();
                    AddCars();
                    finish = false;
                }
                if(!started) {
                    startRace();
                    started = true;
                }

                paused = false;
            }
        });

        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), "Jogo Pausado", Toast.LENGTH_SHORT).show();
                paused = true;
            }
        });

        finishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), "Jogo Finalizado", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), "Jogo gravado", Toast.LENGTH_SHORT).show();
                record();
            }
        });

        readButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), "Jogo carregado", Toast.LENGTH_SHORT).show();
                read();
                started = true;
                Log.d("MainActivity", "To caqqqq ");
                // Exemplo de um delay de 2 segundos (2000 ms)
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // O código aqui dentro será executado após 2 segundos
                        Log.d("MainActivity", "Isso é após o delay de 2 segundos");
                        startRace();
                    }
                }, 4000); // Delay em milissegundos


            }
        });

    }
}
