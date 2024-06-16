package com.example.golfgame.bot.agents;

import com.example.golfgame.bot.neuralnetwork.PolicyNetwork;
import com.example.golfgame.bot.neuralnetwork.ValueNetwork;
import com.example.golfgame.utils.Action;
import com.example.golfgame.utils.BackPropResult;
import com.example.golfgame.utils.State;
import com.example.golfgame.utils.Transition;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PPOAgent implements Serializable {
    private static final long serialVersionUID = 1L;
    private PolicyNetwork policyNetwork;
    private ValueNetwork valueNetwork;
    private List<Transition> memory;
    private double gamma; // Discount factor
    private double lambda; // GAE parameter
    private double epsilon; // Clipping parameter for PPO
    private Random random = new Random(1);

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
        // Compute advantages using GAE
        List<Double> advantages = computeAdvantagesParallel();
        
        // Compute old probabilities
        double[] oldProbabilities = computeOldProbabilitiesParallel();
        
        // Extract necessary data
        double[][] states = new double[memory.size()][];
        double[] advantagesArray = new double[memory.size()];
        double[] targets = new double[memory.size()];
        double[][] actions = new double[memory.size()][2]; 
        
        for (int i = 0; i < memory.size(); i++) {
            states[i] = memory.get(i).getState1().getState();
            advantagesArray[i] = advantages.get(i);
            targets[i] = memory.get(i).getReward() + gamma * valueNetwork.forward(memory.get(i).getState2().getState())[0][0];
            actions[i][0] = memory.get(i).getAction().getAngle(); 
            actions[i][1] = memory.get(i).getAction().getForce(); 
        }
        
        // Compute policy loss and update both networks
        for (int i = 0; i < memory.size(); i++) {
            // Forward pass for policy network
            double[][] policyOutput = policyNetwork.forward(states[i]);
            double policyLoss = policyNetwork.computeLoss(policyOutput, advantagesArray, oldProbabilities, epsilon, actions[i]);
            System.out.println(i + " " + policyLoss);
            
            // Policy network update
            BackPropResult policyBackpropResult = policyNetwork.backprop(states[i], policyLoss);
            policyNetwork.updateParameters(policyBackpropResult.getNablaW(), policyBackpropResult.getNablaB(), 1, memory.size());
    
            // Forward pass for value network
            double[][] valueOutput = valueNetwork.forward(states[i]);
            double valueLoss = valueNetwork.computeLoss(valueOutput, new double[]{targets[i]});
            
            // Value network update
            BackPropResult valueBackpropResult = valueNetwork.backprop(states[i], valueLoss);
            valueNetwork.updateParameters(valueBackpropResult.getNablaW(), valueBackpropResult.getNablaB(), 1, memory.size());
        }
        
        // Clear memory after training
        memory.clear();
    }

    public List<Transition> getMemory() {
        return memory;
    }

    @SuppressWarnings("unused")
    private List<Double> computeAdvantages() {
        List<Double> advantages = new ArrayList<>();
        double[] deltas = new double[memory.size()];
        double[] values = new double[memory.size()];
        double[] nextValues = new double[memory.size()];

        for (int i = 0; i < memory.size(); i++) {
            values[i] = valueNetwork.forward(memory.get(i).getState1().getState())[0][0];
            nextValues[i] = valueNetwork.forward(memory.get(i).getState2().getState())[0][0];
            deltas[i] = memory.get(i).getReward() + gamma * nextValues[i] - values[i];
        }

        double advantage = 0.0;
        for (int i = memory.size() - 1; i >= 0; i--) {
            advantage = deltas[i] + gamma * lambda * advantage;
            advantages.add(0, advantage);
        }
        return advantages;
    }

    @SuppressWarnings("unused")
    private double[] computeOldProbabilities() {
        double[] oldProbabilities = new double[memory.size()];
        for (int i = 0; i < memory.size(); i++) {
            double[] state = memory.get(i).getState1().getState();
            double[] action = {memory.get(i).getAction().getAngle(), memory.get(i).getAction().getForce()};
            double[][] policyOutput = policyNetwork.forward(state);
            oldProbabilities[i] = policyNetwork.computeProbability(policyOutput, action); // Ensure probability is above minProbability
        }
        return oldProbabilities;
    }

    private double[] computeOldProbabilitiesParallel() {
        return memory.parallelStream()
                     .mapToDouble(transition -> {
                         double[] state = transition.getState1().getState();
                         double[] action = {transition.getAction().getAngle(), transition.getAction().getForce()};
                         double[][] policyOutput = policyNetwork.forward(state);
                         return policyNetwork.computeProbability(policyOutput, action);
                     })
                     .toArray();
    }
    
    private List<Double> computeAdvantagesParallel() {
        double[] values = memory.parallelStream()
                                .mapToDouble(transition -> valueNetwork.forward(transition.getState1().getState())[0][0])
                                .toArray();
        double[] nextValues = memory.parallelStream()
                                    .mapToDouble(transition -> valueNetwork.forward(transition.getState2().getState())[0][0])
                                    .toArray();
        double[] deltas = new double[memory.size()];
        for (int i = 0; i < memory.size(); i++) {
            deltas[i] = memory.get(i).getReward() + gamma * nextValues[i] - values[i];
        }
        
        List<Double> advantages = new ArrayList<>();
        double advantage = 0.0;
        for (int i = memory.size() - 1; i >= 0; i--) {
            advantage = deltas[i] + gamma * lambda * advantage;
            advantages.add(0, advantage);
        }
        return advantages;
    }

    public Action selectAction(State state) {
        double[][] policyOutput = policyNetwork.forward(state.getState());
        
        double mu_theta = policyOutput[0][0];
        double sigma_theta_raw = policyOutput[1][0];
        double mu_force = policyOutput[2][0];
        double sigma_force_raw = policyOutput[3][0];
    
        double sigma_theta = softplus(sigma_theta_raw);
        double sigma_force = softplus(sigma_force_raw);

        double theta = mu_theta + sigma_theta * random.nextGaussian();
        double force = mu_force + sigma_force * random.nextGaussian();

        force = Math.min(Math.max(force, 1.0), 5.0);
    
        return new Action(theta, force);
    }
    
    private double softplus(double x) {
        return Math.log(1 + Math.exp(x));
    }

    public Action selectRandomAction() {
        double theta = random.nextDouble() * 2 * Math.PI;
        double force = random.nextDouble() * (5 - 1) + 1;
        return new Action(theta, force);
    }

    public void saveAgent(String filePath) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
            oos.writeObject(this);
        }
    }

    public static PPOAgent loadAgent(String filePath) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath))) {
            return (PPOAgent) ois.readObject();
        }
    }
}