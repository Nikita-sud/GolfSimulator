package com.example.golfgame.bot.agents;

import com.example.golfgame.bot.neuralnetwork.PolicyNetwork;
import com.example.golfgame.bot.neuralnetwork.ValueNetwork;
import com.example.golfgame.utils.BackPropResult;
import com.example.golfgame.utils.Transition;

import java.util.ArrayList;
import java.util.List;

public class PPOAgent {
    private PolicyNetwork policyNetwork;
    private ValueNetwork valueNetwork;
    private List<Transition> memory;
    private double gamma; // Discount factor
    private double lambda; // GAE parameter
    private double epsilon; // Clipping parameter for PPO

    public PPOAgent(int[] policyNetworkSizes, int[] valueNetworkSizes, double gamma, double lambda, double epsilon) {
        this.policyNetwork = new PolicyNetwork(policyNetworkSizes);
        this.valueNetwork = new ValueNetwork(valueNetworkSizes);
        this.memory = new ArrayList<>();
        this.gamma = gamma;
        this.lambda = lambda;
        this.epsilon = epsilon;
    }

    public void storeTransition(Transition transition) {
        memory.add(transition);
    }

    public void train() {
        // Compute returns and advantages
        List<Double> returns = computeReturns();
        List<Double> advantages = computeAdvantages(returns);

        // Compute old probabilities
        double[] oldProbabilities = computeOldProbabilities();

        // Extract necessary data
        double[][] states = new double[memory.size()][];
        double[][] actions = new double[memory.size()][];
        double[] advantagesArray = new double[memory.size()];
        double[] targets = new double[memory.size()];
        for (int i = 0; i < memory.size(); i++) {
            states[i] = memory.get(i).getState1().getState();
            actions[i] = new double[]{memory.get(i).getAction().getAngle(), memory.get(i).getAction().getForce()};
            advantagesArray[i] = advantages.get(i);
            targets[i] = returns.get(i);
        }

        // Train networks
        for (int i = 0; i < memory.size(); i++) {
            double[] state = states[i];
            double[] action = actions[i];
            double target = targets[i];

            // Policy network update
            double[][] policyOutput = policyNetwork.forward(state);
            double policyLoss = policyNetwork.computeLoss(policyOutput, action, advantagesArray, oldProbabilities, epsilon);
            BackPropResult policyBackpropResult = policyNetwork.backprop(state, policyLoss);
            policyNetwork.updateParameters(policyBackpropResult.getNablaW(), policyBackpropResult.getNablaB(), 0.001, memory.size());

            // Value network update
            double[][] valueOutput = valueNetwork.forward(state);
            double valueLoss = valueNetwork.computeLoss(valueOutput, new double[]{target});
            BackPropResult valueBackpropResult = valueNetwork.backprop(state, valueLoss);
            valueNetwork.updateParameters(valueBackpropResult.getNablaW(), valueBackpropResult.getNablaB(), 0.001, memory.size());
        }

        // Clear memory after training
        memory.clear();
    }

    private List<Double> computeReturns() {
        List<Double> returns = new ArrayList<>();
        double G = 0.0;
        for (int i = memory.size() - 1; i >= 0; i--) {
            G = memory.get(i).getReward() + gamma * G;
            returns.add(0, G);
        }
        return returns;
    }

    private List<Double> computeAdvantages(List<Double> returns) {
        List<Double> advantages = new ArrayList<>();
        double[] values = new double[memory.size()];
        for (int i = 0; i < memory.size(); i++) {
            values[i] = valueNetwork.forward(memory.get(i).getState1().getState())[0][0];
        }
        for (int i = 0; i < memory.size(); i++) {
            advantages.add(returns.get(i) - values[i]);
        }
        return advantages;
    }

    private double[] computeOldProbabilities() {
        double[] oldProbabilities = new double[memory.size()];
        for (int i = 0; i < memory.size(); i++) {
            double[] state = memory.get(i).getState1().getState();
            double[] action = {memory.get(i).getAction().getAngle(), memory.get(i).getAction().getForce()};
            double[][] policyOutput = policyNetwork.forward(state);
            oldProbabilities[i] = computeProbability(policyOutput, action);
        }
        return oldProbabilities;
    }

    private double computeProbability(double[][] policyOutput, double[] action) {
        double mu_theta = policyOutput[0][0];
        double sigma_theta = policyOutput[1][0];
        double mu_force = policyOutput[2][0];
        double sigma_force = policyOutput[3][0];

        double theta = action[0];
        double force = action[1];

        double prob_theta = (1 / (Math.sqrt(2 * Math.PI) * sigma_theta)) * Math.exp(-Math.pow(theta - mu_theta, 2) / (2 * Math.pow(sigma_theta, 2)));
        double prob_force = (1 / (Math.sqrt(2 * Math.PI) * sigma_force)) * Math.exp(-Math.pow(force - mu_force, 2) / (2 * Math.pow(sigma_force, 2)));

        return prob_theta * prob_force;
    }
}