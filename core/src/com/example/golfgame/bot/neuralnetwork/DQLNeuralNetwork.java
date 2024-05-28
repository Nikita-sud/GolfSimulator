package com.example.golfgame.bot.neuralnetwork;

import java.util.Arrays;
import com.example.golfgame.utils.ReplayMemory;

public class DQLNeuralNetwork extends NeuralNetwork {
    public DQLNeuralNetwork(int[] sizes) {
        super(sizes);
    }

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