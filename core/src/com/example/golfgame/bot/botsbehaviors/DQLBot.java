// package com.example.golfgame.bot.botsbehaviors;

// import java.io.IOException;
// import java.util.Arrays;

// import com.example.golfgame.GolfGame;
// import com.example.golfgame.bot.BotBehavior;
// import com.example.golfgame.bot.neuralnetwork.DQLNeuralNetwork;
// import com.example.golfgame.utils.ReplayMemory;
// import com.example.golfgame.utils.BallState;

// public class DQLBot implements BotBehavior {
//     private DQLNeuralNetwork mainNetwork;
//     private DQLNeuralNetwork targetNetwork;
//     private ReplayMemory memory;
//     private double epsilon;
//     private double epsilonDecay;
//     private double epsilonMin;
//     private double gamma;
//     private int batchSize;
//     private double[] lastAction;
//     private float deltaAngle = Float.MAX_VALUE;
//     private boolean waitingForStop = false;
//     private double[] currentState;
//     private double[] currentAction;
//     private int steps;
//     private final int targetUpdateFrequency = 50;
//     private final int saveNetworkFrequency = 100;

//     public DQLBot(DQLNeuralNetwork mainNetwork, DQLNeuralNetwork targetNetwork, int memoryCapacity, double epsilon, double epsilonDecay, double epsilonMin, double gamma, int batchSize, String mainNetworkFilePath, String targetNetworkFilePath) {
//         this.mainNetwork = mainNetwork;
//         this.targetNetwork = targetNetwork;
//         this.memory = new ReplayMemory(memoryCapacity);
//         this.epsilon = epsilon;
//         this.epsilonDecay = epsilonDecay;
//         this.epsilonMin = epsilonMin;
//         this.gamma = gamma;
//         this.batchSize = batchSize;
//         this.steps = 0;

//         // Загружаем сети из файлов, если они существуют
//         loadNetworks(mainNetworkFilePath, targetNetworkFilePath);
//     }

//     @Override
//     public float setDirection(GolfGame game) {
//         BallState ball = game.getGolfGameScreen().getBallState();
//         BallState goal = game.getGolfGameScreen().getGoalState();

//         double relativeX = goal.getX() - ball.getX();
//         double relativeY = goal.getY() - ball.getY();

//         double[] state = { ball.getX(), ball.getY(), relativeX, relativeY };
//         double[] action;

//         if (Math.random() < epsilon) {
            
//             action = new double[]{(Math.random() * Math.PI*2), Math.max(Math.random() * 5,1)};
//             System.out.println("Random action");
//         } else {
//             action = mainNetwork.predict(state);
//             System.out.println("NN action");
//         }
//         lastAction = action;

//         float targetAngle = (float) action[0];
//         float currentAngle = game.getGolfGameScreen().getCameraAngle();
//         float adjustedAngle = smoothAngleTransition(currentAngle, targetAngle);
//         System.out.println("Set direction: currentAngle=" + currentAngle + ", targetAngle=" + targetAngle + ", adjustedAngle=" + adjustedAngle);
//         return targetAngle;
//     }

//     @Override
//     public void hit(GolfGame game) {
//         BallState ball = game.getGolfGameScreen().getBallState();
//         BallState goal = game.getGolfGameScreen().getGoalState();

//         double relativeX = goal.getX() - ball.getX();
//         double relativeY = goal.getY() - ball.getY();

//         double[] state = { ball.getX(), ball.getY(), relativeX, relativeY };

//         game.getGolfGameScreen().performHit((float) lastAction[1]);

//         waitingForStop = true;
//         currentState = state;
//         currentAction = lastAction;

//         System.out.println("Hit: ball=" + ball + ", goal=" + goal + ", action=" + lastAction[1]);
//     }

//     private double calculateDistanceToGoal(double[] state) {
//         double relativeX = state[2];
//         double relativeY = state[3];
//         return Math.sqrt(Math.pow(relativeX, 2) + Math.pow(relativeY, 2));
//     }

//     public double calculateReward(BallState currentBallState, BallState goal, boolean win, boolean isBallInWater, BallState lastBallState) {
//         double previousDistanceToGoal = Math.sqrt(Math.pow(goal.getX() - lastBallState.getX(), 2) + Math.pow(goal.getY() - lastBallState.getY(), 2));
//         double currentDistanceToGoal = Math.sqrt(Math.pow(goal.getX() - currentBallState.getX(), 2) + Math.pow(goal.getY() - currentBallState.getY(), 2));
        
