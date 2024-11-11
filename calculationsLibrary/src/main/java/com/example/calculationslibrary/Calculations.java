package com.example.calculationslibrary;

import android.graphics.Point;
import android.util.Log;

public class Calculations {

    public static Point adjustCoordinates(float layoutX, float layoutY, float imageViewWidth, float imageViewHeight, float bitmapWidth, float bitmapHeight) {
        try {
            // Ajusta as coordenadas do layout para as coordenadas da imagem original
            int adjustedX = (int) ((layoutX / imageViewWidth) * bitmapWidth);
            int adjustedY = (int) ((layoutY / imageViewHeight) * bitmapHeight);

            return new Point(adjustedX, adjustedY);

        } catch (Exception e) {
            Log.e("MainActivity", "Erro ao ajustar coordenadas da pista: ", e);
            return null;
        }
    }

    public static float updateXsensorC(float carCenterX, float sensorOffsetX, float angle){
        return (carCenterX + (float) (sensorOffsetX * Math.cos(Math.toRadians(angle)) - Math.sin(Math.toRadians(angle))));
    }

    public static float updateYsensorC(float carCenterY, float sensorOffsetX, float angle){
        return (carCenterY + (float) (sensorOffsetX * Math.sin(Math.toRadians(angle)) + Math.cos(Math.toRadians(angle))));
    }

    public static float updateXsensorE(float carCenterX, float sensorOffsetX, float sensorOffsetY, float angle){
        return (carCenterX + (float) (sensorOffsetX * Math.cos(Math.toRadians(angle)) - sensorOffsetY * Math.sin(Math.toRadians(angle))));
    }

    public static float updateYsensorE(float carCenterY, float sensorOffsetX, float sensorOffsetY, float angle){
        return (carCenterY + (float) (sensorOffsetX * Math.sin(Math.toRadians(angle)) + sensorOffsetY * Math.cos(Math.toRadians(angle))));
    }

    public static float updateXsensorD(float carCenterX, float sensorOffsetX, float sensorOffsetY, float angle){
        return (carCenterX + (float) (sensorOffsetX * Math.cos(Math.toRadians(angle)) + sensorOffsetY * Math.sin(Math.toRadians(angle))));
    }

    public static float updateYsensorD(float carCenterY, float sensorOffsetX, float sensorOffsetY, float angle){
        return (carCenterY + (float) (sensorOffsetX * Math.sin(Math.toRadians(angle)) - sensorOffsetY * Math.cos(Math.toRadians(angle))));
    }

    public static float deltaX(float speed, float angle){
        return (float) (speed * Math.cos(Math.toRadians(angle))); // Deslocamento no eixo X
    }

    public static float deltaY(float speed, float angle){
        return (float) (speed * Math.sin(Math.toRadians(angle))); // Deslocamento no eixo Y
    }

}
