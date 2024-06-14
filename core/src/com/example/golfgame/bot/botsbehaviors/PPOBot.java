package com.example.golfgame.bot.botsbehaviors;

import com.example.golfgame.GolfGame;
import com.example.golfgame.bot.BotBehavior;
import com.example.golfgame.bot.agents.PPOAgent;
import com.example.golfgame.utils.BallState;

public class PPOBot implements BotBehavior {
    private PPOAgent agent;
    private double[] lastAction;
    private boolean waitingForStop = false;
    private double[][] currentState; // Updated to handle multimodal input
    private double[] currentAction;

    public PPOBot(int height, int width, int channels, int numNumericFeatures, int outputSize, int memoryCapacity, double epsilon, double gamma, int batchSize, int updateSteps, double clipValue) {
        this.agent = new PPOAgent(height, width, channels, numNumericFeatures, outputSize, memoryCapacity, epsilon, gamma, batchSize, updateSteps, clipValue);
    }

    @Override
    public float setDirection(GolfGame game) {
        BallState ball = game.getGolfGameScreen().getBallState();
        BallState goal = game.getGolfGameScreen().getGoalState();

        double relativeX = goal.getX() - ball.getX();
        double relativeY = goal.getY() - ball.getY();

        double[] numericState = { ball.getX(), ball.getY(), relativeX, relativeY };

        // Placeholder for image state
        double[] imageState = extractImageState(game);

        double[][] state = { imageState, numericState };
        double[] action = agent.chooseAction(imageState, numericState);
        lastAction = action;

        float targetAngle = (float) action[0];
        return targetAngle;
    }

    @Override
    public void hit(GolfGame game) {
        BallState ball = game.getGolfGameScreen().getBallState();
        BallState goal = game.getGolfGameScreen().getGoalState();

        double relativeX = goal.getX() - ball.getX();
        double relativeY = goal.getY() - ball.getY();

        double[] numericState = { ball.getX(), ball.getY(), relativeX, relativeY };

        // Placeholder for image state
        double[] imageState = extractImageState(game);

        double[][] state = { imageState, numericState };

        game.getGolfGameScreen().performHit((float) lastAction[1]);

        waitingForStop = true;
        currentState = state;
        currentAction = lastAction;
    }

    public void updateMemoryAndTrain(double[] nextImageState, double[] nextNumericState, double reward, boolean done) {
        double[][] nextState = { nextImageState, nextNumericState };
        agent.storeTransition(currentState[0], currentState[1], currentAction, reward, nextState[0], nextState[1], done);
        agent.update();
        waitingForStop = false;
    }

    public boolean isWaitingForStop() {
        return waitingForStop;
    }

    private double[] extractImageState(GolfGame game) {
        // Implement the logic to extract image state from the game
        // For now, we return a placeholder array
        return new double[200];
    }
}