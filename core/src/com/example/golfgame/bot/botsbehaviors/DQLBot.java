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
    private float deltaAngle;

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

        if (Math.random() < epsilon) {
            action = new double[]{ Math.random() * 2 * Math.PI, Math.random() * 10 }; // Случайный угол и сила
        } else {
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

        // Получаем новое состояние после удара
        BallState newBallState = game.getGolfGameScreen().getBallState();
        double newRelativeX = goal.getX() - newBallState.getX();
        double newRelativeY = goal.getY() - newBallState.getY();

        double[] nextState = { newBallState.getX(), newBallState.getY(), newRelativeX, newRelativeY };

        // Определение вознаграждения (например, отрицательное за каждый шаг, положительное за достижение цели)
        double reward = calculateReward(newBallState, goal);
        boolean done = checkIfDone(newBallState, goal);

        memory.add(new ReplayMemory.Experience(state, lastAction, reward, nextState, done));
        neuralNetwork.train(memory, batchSize, gamma);

        if (epsilon > epsilonMin) {
            epsilon *= epsilonDecay;
        }
    }

    private double calculateReward(BallState ball, BallState goal) {
        // Пример простой функции вознаграждения
        double distanceToGoal = Math.sqrt(Math.pow(goal.getX() - ball.getX(), 2) + Math.pow(goal.getY() - ball.getY(), 2));
        if (distanceToGoal < 1.0) {
            return 100.0;  // Большое положительное вознаграждение за достижение цели
        } else {
            return -1.0;  // Отрицательное вознаграждение за каждый шаг
        }
    }

    private boolean checkIfDone(BallState ball, BallState goal) {
        // Условие завершения эпизода
        double distanceToGoal = Math.sqrt(Math.pow(goal.getX() - ball.getX(), 2) + Math.pow(goal.getY() - ball.getY(), 2));
        return distanceToGoal < 1.0;
    }

    private float smoothAngleTransition(float currentAngle, float targetAngle) {
        deltaAngle = targetAngle - currentAngle;

        if (deltaAngle > Math.PI) {
            deltaAngle -= 2 * Math.PI;
        } else if (deltaAngle < -Math.PI) {
            deltaAngle += 2 * Math.PI;
        }

        float smoothingFactor = 0.1f;
        return currentAngle + smoothingFactor * deltaAngle;
    }
}