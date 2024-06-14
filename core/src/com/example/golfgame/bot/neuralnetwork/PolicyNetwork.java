package com.example.golfgame.bot.neuralnetwork;

public class PolicyNetwork extends NeuralNetwork {
    public PolicyNetwork(int[] sizes) {
        super(sizes);
    }

    public double computeLoss(double[][] output, double[] target, double[] advantages, double[] oldProbabilities, double epsilon) {
        double loss = 0.0;
        for (int i = 0; i < output.length; i++) {
            double probabilityRatio = output[i][0] / oldProbabilities[i];
            double clippedRatio = Math.max(Math.min(probabilityRatio, 1 + epsilon), 1 - epsilon);
            loss += Math.min(probabilityRatio * advantages[i], clippedRatio * advantages[i]);
        }
        return -loss / output.length; // Отрицательная функция потерь, так как будем минимизировать
    }

    @Override
    public double computeLoss(double[][] output, double[] target) {
        throw new UnsupportedOperationException("Unimplemented method 'computeLoss' for PolicyNetwork without advantages and oldProbabilities");
    }
}