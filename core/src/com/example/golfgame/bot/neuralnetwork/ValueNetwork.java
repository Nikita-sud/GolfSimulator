package com.example.golfgame.bot.neuralnetwork;

public class ValueNetwork extends NeuralNetwork {
    public ValueNetwork(int[] sizes) {
        super(sizes);
    }

    public double computeLoss(double[][] output, double[] target) {
        double loss = 0.0;
        for (int i = 0; i < output.length; i++) {
            loss += Math.pow(output[i][0] - target[i], 2);
        }
        return loss / output.length;
    }
}