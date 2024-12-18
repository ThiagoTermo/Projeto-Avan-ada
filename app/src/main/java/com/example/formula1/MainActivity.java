package com.example.formula1;

import com.example.automationlibrary.Calculations;
import com.example.automationlibrary.BancoDeDados;
import com.example.automationlibrary.CarsAttributes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

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
    private Boolean BD;
    private ArrayList<Thread> threads;
    private HashMap<String, Object> carsDB;
    private int qtdCars;
    ExecutorService executor;

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
            BD = false;
            threads = new ArrayList<>();
            carsDB = new HashMap<>();

            ControlButton();

            int numThreads = Runtime.getRuntime().availableProcessors(); // qtd processadores disponíveis
            executor = Executors.newFixedThreadPool(1); // qtd a ser usados

        } catch (Exception e) {
            Log.e("MainActivity", "Erro na inicialização: ", e);
        }

    }

    public void startRace() {
        try {

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

                    Sensor sensorE = car.getSensor(0);
                    Sensor sensorD = car.getSensor(1);
                    Sensor sensorC = car.getSensor(2);

                    int id = car.getIdImage();
                    ImageView vehicle = findViewById(id);

                    boolean isColliding = checkCollision(car, sensorC);

                    if (isColliding) {

                        if(checkCollision(car, sensorD) && !checkCollision(car, sensorE)){
                            car.setSpeed(5);
                            turnLeft(vehicle, car);
                        } else if (!checkCollision(car, sensorD) && checkCollision(car, sensorE)) {
                            car.setSpeed(5);
                            turnRight(vehicle, car);
                        } else
                            car.setSpeed(0);

                    } else {

                        //executor.submit(() -> {
                        //long startTime = System.nanoTime(); // Início da tarefa
                            // Código da tarefa a ser executada

                        boolean onTrackE = isCarOnTrack(sensorE.getX(), sensorE.getY(), car, sensorE);
                        boolean onTrackD = isCarOnTrack(sensorD.getX(), sensorD.getY(), car, sensorD);

                        //long endTime = System.nanoTime();   // Final da tarefa
                        //long executionTime = (endTime - startTime) / 1_000; // Tempo em milissegundos
                        //Log.d("ExecutionTime", "Tempo de processamento: " + executionTime + " us");
                        //});

                        if (onTrackD && onTrackE) {
                            car.setSpeed(car.getLineSpeed());
                        } else if (!onTrackE && onTrackD) {
                            car.setSpeed(5);
                            turnRight(vehicle, car);
                            car.setPenalty(car.getPenalty() + 1);
                        } else if (!onTrackD && onTrackE) {
                            car.setSpeed(5);
                            turnLeft(vehicle, car);
                            car.setPenalty(car.getPenalty() + 1);
                        } else {
                            car.setSpeed(-30);
                        }

                    }

                    moveCarForward(vehicle, car, sensorE, sensorD);

                    updateSensorPositions(car, sensorE, sensorD, sensorC, vehicle.getRotation());

                    // Re-agendar o movimento após um pequeno delay
                    //handler.postDelayed(this, 1000000000);  // Ajuste o delay conforme necessário
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

            int x=0;
            int y=1;

            for (int i = 0; i < qtdCars; i++) {

                //int randomSpeed = (int)(Math.random() * ((18 - 10) + 1)) + 10;

                if (i == 0) {
                    ImageView vehicle = new ImageView(this);

                    // Definindo a imagem do carro
                    vehicle.setImageResource(R.drawable.safetycar);

                    vehicle.setId(i + 1);

                    // Definindo os parâmetros de layout
                    RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(60, 30);

                    // Definindo margens (ajuste as margens conforme necessário)
                    layoutParams.setMargins(800, 720, 0, 0);

                    // Aplicando os parâmetros ao ImageView
                    vehicle.setLayoutParams(layoutParams);

                    vehicle.setRotation(0);

                    // Adicionando o ImageView ao layout
                    carsContainer.addView(vehicle);

                    Car car = new SafetyCar("SafetyCar ", i + 1, 800, 720, 1000, 10, 15, 0, 0, 0, this, semaphore);
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
                    RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(60, 30);

                    if(y==0){
                        y = 45;

                    } else {
                        y = 0;
                        x++;
                    }

                    // Definindo margens (ajuste as margens conforme necessário)
                    layoutParams.setMargins(780 - (x * 60), 700 + y, 0, 0);

                    // Aplicando os parâmetros ao ImageView
                    vehicle.setLayoutParams(layoutParams);

                    vehicle.setRotation(0);

                    // Adicionando o ImageView ao layout
                    carsContainer.addView(vehicle);

                    Car car = new RaceCar("Carro " + (i), i + 1, 780 - (x * 60), 700 + y, 1000, 10, 15,0, 0, 0, this, semaphore);
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
                //if (pixelColor == green && car.getSensor(2) == sensor) {
                //if(adjustedPoint.x >= 1905 && adjustedPoint.x <= 1920 && adjustedPoint.y >= 1700 && adjustedPoint.y <= 1850){
                if(car.getX() >= 780 && car.getX() <= 820 && car.getY() >= 680 && car.getY() <= 800){
                    if(!car.getisPassed()) {
                        car.setLaps(car.getLaps() + 1);
                        Log.d("MainActivity", "Voltas: " + car.getLaps() + " Com o carro: " + car.getName());
                        Log.d("Car" + car.getName(), "Cood X: " + car.getX());
                        Log.d("Car" + car.getName(), "Cood Y: " + car.getY());
                        car.setisPassed(true);
                    } else {
                        //isPassed = false;
                    }
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

            if (car.getX() >= 300 && car.getX() <= 470 && car.getY() >= 275 && car.getY() <= 290) {
                car.setisPassed(false);
                return 1;
            } else if (car.getX() >= 70 && car.getX() <= 240 && car.getY() >= 275 && car.getY() <= 290) {
                return 2;
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
            float carCenterX = car.getX() + (30);
            float carCenterY = car.getY() + (15);

            float sensorOffsetX = 35;
            float sensorOffsetY = -20;

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
        ObjectAnimator turnCar = ObjectAnimator.ofFloat(vehicle, "rotation", vehicle.getRotation(), vehicle.getRotation() + car.getSpeed() + 12);
        turnCar.setDuration(100);
        turnCar.start();
    }

    private void turnLeft(ImageView vehicle, Car car) {
        ObjectAnimator turnCar = ObjectAnimator.ofFloat(vehicle, "rotation", vehicle.getRotation(), vehicle.getRotation() - car.getSpeed() - 12);
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
            BD = false;

            BancoDeDados.clearArray();

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
            carsDB.put("lineSpeed", car.getLineSpeed());
            carsDB.put("Laps", car.getLaps());
            carsDB.put("Distance", car.getDistance());

            BancoDeDados.record(carsDB, car.getName());
        }
    }

    public void read(){
        BancoDeDados.read();

        started = true;

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // O código aqui dentro será executado após 3 segundos

                qtdCars = BancoDeDados.getQtdCars();

                ArrayList<CarsAttributes> carrosBD = BancoDeDados.getArray();

                Semaphore semaphore = new Semaphore(1);

                for(int i=0; i<qtdCars; i++){
                    CarsAttributes carBD = carrosBD.get(i);
                    AddCarsBD(carBD.getName(), carBD.getX(), carBD.getY(), carBD.getRotation(), carBD.getSpeed(), carBD.getLineSpeed(), carBD.getLaps(), carBD.getIdImage(), semaphore);
                }
                BD = true;
            }
        }, 3000); // Delay em milissegundos
    }

    public void AddCarsBD(String name, int x, int y, float rotation, long speed, long lineSpeed, int laps, int idImage, Semaphore semaphore) {
        try {
            if (idImage == 1) {
                    ImageView vehicle = new ImageView(this);

                    // Definindo a imagem do carro
                    vehicle.setImageResource(R.drawable.safetycar);

                    vehicle.setId(idImage);

                    // Definindo os parâmetros de layout
                    RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(60, 30);

                    // Definindo margens (ajuste as margens conforme necessário)
                    layoutParams.setMargins(x, y, 0, 0);

                    // Aplicando os parâmetros ao ImageView
                    vehicle.setLayoutParams(layoutParams);

                    vehicle.setRotation(rotation);

                    // Adicionando o ImageView ao layout
                    carsContainer.addView(vehicle);

                    Car car = new SafetyCar(name, idImage, x, y, 1000, speed, lineSpeed, laps, 0, 0, this, semaphore);
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
                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(60, 30);

                // Definindo margens (ajuste as margens conforme necessário)
                layoutParams.setMargins(x, y, 0, 0);

                // Aplicando os parâmetros ao ImageView
                vehicle.setLayoutParams(layoutParams);

                vehicle.setRotation(rotation);

                // Adicionando o ImageView ao layout
                carsContainer.addView(vehicle);

                Car car = new RaceCar(name, idImage, x, y, 1000, speed, lineSpeed, laps, 0, 0, this, semaphore);
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

                if((!paused || finish) && !BD) {
                    QtdsCars();
                    AddCars();
                    finish = false;
                }

                if(!started || BD) {
                    started = true;
                    startRace();
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
                Toast.makeText(getApplicationContext(), "Jogo carregando", Toast.LENGTH_SHORT).show();
                read();
            }
        });

    }

}

