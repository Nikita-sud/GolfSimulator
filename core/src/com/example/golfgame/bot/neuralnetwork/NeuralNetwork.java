package com.example.golfgame.bot.neuralnetwork;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import com.example.golfgame.utils.MatrixUtils;
import com.example.golfgame.utils.ppoUtils.BackPropResult;

/**
 * Abstract class representing a neural network for machine learning tasks.
 * Provides methods for forward propagation, backpropagation, and parameter updates using Adam optimizer.
 */
public abstract class NeuralNetwork implements Serializable {
    private static final long serialVersionUID = 1L; // Добавлено для Serializable
    protected int numLayers;
    protected int[] sizes;
    protected double[][][] weights;
    protected double[][] biases;
    protected ActivationType[] layerActivationTypes;

    // --- Поля для Adam Optimizer ---
    // transient: Не сохраняем состояние Adam при сериализации (опционально, но часто проще инициализировать заново)
    private transient double[][][] m_weights; // Первый момент для весов
    private transient double[][][] v_weights; // Второй момент для весов
    private transient double[][] m_biases;    // Первый момент для смещений
    private transient double[][] v_biases;    // Второй момент для смещений
    private transient double beta1 = 0.9;     // Параметр Adam
    private transient double beta2 = 0.999;   // Параметр Adam
    private transient double epsilon_adam = 1e-8; // Параметр Adam для стабильности
    private transient long t = 0;             // Счетчик шагов Adam (для коррекции смещения)
    // --- Конец полей Adam ---

    /**
     * Constructs a neural network with specified layer sizes and activation functions.
     * Also initializes the Adam optimizer state.
     *
     * @param sizes               An array specifying the number of neurons in each layer.
     * @param layerActivationTypes An array specifying the activation function for each layer
     *                            (starting from the first hidden layer). Its length must be
     *                            sizes.length - 1. The last element corresponds to the output layer.
     * @throws IllegalArgumentException if the lengths of sizes and layerActivationTypes don't match.
     */
    public NeuralNetwork(int[] sizes, ActivationType[] layerActivationTypes) {
        if (sizes.length - 1 != layerActivationTypes.length) {
            throw new IllegalArgumentException("Number of activation types must be equal to number of layers minus 1.");
        }
        this.numLayers = sizes.length;
        this.sizes = sizes;
        this.layerActivationTypes = layerActivationTypes;
        this.biases = new double[sizes.length - 1][];
        this.weights = new double[sizes.length - 1][][];
        Random rand = new Random();

        // Инициализируем состояние Adam сразу
        initializeAdamState();

        for (int i = 1; i < sizes.length; i++) {
            int layerIdx = i - 1;
            biases[layerIdx] = new double[sizes[i]];
            double stdDev = calculateInitStdDev(sizes[i-1], sizes[i], layerActivationTypes[layerIdx]);
            weights[layerIdx] = new double[sizes[i]][sizes[i - 1]];

            for (int j = 0; j < sizes[i]; j++) {
                biases[layerIdx][j] = 0.01 * rand.nextGaussian(); // Малое случайное значение
                for (int k = 0; k < sizes[i - 1]; k++) {
                    weights[layerIdx][j][k] = rand.nextGaussian() * stdDev;
                }
            }
        }
    }

    // Метод для инициализации или сброса состояния Adam
    private void initializeAdamState() {
        this.t = 0;
        this.m_weights = new double[weights.length][][];
        this.v_weights = new double[weights.length][][];
        this.m_biases = new double[biases.length][];
        this.v_biases = new double[biases.length][];

        for (int i = 1; i < sizes.length; i++) {
            int layerIdx = i - 1;
            m_weights[layerIdx] = new double[sizes[i]][sizes[i - 1]];
            v_weights[layerIdx] = new double[sizes[i]][sizes[i - 1]];
            m_biases[layerIdx] = new double[sizes[i]];
            v_biases[layerIdx] = new double[sizes[i]];
            // Заполняем нулями
            for(int j=0; j<m_biases[layerIdx].length; ++j) {
                m_biases[layerIdx][j] = 0.0;
                v_biases[layerIdx][j] = 0.0;
            }
             for(int j=0; j<m_weights[layerIdx].length; ++j) {
                 Arrays.fill(m_weights[layerIdx][j], 0.0);
                 Arrays.fill(v_weights[layerIdx][j], 0.0);
             }
        }
         System.out.println("Adam optimizer state initialized.");
    }


