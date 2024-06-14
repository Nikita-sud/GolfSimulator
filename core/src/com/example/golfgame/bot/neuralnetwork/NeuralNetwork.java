package com.example.golfgame.bot.neuralnetwork;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import com.example.golfgame.utils.BackPropResult;
import com.example.golfgame.utils.MatrixUtils;

public abstract class NeuralNetwork implements Serializable {
    private static final long serialVersionUID = 1L;
    protected int numLayers;
    protected int[] sizes;
    protected double[][][] weights;
    protected double[][] biases;

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

    public void updateParameters(double[][][] nabla_w, double[][] nabla_b, double eta, int miniBatchSize) {
        for (int j = 0; j < weights.length; j++) {
            for (int m = 0; m < weights[j].length; m++) {
                for (int n = 0; n < weights[j][m].length; n++) {
                    this.weights[j][m][n] -= (eta / miniBatchSize) * nabla_w[j][m][n];
                }
            }
        }

        for (int j = 0; j < biases.length; j++) {
            for (int m = 0; m < biases[j].length; m++) {
                this.biases[j][m] -= (eta / miniBatchSize) * nabla_b[j][m];
            }
        }
    }

    public BackPropResult backprop(double[] input, double loss) {
        double[][] nabla_b = new double[this.biases.length][];
        for (int i = 0; i < this.biases.length; i++) {
            nabla_b[i] = new double[this.biases[i].length];
            Arrays.fill(nabla_b[i], 0.0); // Fill with zeros
        }
        double[][][] nabla_w = new double[this.weights.length][][];
        for (int i = 0; i < this.weights.length; i++) {
            nabla_w[i] = new double[this.weights[i].length][];
            for (int j = 0; j < this.weights[i].length; j++) {
                nabla_w[i][j] = new double[this.weights[i][j].length];
                Arrays.fill(nabla_w[i][j], 0.0); // Fill with zeros
            }
        }

        // feedforward
        double[][] activation = new double[input.length][1];
        for (int i = 0; i < input.length; i++) {
            activation[i][0] = input[i];
        }
        List<double[][]> activations = new ArrayList<>();
        activations.add(activation);
        List<double[][]> zs = new ArrayList<>();

        for (int i = 0; i < numLayers - 1; i++) {
            double[][] z = MatrixUtils.multiplyMatrices(weights[i], activation);
            z = MatrixUtils.addBias(z, biases[i]);
            zs.add(z);
            activation = MatrixUtils.sigmoidVector(z);
            activations.add(activation);
        }

        // Backward pass
        double[][] delta = MatrixUtils.scalarMultiply(MatrixUtils.sigmoidPrimeVector(zs.get(zs.size() - 1)), loss);
        nabla_b[nabla_b.length - 1] = MatrixUtils.getOneDimensionalVector(delta);
        nabla_w[nabla_w.length - 1] = MatrixUtils.multiplyMatrices(delta, MatrixUtils.transpose(activations.get(activations.size() - 2)));

        for (int i = 2; i < numLayers; i++) {
            double[][] z = zs.get(zs.size() - i);
            double[][] sp = MatrixUtils.sigmoidPrimeVector(z);
            delta = MatrixUtils.multiplyMatrices(MatrixUtils.transpose(weights[weights.length - i + 1]), delta);
            delta = MatrixUtils.hadamardProduct(delta, sp);
            nabla_b[nabla_b.length - i] = MatrixUtils.getOneDimensionalVector(delta);
            nabla_w[nabla_w.length - i] = MatrixUtils.multiplyMatrices(delta, MatrixUtils.transpose(activations.get(activations.size() - i - 1)));
        }

        return new BackPropResult(nabla_w, nabla_b);
    }

    public double[][] forward(double[] input) {
        double[][] activation = new double[input.length][1];
        for (int i = 0; i < input.length; i++) {
            activation[i][0] = input[i];
        }

        for (int i = 0; i < numLayers - 1; i++) {
            double[][] z = MatrixUtils.multiplyMatrices(weights[i], activation);
            z = MatrixUtils.addBias(z, biases[i]);
            activation = MatrixUtils.sigmoidVector(z);
        }

        return activation;
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
}