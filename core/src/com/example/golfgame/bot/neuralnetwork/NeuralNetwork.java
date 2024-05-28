package com.example.golfgame.bot.neuralnetwork;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import com.example.golfgame.utils.MatrixUtils;

public class NeuralNetwork implements Serializable {
    private static final long serialVersionUID = 1L;

    protected int numLayers;
    protected int[] sizes;
    protected double[][][] weights;
    protected double[][] biases;

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
        double[][] activation = MatrixUtils.getTwoDimensionalVector(input);

        for (int i = 0; i < numLayers - 1; i++) {
            activation = MatrixUtils.multiplyMatrices(weights[i], activation);
            activation = MatrixUtils.addBias(activation, biases[i]);
            activation = MatrixUtils.sigmoidVector(activation);
        }

        return MatrixUtils.getOneDimensionalVector(activation);
    }

    public void trainSingle(double[] input, double[] target) {
        List<double[][]> activations = new ArrayList<>();
        List<double[][]> zs = new ArrayList<>();
        double[][] activation = MatrixUtils.getTwoDimensionalVector(input);
        activations.add(activation);

        for (int i = 0; i < numLayers - 1; i++) {
            double[][] z = MatrixUtils.multiplyMatrices(weights[i], activation);
            z = MatrixUtils.addBias(z, biases[i]);
            zs.add(z);
            activation = MatrixUtils.sigmoidVector(z);
            activations.add(activation);
        }

        double[][] delta = MatrixUtils.matrixSubtraction(activations.get(activations.size() - 1), MatrixUtils.getTwoDimensionalVector(target));
        double[][][] nablaW = new double[weights.length][][];
        double[][] nablaB = new double[biases.length][];

        for (int i = weights.length - 1; i >= 0; i--) {
            nablaW[i] = MatrixUtils.multiplyMatrices(delta, MatrixUtils.transpose(activations.get(i)));
            nablaB[i] = MatrixUtils.getOneDimensionalVector(delta);
            if (i > 0) {
                delta = MatrixUtils.hadamardProduct(MatrixUtils.multiplyMatrices(MatrixUtils.transpose(weights[i]), delta), MatrixUtils.primeSigmoidVector(zs.get(i - 1)));
            }
        }

        for (int i = 0; i < weights.length; i++) {
            weights[i] = MatrixUtils.matrixSubtraction(weights[i], MatrixUtils.scalarMultiplyMatrix(nablaW[i], 0.01));
            biases[i] = MatrixUtils.vectorSubtraction(biases[i], MatrixUtils.scalarMultiplyVector(nablaB[i], 0.01));
        }
    }

    public double[][][] getWeights() {
        return weights;
    }

    public double[][] getBiases() {
        return biases;
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