    // Вспомогательный метод для инициализации весов (He/Xavier initialization)
    private double calculateInitStdDev(int fanIn, int fanOut, ActivationType activation) {
        switch (activation) {
            case RELU:
                return Math.sqrt(2.0 / fanIn); // He initialization
            case TANH:
            case SIGMOID: // Xavier/Glorot initialization
                return Math.sqrt(1.0 / fanIn); // Упрощенный Xavier/Glorot
            case LINEAR:
            default: // Для линейного или неизвестного - используем базовый Glorot/Xavier
                 return Math.sqrt(1.0 / fanIn);
        }
    }

    // Применение функции активации
    private double[][] applyActivation(double[][] z, ActivationType type) {
        switch (type) {
            case SIGMOID: return MatrixUtils.sigmoidVector(z);
            case RELU:    return MatrixUtils.reluVector(z);
            case TANH:    return MatrixUtils.tanhVector(z);
            case LINEAR:  default: return MatrixUtils.linearVector(z);
        }
    }

    // Применение производной функции активации
    private double[][] applyActivationPrime(double[][] z, ActivationType type) {
         switch (type) {
            case SIGMOID: return MatrixUtils.sigmoidPrimeVector(z);
            case RELU:    return MatrixUtils.reluPrimeVector(z);
            case TANH:    return MatrixUtils.tanhPrimeVector(z);
            case LINEAR:  default: return MatrixUtils.linearPrimeVector(z);
        }
    }

    /**
     * Updates the parameters of the neural network using the Adam optimizer.
     *
     * @param nabla_w       the gradients for the weights (dL/dW) accumulated over the mini-batch.
     * @param nabla_b       the gradients for the biases (dL/db) accumulated over the mini-batch.
     * @param eta           the learning rate (alpha in Adam).
     * @param miniBatchSize the size of the mini-batch (used for averaging gradients).
     */
    public void updateParameters(double[][][] nabla_w, double[][] nabla_b, double eta, int miniBatchSize) {
        if (miniBatchSize <= 0) {
             System.err.println("Warning: miniBatchSize is zero or negative in updateParameters. Skipping update.");
             return;
        }
        // Проверяем, инициализировано ли состояние Adam (важно после десериализации)
        if (m_weights == null) {
            initializeAdamState();
        }

        t++; // Увеличиваем счетчик шагов Adam

        // Коррекция смещения для моментов (bias correction terms)
        double biasCorrection1 = 1.0 - Math.pow(beta1, t);
        double biasCorrection2 = 1.0 - Math.pow(beta2, t);

        for (int j = 0; j < weights.length; j++) { // Индекс слоя (0 = первый скрытый)
            for (int m = 0; m < weights[j].length; m++) { // Индекс нейрона в слое j+1
                for (int n = 0; n < weights[j][m].length; n++) { // Индекс нейрона в слое j
                    // Вычисляем средний градиент по мини-батчу
                    double grad_w = nabla_w[j][m][n] / miniBatchSize;

                    // Обновляем моменты для веса w[j][m][n]
                    m_weights[j][m][n] = beta1 * m_weights[j][m][n] + (1.0 - beta1) * grad_w;
                    v_weights[j][m][n] = beta2 * v_weights[j][m][n] + (1.0 - beta2) * (grad_w * grad_w);

                    // Коррекция смещения моментов
                    double m_hat_w = m_weights[j][m][n] / biasCorrection1;
                    double v_hat_w = v_weights[j][m][n] / biasCorrection2;

                    // Обновление веса по формуле Adam
                    this.weights[j][m][n] -= eta * m_hat_w / (Math.sqrt(v_hat_w) + epsilon_adam);
                }
            }
        }

        for (int j = 0; j < biases.length; j++) { // Индекс слоя (0 = первый скрытый)
            for (int m = 0; m < biases[j].length; m++) { // Индекс нейрона (смещения) в слое j+1
                 // Вычисляем средний градиент по мини-батчу
                double grad_b = nabla_b[j][m] / miniBatchSize;

                 // Обновляем моменты для смещения b[j][m]
                m_biases[j][m] = beta1 * m_biases[j][m] + (1.0 - beta1) * grad_b;
                v_biases[j][m] = beta2 * v_biases[j][m] + (1.0 - beta2) * (grad_b * grad_b);

                 // Коррекция смещения моментов
                double m_hat_b = m_biases[j][m] / biasCorrection1;
                double v_hat_b = v_biases[j][m] / biasCorrection2;

                 // Обновление смещения по формуле Adam
                this.biases[j][m] -= eta * m_hat_b / (Math.sqrt(v_hat_b) + epsilon_adam);
            }
        }
    }

