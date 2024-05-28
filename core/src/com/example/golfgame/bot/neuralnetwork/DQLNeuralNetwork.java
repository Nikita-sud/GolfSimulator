package com.example.golfgame.bot.neuralnetwork;

import java.util.Arrays;
import com.example.golfgame.utils.ReplayMemory;

/**
 * A class representing a Deep Q-Learning Neural Network for reinforcement learning tasks.
 * This class extends the NeuralNetwork class and provides methods for training the network
 * using experiences stored in replay memory.
 */
public class DQLNeuralNetwork extends NeuralNetwork {
    
    /**
     * Constructs a DQLNeuralNetwork with the specified layer sizes.
     *
     * @param sizes An array specifying the number of neurons in each layer of the neural network.
     */
    public DQLNeuralNetwork(int[] sizes) {
        super(sizes);
    }

    /**
     * Trains the neural network using a batch of experiences from the replay memory.
     * Updates the network based on the Q-learning algorithm.
     *
     * @param memory The replay memory containing past experiences.
     * @param batchSize The number of experiences to sample from the replay memory for each training step.
     * @param gamma The discount factor used in Q-learning to balance immediate and future rewards.
     */
    public void train(ReplayMemory memory, int batchSize, double gamma) {
        if (memory.size() < batchSize) {
            return;
        }

        ReplayMemory.Experience[] batch = memory.sample(batchSize);
        for (ReplayMemory.Experience experience : batch) {
            double[] qValues = predict(experience.state);
            double[] nextQValues = predict(experience.nextState);

            double target = experience.reward;
            if (!experience.done) {
                target += gamma * Arrays.stream(nextQValues).max().orElse(0);
            }

            qValues[0] = target;  // Angle
            qValues[1] = target;  // Speed

            trainSingle(experience.state, qValues);
        }

        System.out.println("Training completed with batch size: " + batchSize);
    }
}
