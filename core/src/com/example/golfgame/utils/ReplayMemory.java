package com.example.golfgame.utils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.List;

/**
 * A class representing replay memory for storing experiences in reinforcement learning.
 * The replay memory helps in storing past experiences to be used later for training a model.
 */
public class ReplayMemory {
    private Queue<Experience> memory;
    private int capacity;
    private Random random;

    /**
     * A nested static class representing a single experience.
     * An experience contains the state, action, reward, next state, and done flag.
     */
    public static class Experience {
        public double[] state;
        public double[] action;  // Array to store both angle and strength
        public double reward;
        public double[] nextState;
        public boolean done;

        /**
         * Constructs an Experience object with the specified state, action, reward, next state, and done flag.
         *
         * @param state     The state at the time of the experience.
         * @param action    The action taken during the experience.
         * @param reward    The reward received after taking the action.
         * @param nextState The state after taking the action.
         * @param done      A flag indicating whether the episode has ended.
         */
        public Experience(double[] state, double[] action, double reward, double[] nextState, boolean done) {
            this.state = state;
            this.action = action;
            this.reward = reward;
            this.nextState = nextState;
            this.done = done;
        }
    }

    /**
     * Constructs a ReplayMemory object with the specified capacity.
     *
     * @param capacity The maximum number of experiences the replay memory can hold.
     */
    public ReplayMemory(int capacity) {
        this.capacity = capacity;
        this.memory = new LinkedList<>();
        this.random = new Random();
    }

    /**
     * Adds a new experience to the replay memory.
     * If the memory exceeds its capacity, the oldest experience is removed.
     *
     * @param experience The experience to be added to the replay memory.
     */
    public void add(Experience experience) {
        if (memory.size() >= capacity) {
            memory.poll(); // Remove the oldest experience if the capacity is exceeded
        }
        memory.add(experience);
    }

    /**
     * Samples a batch of experiences from the replay memory.
     *
     * @param batchSize The number of experiences to sample.
     * @return An array of sampled experiences.
     */
    public Experience[] sample(int batchSize) {
        List<Experience> experiences = new ArrayList<>(memory);
        Experience[] batch = new Experience[batchSize];
        for (int i = 0; i < batchSize; i++) {
            int index = random.nextInt(experiences.size());
            batch[i] = experiences.remove(index);
        }
        return batch;
    }

    /**
     * Returns the current size of the replay memory.
     *
     * @return The number of experiences currently stored in the replay memory.
     */
    public int size() {
        return memory.size();
    }
}