    // Прямое распространение с сохранением промежуточных значений z
    public Pair<List<double[][]>, List<double[][]>> forwardWithZs(double[] input) {
        double[][] activation = new double[input.length][1];
        for (int i = 0; i < input.length; i++) {
            activation[i][0] = input[i];
        }
        List<double[][]> activations = new ArrayList<>();
        activations.add(MatrixUtils.copyMatrix(activation));
        List<double[][]> zs = new ArrayList<>();

        for (int i = 0; i < numLayers - 1; i++) {
            double[][] z = MatrixUtils.multiplyMatrices(weights[i], activation);
            if (containsNaN(z)) throw new IllegalStateException("NaN in z mult layer " + (i+1));
            z = MatrixUtils.addBias(z, biases[i]);
            if (containsNaN(z)) throw new IllegalStateException("NaN after bias layer " + (i+1));
            zs.add(z);
            ActivationType currentActivationType = layerActivationTypes[i];
            activation = applyActivation(z, currentActivationType);
            if (containsNaN(activation)) throw new IllegalStateException("NaN after activation layer " + (i+1) + " type " + currentActivationType);
            activations.add(activation);
        }
        return new Pair<>(new ArrayList<>(activations), new ArrayList<>(zs));
    }

    // Класс Pair для возврата двух значений
    public static class Pair<T, U> implements Serializable {
        private static final long serialVersionUID = 1L;
        public final T _1;
        public final U _2;
        public Pair(T t, U u) { this._1 = t; this._2 = u; }
     }

    // Обратное распространение ошибки
    public BackPropResult backprop(double[] input, double[][] initial_delta) {
        double[][] nabla_b = new double[this.biases.length][];
        for (int i = 0; i < this.biases.length; i++) {
            nabla_b[i] = new double[this.biases[i].length];
            // Не нужно заполнять нулями здесь, т.к. они будут перезаписаны
        }
        double[][][] nabla_w = new double[this.weights.length][][];
        for (int i = 0; i < this.weights.length; i++) {
            nabla_w[i] = new double[this.weights[i].length][];
            for (int j = 0; j < this.weights[i].length; j++) {
                nabla_w[i][j] = new double[this.weights[i][j].length];
                // Не нужно заполнять нулями здесь
            }
        }

        Pair<List<double[][]>, List<double[][]>> forwardResult = forwardWithZs(input);
        List<double[][]> activations = forwardResult._1;
        List<double[][]> zs = forwardResult._2;
        double[][] delta = initial_delta;

        int lastLayerIndex = numLayers - 2;
        nabla_b[lastLayerIndex] = MatrixUtils.getOneDimensionalVector(delta);
        nabla_w[lastLayerIndex] = MatrixUtils.multiplyMatrices(delta, MatrixUtils.transpose(activations.get(lastLayerIndex)));

        for (int i = 2; i < numLayers; i++) {
            int currentLayerIndexInArrays = numLayers - 1 - i;
            double[][] z = zs.get(currentLayerIndexInArrays);
            ActivationType activationType = layerActivationTypes[currentLayerIndexInArrays];
            double[][] sp = applyActivationPrime(z, activationType);
            delta = MatrixUtils.multiplyMatrices(MatrixUtils.transpose(weights[currentLayerIndexInArrays + 1]), delta);
            delta = MatrixUtils.hadamardProduct(delta, sp);
            nabla_b[currentLayerIndexInArrays] = MatrixUtils.getOneDimensionalVector(delta);
            nabla_w[currentLayerIndexInArrays] = MatrixUtils.multiplyMatrices(delta, MatrixUtils.transpose(activations.get(currentLayerIndexInArrays)));
        }
        return new BackPropResult(nabla_w, nabla_b);
    }

