package com.example.golfgame.bot.neuralnetwork;

/**
 * A policy network that extends the NeuralNetwork class.
 * This network is used for policy-based reinforcement learning.
 */
public class PolicyNetwork extends NeuralNetwork {
    private double minProbability = 1e-10; // Minimum probability to avoid zero probabilities

    /**
     * Constructs a PolicyNetwork with the specified layer sizes.
     *
     * @param sizes an array specifying the number of neurons in each layer
     */
    public PolicyNetwork(int[] sizes) {
        super(sizes);
    }

    /**
     * Computes the loss for the policy network using the PPO objective function.
     *
     * @param policyOutputs the outputs of the policy network
     * @param advantages the advantages computed for the actions
     * @param oldProbabilities the old probabilities of the actions
     * @param epsilon the clipping parameter for PPO
     * @param action the action taken
     * @return the computed loss value
     */
    public double computeLoss(double[][] policyOutputs, double[] advantages, double[] oldProbabilities, double epsilon, double[] action) {
        double loss = 0.0;
        for (int i = 0; i < advantages.length; i++) {        
            double probability = computeProbability(policyOutputs, action);
            double probabilityRatio = probability / oldProbabilities[i];
            double clippedRatio = Math.max(Math.min(probabilityRatio, 1 + epsilon), 1 - epsilon);
            loss += Math.min(probabilityRatio * advantages[i], clippedRatio * advantages[i]);
        }
        return -loss / advantages.length;
    }

    /**
     * Computes the probability of taking a specific action given the policy output.
     *
     * @param policyOutput the output of the policy network
     * @param action the action for which the probability is computed
     * @return the computed probability value
     */
    public double computeProbability(double[][] policyOutput, double[] action) {
        double mu_theta = policyOutput[0][0];
        double sigma_theta_raw = policyOutput[1][0];
        double mu_force = policyOutput[2][0];
        double sigma_force_raw = policyOutput[3][0];

        // Softplus
        double sigma_theta = softplus(sigma_theta_raw);
        double sigma_force = softplus(sigma_force_raw);

        double theta = action[0];
        double force = action[1];

        double prob_theta = (1 / (Math.sqrt(2 * Math.PI) * sigma_theta)) 
                            * Math.exp(-Math.pow(theta - mu_theta, 2) / (2 * Math.pow(sigma_theta, 2)));
        
        double prob_force = (1 / (Math.sqrt(2 * Math.PI) * sigma_force)) 
                            * Math.exp(-Math.pow(force - mu_force, 2) / (2 * Math.pow(sigma_force, 2)));
        
        double probability = prob_theta * prob_force;

        return Math.max(probability, minProbability);
    }

    /**
     * Applies the softplus activation function to the input value.
     *
     * @param x the input value
     * @return the result of applying the softplus function to the input value
     */
    private double softplus(double x) {
        return Math.log(1 + Math.exp(x));
    }
}