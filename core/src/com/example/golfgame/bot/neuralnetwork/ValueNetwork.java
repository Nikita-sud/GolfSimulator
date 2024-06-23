package com.example.golfgame.bot.neuralnetwork;

/**
 * A value network that extends the NeuralNetwork class.
 * This network is used for estimating the value function in reinforcement learning.
 */
public class ValueNetwork extends NeuralNetwork {

    /**
     * Constructs a ValueNetwork with the specified layer sizes.
     *
     * @param sizes an array specifying the number of neurons in each layer
     */
    public ValueNetwork(int[] sizes) {
        super(sizes);
    }

    /**
     * Computes the loss for the value network using mean squared error.
     *
     * @param output the output of the value network
     * @param target the target values
     * @return the computed loss value
     */
    public double computeLoss(double[][] output, double[] target) {
        double loss = 0.0;
        for (int i = 0; i < output.length; i++) {
            loss += Math.pow(output[i][0] - target[i], 2);
        }
        return loss / output.length;
    }
}