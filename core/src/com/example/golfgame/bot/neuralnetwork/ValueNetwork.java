package com.example.golfgame.bot.neuralnetwork;

public class ValueNetwork extends NeuralNetwork {
    public ValueNetwork(int[] sizes) {
        super(sizes);
    }

    public double getValue(double[] state) {
        return predict(state)[0];
    }
}