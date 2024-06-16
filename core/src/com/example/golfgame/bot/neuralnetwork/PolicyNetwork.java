package com.example.golfgame.bot.neuralnetwork;

public class PolicyNetwork extends NeuralNetwork {
    private double minProbability = 1e-10; // Minimum probability to avoid zero probabilities
    public PolicyNetwork(int[] sizes) {
        super(sizes);
    }

    public double computeLoss(double[][] policyOutputs, double[] advantages, double[] oldProbabilities, double epsilon, double[][] actions) {
        double loss = 0.0;
        for (int i = 0; i < advantages.length; i++) {        
            double[] action = actions[i];
            double probability = computeProbability(policyOutputs, action);
            double probabilityRatio = probability / oldProbabilities[i];
            double clippedRatio = Math.max(Math.min(probabilityRatio, 1 + epsilon), 1 - epsilon);
            loss += Math.min(probabilityRatio * advantages[i], clippedRatio * advantages[i]);
        }
        return -loss / policyOutputs.length;
    }

    public double computeProbability(double[][] policyOutput, double[] action) {
        double mu_theta = policyOutput[0][0];
        double sigma_theta = policyOutput[1][0];
        double mu_force = policyOutput[2][0];
        double sigma_force = policyOutput[3][0];

        double theta = action[0];
        double force = action[1];

        double prob_theta = (1 / (Math.sqrt(2 * Math.PI) * sigma_theta)) * Math.exp(-Math.pow(theta - mu_theta, 2) / (2 * Math.pow(sigma_theta, 2)));
        double prob_force = (1 / (Math.sqrt(2 * Math.PI) * sigma_force)) * Math.exp(-Math.pow(force - mu_force, 2) / (2 * Math.pow(sigma_force, 2)));

        return Math.max(prob_theta * prob_force, minProbability);
    }
}