    // Геттеры для весов и смещений
    public double[][][] getWeights() {
        return weights;
    }
    public double[][] getBiases() {
        return biases;
    }

    // Старый метод forward (не используется в обучении PPO, но может быть нужен где-то еще)
    public double[][] forward(double[] input) {
        double[][] activation = new double[input.length][1];
        for (int i = 0; i < input.length; i++) {
            activation[i][0] = input[i];
        }
        for (int i = 0; i < numLayers - 1; i++) {
            double[][] z = MatrixUtils.multiplyMatrices(weights[i], activation);
            if (containsNaN(z)) throw new IllegalStateException("NaN value encountered after z " + i);
            z = MatrixUtils.addBias(z, biases[i]);
            if (containsNaN(z)) throw new IllegalStateException("NaN value encountered after adding bias in layer " + i);
            // --- ВАЖНО: Этот forward использует только applyActivation ---
            // --- Если вам нужен старый forward, убедитесь, что он использует правильные активации ---
            // activation = MatrixUtils.sigmoidVector(z); // <-- Старый код, использовал только Sigmoid
            activation = applyActivation(z, layerActivationTypes[i]); // <-- Используем правильную активацию
            if (containsNaN(activation)) throw new IllegalStateException("NaN value encountered after activation in layer " + (i+1));
        }
        return activation;
    }


    // Проверка на NaN
    private boolean containsNaN(double[][] matrix) {
        if (matrix == null) return true; // Считаем null как проблему
        for (double[] row : matrix) {
            if (row == null) return true;
            for (double value : row) {
                if (Double.isNaN(value) || Double.isInfinite(value)) { // Проверяем и на Infinity
                    return true;
                }
            }
        }
        return false;
    }

    // Сохранение сети
    public void saveNetwork(String filePath) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
            oos.writeObject(this); // Сохраняем объект NeuralNetwork целиком
        }
    }

    // Загрузка сети
    public static NeuralNetwork loadNetwork(String filePath) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath))) {
            NeuralNetwork loadedNetwork = (NeuralNetwork) ois.readObject();
            // Явно инициализируем состояние Adam после загрузки, если оно transient
            if (loadedNetwork.m_weights == null) {
                loadedNetwork.initializeAdamState();
            }
            return loadedNetwork;
        }
    }

    // --- Переопределение readObject для инициализации Adam при десериализации ---
    // Этот метод вызывается АВТОМАТИЧЕСКИ при десериализации
    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ois.defaultReadObject(); // Читаем основные не-transient поля (weights, biases, sizes, etc.)

        // Так как поля Adam объявлены transient, они НЕ будут прочитаны из файла.
        // Поэтому мы должны их инициализировать заново после загрузки объекта.
        initializeAdamState();
    }
    public void resetAdamState() {
        System.out.println("Resetting Adam optimizer state for " + this.getClass().getSimpleName()); // Добавим имя класса для ясности
        this.t = 0;
        // ... остальная логика инициализации m_weights, v_weights и т.д. ...
        this.m_weights = new double[weights.length][][]; // Переинициализация массивов
        this.v_weights = new double[weights.length][][];
        this.m_biases = new double[biases.length][];
        this.v_biases = new double[biases.length][];
        for (int i = 1; i < sizes.length; i++) {
             int layerIdx = i - 1;
             m_weights[layerIdx] = new double[sizes[i]][sizes[i - 1]];
             v_weights[layerIdx] = new double[sizes[i]][sizes[i - 1]];
             m_biases[layerIdx] = new double[sizes[i]];
             v_biases[layerIdx] = new double[sizes[i]];
             for(int j=0; j<m_biases[layerIdx].length; ++j) {
                 m_biases[layerIdx][j] = 0.0;
                 v_biases[layerIdx][j] = 0.0;
             }
              for(int j=0; j<m_weights[layerIdx].length; ++j) {
                  Arrays.fill(m_weights[layerIdx][j], 0.0);
                  Arrays.fill(v_weights[layerIdx][j], 0.0);
              }
         }
    }
}