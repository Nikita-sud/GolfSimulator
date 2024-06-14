package com.example.golfgame.bot.agents;

import com.example.golfgame.bot.neuralnetwork.PolicyNetwork;
import com.example.golfgame.bot.neuralnetwork.ValueNetwork;
import com.example.golfgame.utils.ReplayMemory;
import java.util.ArrayList;
import java.util.List;

public class PPOAgent {
    private PolicyNetwork policyNetwork;
    private ValueNetwork valueNetwork;
    private ReplayMemory memory;
    private double gamma;
    private double epsilon;
    private int batchSize;
    private double clipParam;
    private double learningRate;
    private double lambda;

    public PPOAgent(int[] policyNetworkSizes, int[] valueNetworkSizes, int memoryCapacity, double gamma, double epsilon, int batchSize, double clipParam, double learningRate, double lambda) {
        this.policyNetwork = new PolicyNetwork(policyNetworkSizes);
        this.valueNetwork = new ValueNetwork(valueNetworkSizes);
        this.memory = new ReplayMemory(memoryCapacity);
        this.gamma = gamma;
        this.epsilon = epsilon;
        this.batchSize = batchSize;
        this.clipParam = clipParam;
        this.learningRate = learningRate;
        this.lambda = lambda;
    }

    public double[] selectAction(double[] state) {
        if (Math.random() < epsilon) {
            return new double[]{Math.random() * 2 * Math.PI, Math.random() * 10}; // random action
        }
        return policyNetwork.getActionProbabilities(state); // policy network action
    }

    public void addExperience(double[] state, double[] action, double reward, double[] nextState, boolean done) {
        memory.add(new ReplayMemory.Experience(state, action, reward, nextState, done));
    }

    public void train() {
        List<ReplayMemory.Experience> batch = memory.sample(batchSize);
        update(batch);
    }

    private void update(List<ReplayMemory.Experience> batch) {
        List<double[]> states = new ArrayList<>();
        List<double[]> actions = new ArrayList<>();
        List<Double> rewards = new ArrayList<>();
        List<double[]> nextStates = new ArrayList<>();
        List<Boolean> dones = new ArrayList<>();

        for (ReplayMemory.Experience exp : batch) {
            states.add(exp.state);
            actions.add(exp.action);
            rewards.add(exp.reward);
            nextStates.add(exp.nextState);
            dones.add(exp.done);
        }

        double[][] stateArray = states.toArray(new double[0][0]);
        double[][] actionArray = actions.toArray(new double[0][0]);
        double[] rewardArray = rewards.stream().mapToDouble(d -> d).toArray();
        double[][] nextStateArray = nextStates.toArray(new double[0][0]);
        boolean[] doneArray = new boolean[dones.size()];
        for (int i = 0; i < dones.size(); i++) {
            doneArray[i] = dones.get(i);
        }

        double[] values = new double[stateArray.length];
        for (int i = 0; i < stateArray.length; i++) {
            values[i] = valueNetwork.getValue(stateArray[i]);
        }

        double[] nextValues = new double[nextStateArray.length];
        for (int i = 0; i < nextStateArray.length; i++) {
            nextValues[i] = valueNetwork.getValue(nextStateArray[i]);
        }

        double[] advantages = new double[rewardArray.length];
        double[] targets = new double[rewardArray.length];
        for (int i = 0; i < rewardArray.length; i++) {
            double tdError = rewardArray[i] + (doneArray[i] ? 0 : gamma * nextValues[i]) - values[i];
            advantages[i] = tdError;
            targets[i] = rewardArray[i] + (doneArray[i] ? 0 : gamma * nextValues[i]);
        }

        for (int i = 0; i < stateArray.length; i++) {
            policyNetwork.update(stateArray[i], actionArray[i], advantages[i], clipParam, learningRate, lambda);
        }

        for (int i = 0; i < stateArray.length; i++) {
            valueNetwork.update(stateArray[i], new double[]{targets[i]}, learningRate, lambda);
        }
    }
}