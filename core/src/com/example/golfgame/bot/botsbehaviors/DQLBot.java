package com.example.golfgame.bot.botsbehaviors;

import com.example.golfgame.GolfGame;
import com.example.golfgame.bot.BotBehavior;
import com.example.golfgame.bot.neuralnetwork.DQLNeuralNetwork;
import com.example.golfgame.utils.ReplayMemory;
import com.example.golfgame.utils.BallState;

public class DQLBot implements BotBehavior {
    private DQLNeuralNetwork neuralNetwork;
    private ReplayMemory memory;
    private double epsilon;
    private double epsilonDecay;
    private double epsilonMin;
    private double gamma;
    private int batchSize;
    private double[] lastAction;
    private float deltaAngle = Float.MAX_VALUE;
    private boolean waitingForStop = false;
    private double[] currentState;
    private double[] currentAction;

    public DQLBot(DQLNeuralNetwork neuralNetwork, int memoryCapacity, double epsilon, double epsilonDecay, double epsilonMin, double gamma, int batchSize) {
        this.neuralNetwork = neuralNetwork;
        this.memory = new ReplayMemory(memoryCapacity);
        this.epsilon = epsilon;
        this.epsilonDecay = epsilonDecay;
        this.epsilonMin = epsilonMin;
        this.gamma = gamma;
        this.batchSize = batchSize;
    }

    @Override
    public float setDirection(GolfGame game) {
        BallState ball = game.getGolfGameScreen().getBallState();
        BallState goal = game.getGolfGameScreen().getGoalState();

        double relativeX = goal.getX() - ball.getX();
        double relativeY = goal.getY() - ball.getY();

        double[] state = { ball.getX(), ball.getY(), relativeX, relativeY }; 
        double[] action;

        if(Math.abs(deltaAngle)>0.001){
            if (Math.random() < epsilon) {
                action = new double[]{ Math.random() * 2 * Math.PI, Math.random() * 10 }; // Случайный угол и сила
            } else {
                action = neuralNetwork.predict(state);
            }
        }else{
            action = neuralNetwork.predict(state);
        }
        lastAction = action;

        float targetAngle = (float) action[0];
        float currentAngle = game.getGolfGameScreen().getCameraAngel();
        float adjustedAngle = smoothAngleTransition(currentAngle, targetAngle);
        return adjustedAngle;
    }

    @Override
    public void hit(GolfGame game) {

            BallState ball = game.getGolfGameScreen().getBallState();
            BallState goal = game.getGolfGameScreen().getGoalState();

            // Используем относительные координаты мяча и цели
            double relativeX = goal.getX() - ball.getX();
            double relativeY = goal.getY() - ball.getY();

            // Текущее состояние
            double[] state = { ball.getX(), ball.getY(), relativeX, relativeY };

            // Выполнение удара
            game.getGolfGameScreen().performHit((float) lastAction[1]);  // Используем силу удара из action[1]

            // Устанавливаем флаг ожидания и сохраняем текущее состояние и действие
            waitingForStop = true;
            currentState = state;
            currentAction = lastAction;
        
    }

    public double calculateReward(BallState ball, BallState goal) {
        // Пример простой функции вознаграждения
        double distanceToGoal = Math.sqrt(Math.pow(goal.getX() - ball.getX(), 2) + Math.pow(goal.getY() - ball.getY(), 2));
        if (distanceToGoal < 1.0) {
            return 100.0;  // Большое положительное вознаграждение за достижение цели
        } else {
            return -1.0;  // Отрицательное вознаграждение за каждый шаг
        }
    }

    public boolean checkIfDone(BallState ball, BallState goal) {
        // Условие завершения эпизода
        double distanceToGoal = Math.sqrt(Math.pow(goal.getX() - ball.getX(), 2) + Math.pow(goal.getY() - ball.getY(), 2));
        return distanceToGoal < 1.0;
    }

    private float smoothAngleTransition(float currentAngle, float targetAngle) {
        deltaAngle = targetAngle - currentAngle;

        // Ensure the transition is within -PI to PI for shortest rotation direction
        if (deltaAngle > Math.PI) {
            deltaAngle -= 2 * Math.PI;
        } else if (deltaAngle < -Math.PI) {
            deltaAngle += 2 * Math.PI;
        }

        // Apply a smoothing factor (adjust as necessary for smooth transition)
        float smoothingFactor = 0.1f;
        return currentAngle + smoothingFactor * deltaAngle;
    }

    public boolean isWaitingForStop() {
        return waitingForStop;
    }
    
    public void updateMemoryAndTrain(double[] nextState, double reward, boolean done) {
        memory.add(new ReplayMemory.Experience(currentState, currentAction, reward, nextState, done));
        neuralNetwork.train(memory, batchSize, gamma);
        waitingForStop = false;
    
        if (epsilon > epsilonMin) {
            epsilon *= epsilonDecay;
        }
    }
    
}