//         double distanceDifference = previousDistanceToGoal - currentDistanceToGoal;
//         double reward = distanceDifference * 10;
        
//         if (distanceDifference == 0) {
//             reward += 100.0;
//         }
        
//         if (win) {
//             reward += 500.0;
//         }
        
//         if (isBallInWater) {
//             reward -= 100.0;
//         }
        
//         // Нелинейное наказание за отдаление от цели
//         if (distanceDifference < 0) {
//             double penaltyFactor = Math.exp(Math.abs(distanceDifference) / 10.0); // Экспоненциальная функция
//             reward -= penaltyFactor * 10; // Увеличиваем наказание
//         }
        
//         return reward;
//     }

//     public boolean checkIfDone(BallState ball, BallState goal) {
//         double distanceToGoal = calculateDistanceToGoal(new double[]{ball.getX(), ball.getY(), goal.getX() - ball.getX(), goal.getY() - ball.getY()});
//         return distanceToGoal < 1.0 || ball.getX() < 0 || ball.getY() < 0; // Добавить другие условия при необходимости
//     }

//     private float smoothAngleTransition(float currentAngle, float targetAngle) {
//         deltaAngle = targetAngle - currentAngle;
//         if (deltaAngle > Math.PI) {
//             deltaAngle -= 2 * Math.PI;
//         } else if (deltaAngle < -Math.PI) {
//             deltaAngle += 2 * Math.PI;
//         }
//         float smoothingFactor = 0.1f;
//         if (Math.abs(deltaAngle) > Math.PI / 2) {
//             smoothingFactor = 0.5f;
//         }
//         return currentAngle + smoothingFactor * deltaAngle;
//     }

//     public boolean isWaitingForStop() {
//         return waitingForStop;
//     }

//     public void updateMemoryAndTrain(double[] nextState, double reward, boolean done) {
//         memory.add(new ReplayMemory.Experience(currentState, currentAction, reward, nextState, done));
//         mainNetwork.train(memory, batchSize, gamma, targetNetwork);
//         waitingForStop = false;

//         if (epsilon > epsilonMin) {
//             epsilon *= epsilonDecay;
//         }

//         steps++;
//         if (steps % targetUpdateFrequency == 0) {
//             targetNetwork.setWeights(mainNetwork.getWeights()); // Копирование весов из основной сети в целевую
//         }

//         // Сохраняем сети в файлы после определенного количества шагов
//         if (steps % saveNetworkFrequency == 0) {
//             saveNetworks("neuralnetworkinformation/mainNetwork.ser", "neuralnetworkinformation/targetNetwork.ser");
//         }

//         System.out.println("Memory updated and trained. Epsilon: " + epsilon);
//         System.out.println("Reward: " + reward + ", Current State: " + Arrays.toString(currentState) + ", Next State: " + Arrays.toString(nextState));
//         System.out.println("Distance to Goal: " + calculateDistanceToGoal(nextState));
//     }

//     public void saveNetworks(String mainNetworkFilePath, String targetNetworkFilePath) {
//         try {
//             mainNetwork.saveNetwork(mainNetworkFilePath);
//             targetNetwork.saveNetwork(targetNetworkFilePath);
//             System.out.println("Networks saved successfully.");
//         } catch (IOException e) {
//             System.err.println("Failed to save networks: " + e.getMessage());
//         }
//     }

//     public void loadNetworks(String mainNetworkFilePath, String targetNetworkFilePath) {
//         try {
//             DQLNeuralNetwork mainNetworkLoaded = DQLNeuralNetwork.loadNetwork(mainNetworkFilePath);
//             DQLNeuralNetwork targetNetworkLoaded = DQLNeuralNetwork.loadNetwork(targetNetworkFilePath);
            
//             this.mainNetwork.setWeights(mainNetworkLoaded.getWeights());
//             this.mainNetwork.setBiases(mainNetworkLoaded.getBiases());
    
//             this.targetNetwork.setWeights(targetNetworkLoaded.getWeights());
//             this.targetNetwork.setBiases(targetNetworkLoaded.getBiases());
    
//             System.out.println("Networks loaded successfully.");
//         } catch (IOException | ClassNotFoundException e) {
//             System.err.println("Failed to load networks: " + e.getMessage());
//         }
//     }
// }
