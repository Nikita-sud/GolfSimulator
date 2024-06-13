package com.example.golfgame.bot.neuralnetwork;

import java.io.IOException;
import java.util.Arrays;

public class SaveInitialNetworks {
    public static void main(String[] args) {
        // Укажите размеры сети
        int[] networkSizes = {4, 20,20, 2};

        // Инициализируем основную сеть
        DQLNeuralNetwork mainNetwork = new DQLNeuralNetwork(networkSizes);

        // Инициализируем целевую сеть
        DQLNeuralNetwork targetNetwork = new DQLNeuralNetwork(networkSizes);

        // Указываем пути для сохранения сетей
        String mainNetworkFilePath = "neuralnetworkinformation/mainNetwork.ser";
        String targetNetworkFilePath = "neuralnetworkinformation/targetNetwork.ser";

        // Сохраняем основную сеть
        try {
            mainNetwork.saveNetwork(mainNetworkFilePath);
            System.out.println("Main network saved successfully to " + mainNetworkFilePath);
        } catch (IOException e) {
            System.err.println("Failed to save main network: " + e.getMessage());
        }

        // Сохраняем целевую сеть
        try {
            targetNetwork.saveNetwork(targetNetworkFilePath);
            System.out.println("Target network saved successfully to " + targetNetworkFilePath);
        } catch (IOException e) {
            System.err.println("Failed to save target network: " + e.getMessage());
        }

        // Загрузка и проверка сетей
        try {
            DQLNeuralNetwork loadedMainNetwork = DQLNeuralNetwork.loadNetwork(mainNetworkFilePath);
            DQLNeuralNetwork loadedTargetNetwork = DQLNeuralNetwork.loadNetwork(targetNetworkFilePath);
            System.out.println("Main network loaded successfully from " + mainNetworkFilePath);
            System.out.println("Target network loaded successfully from " + targetNetworkFilePath);
            
            // Проверьте, что загруженные сети совпадают с оригинальными
            double[][][] mainWeights = mainNetwork.getWeights();
            double[][][] loadedMainWeights = loadedMainNetwork.getWeights();
            if (Arrays.deepEquals(mainWeights, loadedMainWeights)) {
                System.out.println("Main network weights match!");
            } else {
                System.out.println("Main network weights do not match.");
            }

            double[][][] targetWeights = targetNetwork.getWeights();
            double[][][] loadedTargetWeights = loadedTargetNetwork.getWeights();
            if (Arrays.deepEquals(targetWeights, loadedTargetWeights)) {
                System.out.println("Target network weights match!");
            } else {
                System.out.println("Target network weights do not match.");
            }
            
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Failed to load networks: " + e.getMessage());
        }
    }
}