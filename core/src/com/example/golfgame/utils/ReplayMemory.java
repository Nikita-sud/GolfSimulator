package com.example.golfgame.utils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.List;

public class ReplayMemory {
    private Queue<Experience> memory;
    private int capacity;
    private Random random;

    public static class Experience {
        public double[] state;
        public double[] action;  // Array to store both angle and strength
        public double reward;
        public double[] nextState;
        public boolean done;

        public Experience(double[] state, double[] action, double reward, double[] nextState, boolean done) {
            this.state = state;
            this.action = action;
            this.reward = reward;
            this.nextState = nextState;
            this.done = done;
        }
    }

    public ReplayMemory(int capacity) {
        this.capacity = capacity;
        this.memory = new LinkedList<>();
        this.random = new Random();
    }

    public void add(Experience experience) {
        if (memory.size() >= capacity) {
            memory.poll(); // Remove the oldest experience if the capacity is exceeded
        }
        memory.add(experience);
    }

    public Experience[] sample(int batchSize) {
        List<Experience> experiences = new ArrayList<>(memory);
        Experience[] batch = new Experience[batchSize];
        for (int i = 0; i < batchSize; i++) {
            int index = random.nextInt(experiences.size());
            batch[i] = experiences.remove(index);
        }
        return batch;
    }

    public int size() {
        return memory.size();
    }
}