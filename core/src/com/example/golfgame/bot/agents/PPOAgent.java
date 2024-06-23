package com.example.golfgame.bot.agents;

import com.example.golfgame.bot.neuralnetwork.PolicyNetwork;
import com.example.golfgame.bot.neuralnetwork.ValueNetwork;
import com.example.golfgame.utils.ppoUtils.Action;
import com.example.golfgame.utils.ppoUtils.BackPropResult;
import com.example.golfgame.utils.ppoUtils.Batch;
import com.example.golfgame.utils.ppoUtils.State;
import com.example.golfgame.utils.ppoUtils.Transition;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * The PPOAgent class implements a Proximal Policy Optimization (PPO) agent.
 * It uses policy and value networks to learn and select actions based on states.
 */
public class PPOAgent implements Serializable {
    private static final long serialVersionUID = 1L;
    private PolicyNetwork policyNetwork;
    private ValueNetwork valueNetwork;
    private List<Transition> memory;
    private double gamma; // Discount factor
    private double lambda; // GAE parameter
    private double epsilon; // Clipping parameter for PPO
    private Random random = new Random(1);

    /**
     * Constructs a PPOAgent with the specified parameters.
     *
     * @param policyNetworkSizes sizes of the layers in the policy network
     * @param valueNetworkSizes sizes of the layers in the value network
     * @param gamma discount factor
     * @param lambda GAE parameter
     * @param epsilon clipping parameter for PPO
     */
    public PPOAgent(int[] policyNetworkSizes, int[] valueNetworkSizes, double gamma, double lambda, double epsilon) {
        this.policyNetwork = new PolicyNetwork(policyNetworkSizes);
        this.valueNetwork = new ValueNetwork(valueNetworkSizes);
        this.memory = new ArrayList<>();
        this.gamma = gamma;
        this.lambda = lambda;
        this.epsilon = epsilon;
    }

    /**
     * Stores a transition in the agent's memory.
     *
     * @param transition the transition to store
     */
    public void storeTransition(Transition transition) {
        memory.add(transition);
    }

    /**
     * Trains the agent on the provided data batches.
     *
     * @param data the list of data batches to train on
     */
    public void trainOnData(List<Batch> data){
        int numberOfEpoches = data.size();
        int epochesCounter = 0;
        System.out.println("\n===========");
        for(Batch batch : data){
            for(Transition transition : batch.getBatch()){
                memory.add(transition);
            }
            train();
            System.out.println("Epoch: "+ epochesCounter+"/"+numberOfEpoches);
            epochesCounter++;
        }
        System.out.println("===========\n");
        System.out.println("Training complete.\n");
    }

    /**
     * Trains the agent using stored transitions in memory.
     */
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

    /**
     * Retrieves the agent's memory of transitions.
     *
     * @return the list of transitions stored in memory
     */
    public List<Transition> getMemory() {
        return memory;
    }

    /**
     * Computes the advantages using Generalized Advantage Estimation (GAE).
     *
     * @return the list of computed advantages
     */
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

    /**
     * Computes the old probabilities for actions using the policy network.
     *
     * @return an array of old probabilities
     */
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

    /**
     * Computes the old probabilities for actions using the policy network in parallel.
     *
     * @return an array of old probabilities
     */
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
    
    /**
     * Computes the advantages using Generalized Advantage Estimation (GAE) in parallel.
     *
     * @return the list of computed advantages
     */
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

    /**
     * Selects an action based on the current state using the policy network.
     *
     * @param state the current state
     * @return the selected action
     */
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
    
    /**
     * Applies the softplus activation function to the input.
     *
     * @param x the input value
     * @return the output value after applying softplus
     */
    private double softplus(double x) {
        return Math.log(1 + Math.exp(x));
    }

    /**
     * Selects a random action within the specified bounds.
     *
     * @return the randomly selected action
     */
    public Action selectRandomAction() {
        double theta = random.nextDouble() * 2 * Math.PI;
        double force = random.nextDouble() * (5 - 1) + 1;
        return new Action(theta, force);
    }

    /**
     * Saves the PPOAgent to a file.
     *
     * @param filePath the path of the file to save the agent
     * @throws IOException if an I/O error occurs
     */
    public void saveAgent(String filePath) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
            oos.writeObject(this);
        }
    }

    /**
     * Loads a PPOAgent from a file.
     *
     * @param filePath the path of the file to load the agent
     * @return the loaded PPOAgent
     * @throws IOException if an I/O error occurs
     * @throws ClassNotFoundException if the class of the serialized object cannot be found
     */
    public static PPOAgent loadAgent(String filePath) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath))) {
            return (PPOAgent) ois.readObject();
        }
    }
}
