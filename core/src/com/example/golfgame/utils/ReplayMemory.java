package com.example.golfgame.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ReplayMemory {
    private int capacity;
    private List<Experience> experiences;
    private Random random;

    public ReplayMemory(int capacity) {
        this.capacity = capacity;
        this.experiences = new ArrayList<>();
        this.random = new Random();
    }

    public void add(Experience experience) {
        if (experiences.size() >= capacity) {
            experiences.remove(0);
        }
        experiences.add(experience);
    }

    public List<Experience> sample(int batchSize) {
        List<Experience> batch = new ArrayList<>();
        for (int i = 0; i < batchSize; i++) {
            int index = random.nextInt(experiences.size());
            batch.add(experiences.get(index));
        }
        return batch;
    }

    public static class Experience {
        public double[] state;
        public double[] action;
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
}