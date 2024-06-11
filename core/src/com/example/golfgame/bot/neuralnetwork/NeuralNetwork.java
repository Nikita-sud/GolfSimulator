package com.example.golfgame.bot.neuralnetwork;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import com.example.golfgame.utils.MatrixUtils;

/**
 * A class representing a simple feedforward neural network.
 * This class provides methods for training the network and making predictions.
 */
public class NeuralNetwork implements Serializable {
    private static final long serialVersionUID = 1L;

    protected int numLayers;
    protected int[] sizes;
    protected double[][][] weights;
    protected double[][] biases;

    /**
     * A static class to store the results of backpropagation.
     */
    static class BackPropResult {
        double[][][] deltaNablaW;
        double[][] deltaNablaB;

        /**
         * Constructs a BackPropResult object with the specified weight and bias gradients.
         *
         * @param deltaNablaW The gradient of the weights.
         * @param deltaNablaB The gradient of the biases.
         */
        public BackPropResult(double[][][] deltaNablaW, double[][] deltaNablaB) {
            this.deltaNablaW = deltaNablaW;
            this.deltaNablaB = deltaNablaB;
        }
    }

    /**
     * Constructs a neural network with the specified layer sizes.
     *
     * @param sizes An array specifying the number of neurons in each layer of the neural network.
     */
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

    /**
     * Makes a prediction based on the input data.
     *
     * @param input The input data.
     * @return The predicted output.
     */
    public double[] predict(double[] input) {
        double[][] activation = MatrixUtils.getTwoDimensionalVector(input);
    
        for (int i = 0; i < numLayers - 1; i++) {
            activation = MatrixUtils.multiplyMatrices(weights[i], activation);
            activation = MatrixUtils.addBias(activation, biases[i]);
            if (i < numLayers - 2) {
                activation = MatrixUtils.sigmoidVector(activation);
            }
        }
    
        double[] output = MatrixUtils.getOneDimensionalVector(activation);
        double angle = output[0];
        double rawSpeed = output[1];
        double minSpeed = 1;
        double maxSpeed = 5;
        double speed = MatrixUtils.scaleToRange(rawSpeed, minSpeed, maxSpeed);
    
        return new double[]{angle, speed};
    }

    /**
     * Trains the network with a single training example.
     *
     * @param input  The input data.
     * @param target The target output.
     */
    public void trainSingle(double[] input, double[] target) {
        double learningRate = 0.01;
        double lambda = 0.01;  // Regularization parameter
    
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
            weights[i] = MatrixUtils.matrixSubtraction(weights[i], MatrixUtils.scalarMultiplyMatrix(nablaW[i], learningRate));
            biases[i] = MatrixUtils.vectorSubtraction(biases[i], MatrixUtils.scalarMultiplyVector(nablaB[i], learningRate));
    
            // Apply L2 regularization
            for (int j = 0; j < weights[i].length; j++) {
                for (int k = 0; k < weights[i][j].length; k++) {
                    weights[i][j][k] -= lambda * weights[i][j][k];
                }
            }
        }
    }

    /**
     * Returns the weights of the neural network.
     *
     * @return A three-dimensional array representing the weights.
     */
    public double[][][] getWeights() {
        return weights;
    }

    /**
     * Returns the biases of the neural network.
     *
     * @return A two-dimensional array representing the biases.
     */
    public double[][] getBiases() {
        return biases;
    }

    /**
     * Saves the neural network to a file.
     *
     * @param filePath The path of the file to save the network.
     * @throws IOException If an I/O error occurs while saving the network.
     */
    public void saveNetwork(String filePath) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
            oos.writeObject(this);
        }
    }

    /**
     * Loads a neural network from a file.
     *
     * @param filePath The path of the file to load the network from.
     * @return The loaded neural network.
     * @throws IOException            If an I/O error occurs while loading the network.
     * @throws ClassNotFoundException If the class of the serialized object cannot be found.
     */
    public static NeuralNetwork loadNetwork(String filePath) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath))) {
            return (NeuralNetwork) ois.readObject();
        }
    }

    /**
     * Main method for testing the neural network.
     *
     * @param args Command line arguments.
     */
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
