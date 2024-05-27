package com.example.golfgame.bot.neuralnetwork;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class NeuralNetwork implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private int numLayers;
    @SuppressWarnings("unused")
    private int[] sizes;
    private double[][][] weights;
    private double[][] biases;

    static class BackPropResult {
        double[][][] deltaNablaW;
        double[][] deltaNablaB;
        public BackPropResult(double[][][] deltaNablaW, double[][] deltaNablaB) {
            this.deltaNablaW = deltaNablaW;
            this.deltaNablaB = deltaNablaB;
        }
    }

    public NeuralNetwork(int[] sizes) {
        this.numLayers = sizes.length;
        this.sizes = sizes;
        this.biases = new double[sizes.length - 1][];
        this.weights = new double[sizes.length - 1][][];
        Random rand = new Random();

        for (int i = 1; i < sizes.length; i++) {
            biases[i - 1] = new double[sizes[i]];
            weights[i - 1] = new double[sizes[i]][sizes[i - 1]];

            for (int j = 0; j < sizes[i]; j++) {
                biases[i - 1][j] = rand.nextGaussian();
                for (int k = 0; k < sizes[i - 1]; k++) {
                    weights[i - 1][j][k] = rand.nextGaussian();
                }
            }
        }
    }

    public double[] predict(double[] input) {
        double[][] activation = get2DimentionalVector(input);

        for (int i = 0; i < numLayers - 1; i++) {
            activation = multiplyMatrices(weights[i], activation);
            activation = addBias(activation, biases[i]);
            activation = sigmoidVector(activation);
        }

        return getOneDimentionalVector(activation);
    }

    public void trainSingle(double[] input, double[] target) {
        List<double[][]> activations = new ArrayList<>();
        List<double[][]> zs = new ArrayList<>();
        double[][] activation = get2DimentionalVector(input);
        activations.add(activation);

        // Feedforward
        for (int i = 0; i < numLayers - 1; i++) {
            double[][] z = multiplyMatrices(weights[i], activation);
            z = addBias(z, biases[i]);
            zs.add(z);
            activation = sigmoidVector(z);
            activations.add(activation);
        }

        // Backpropagation
        double[][] delta = matrixSubstraction(activations.get(activations.size() - 1), get2DimentionalVector(target));
        double[][][] nablaW = new double[weights.length][][];
        double[][] nablaB = new double[biases.length][];

        for (int i = weights.length - 1; i >= 0; i--) {
            nablaW[i] = multiplyMatrices(delta, transpose(activations.get(i)));
            nablaB[i] = getOneDimentionalVector(delta);
            if (i > 0) {
                delta = hadamardProduct(multiplyMatrices(transpose(weights[i]), delta), primeSigmoidVector(zs.get(i - 1)));
            }
        }

        // Update weights and biases
        for (int i = 0; i < weights.length; i++) {
            weights[i] = matrixSubstraction(weights[i], scalarMultiplyMatrix(nablaW[i], 0.01));
            biases[i] = vectorSubstraction(biases[i], scalarMultiplyVector(nablaB[i], 0.01));
        }
    }

    private double[][] addBias(double[][] matrix, double[] bias) {
        double[][] result = new double[matrix.length][matrix[0].length];
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[0].length; j++) {
                result[i][j] = matrix[i][j] + bias[i];
            }
        }
        return result;
    }

    private double[][] multiplyMatrices(double[][] firstMatrix, double[][] secondMatrix) {
        int firstMatrixRows = firstMatrix.length;
        int firstMatrixCols = firstMatrix[0].length;
        int secondMatrixRows = secondMatrix.length;
        int secondMatrixCols = secondMatrix[0].length;

        if (firstMatrixCols != secondMatrixRows) {
            throw new IllegalArgumentException("Matrix multiplication dimensions do not match");
        }

        double[][] result = new double[firstMatrixRows][secondMatrixCols];
        for (int i = 0; i < firstMatrixRows; i++) {
            for (int j = 0; j < secondMatrixCols; j++) {
                for (int k = 0; k < firstMatrixCols; k++) {
                    result[i][j] += firstMatrix[i][k] * secondMatrix[k][j];
                }
            }
        }
        return result;
    }

    private double[][] sigmoidVector(double[][] x) {
        for (int i = 0; i < x.length; i++) {
            x[i][0] = sigmoid(x[i][0]);
        }
        return x;
    }

    private double sigmoid(double x) {
        return 1 / (1 + Math.exp(-x));
    }

    private double[][] primeSigmoidVector(double[][] x) {
        for (int i = 0; i < x.length; i++) {
            x[i][0] = primeSigmoid(x[i][0]);
        }
        return x;
    }

    private double primeSigmoid(double x) {
        return sigmoid(x) * (1 - sigmoid(x));
    }

    private double[][] transpose(double[][] matrix) {
        int rows = matrix.length;
        int cols = matrix[0].length;
        double[][] transposedMatrix = new double[cols][rows];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                transposedMatrix[j][i] = matrix[i][j];
            }
        }

        return transposedMatrix;
    }

    private double[][] hadamardProduct(double[][] firstMatrix, double[][] secondMatrix) {
        double[][] result = new double[firstMatrix.length][firstMatrix[0].length];
        for (int i = 0; i < firstMatrix.length; i++) {
            for (int j = 0; j < firstMatrix[0].length; j++) {
                result[i][j] = firstMatrix[i][j] * secondMatrix[i][j];
            }
        }
        return result;
    }

    private double[] getOneDimentionalVector(double[][] x) {
        double[] result = new double[x.length];
        for (int i = 0; i < x.length; i++) {
            result[i] = x[i][0];
        }
        return result;
    }

    private double[][] get2DimentionalVector(double[] x) {
        double[][] result = new double[x.length][1];
        for (int i = 0; i < x.length; i++) {
            result[i][0] = x[i];
        }
        return result;
    }

    private double[][] matrixSubstraction(double[][] firstMatrix, double[][] secondMatrix) {
        double[][] result = new double[firstMatrix.length][firstMatrix[0].length];
        for (int i = 0; i < firstMatrix.length; i++) {
            for (int j = 0; j < firstMatrix[0].length; j++) {
                result[i][j] = firstMatrix[i][j] - secondMatrix[i][j];
            }
        }
        return result;
    }

    private double[][] scalarMultiplyMatrix(double[][] matrix, double scalar) {
        double[][] result = new double[matrix.length][matrix[0].length];
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[0].length; j++) {
                result[i][j] = matrix[i][j] * scalar;
            }
        }
        return result;
    }

    private double[] scalarMultiplyVector(double[] vector, double scalar) {
        double[] result = new double[vector.length];
        for (int i = 0; i < vector.length; i++) {
            result[i] = vector[i] * scalar;
        }
        return result;
    }

    private double[] vectorSubstraction(double[] firstVector, double[] secondVector) {
        double[] result = new double[firstVector.length];
        for (int i = 0; i < firstVector.length; i++) {
            result[i] = firstVector[i] - secondVector[i];
        }
        return result;
    }

    public void saveNetwork(String filePath) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
            oos.writeObject(this);
        }
    }

    public static NeuralNetwork loadNetwork(String filePath) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath))) {
            return (NeuralNetwork) ois.readObject();
        }
    }

    public static void main(String[] args) {
        try {

            NeuralNetwork network = new NeuralNetwork(new int[]{4, 10, 2});

            double[] state = new double[]{0.5, 0.2, 0.1, 0.4};
            double[] target = new double[]{1.0, 0.0};

            network.trainSingle(state, target);

            double[] prediction = network.predict(state);
            System.out.println("Prediction: " + Arrays.toString(prediction));

            network.saveNetwork("neuralnetworkinformation/neuralNetwork.ser");

            NeuralNetwork loadedNetwork = NeuralNetwork.loadNetwork("neuralnetworkinformation/neuralNetwork.ser");

            double[] loadedPrediction = loadedNetwork.predict(state);
            System.out.println("Loaded Prediction: " + Arrays.toString(loadedPrediction));
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
