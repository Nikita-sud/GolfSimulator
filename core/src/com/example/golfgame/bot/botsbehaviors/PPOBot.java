package com.example.golfgame.bot.botsbehaviors;

import com.example.golfgame.GolfGame;
import com.example.golfgame.bot.BotBehavior;
import com.example.golfgame.bot.agents.PPOAgent;
import com.example.golfgame.utils.Action;
import com.example.golfgame.utils.BallState;
import com.example.golfgame.utils.State;

public class PPOBot implements BotBehavior {
    private PPOAgent agent;
    private float deltaAngle;

    public PPOBot(PPOAgent agent) {
        this.agent = agent;
    }

    @Override
    public float setDirection(GolfGame game) {
        // Get the current state of the game
        BallState ball = game.getGolfGameScreen().getBallState();
        BallState goal = game.getGolfGameScreen().getGoalState();
        double[] stateArray = {ball.getX(), ball.getY(), goal.getX(), goal.getY()};
        State state = new State(stateArray);

        // Use the PPO agent to select an action
        Action action = agent.selectAction(state);

        // Get the angle from the action
        float targetAngle = (float) action.getAngle();

        // Get the current camera angle
        float currentAngle = game.getGolfGameScreen().getCameraAngle();

        // Smoothly adjust the camera angle
        float adjustedAngle = smoothAngleTransition(currentAngle, targetAngle);

        return adjustedAngle;
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

    @Override
    public void hit(GolfGame game) {
        // Get the current state of the game
        BallState ball = game.getGolfGameScreen().getBallState();
        BallState goal = game.getGolfGameScreen().getGoalState();
        double[] stateArray = {ball.getX(), ball.getY(), goal.getX(), goal.getY()};
        State state = new State(stateArray);

        // Use the PPO agent to select an action
        Action action = agent.selectAction(state);

        // Perform the hit action
        game.getGolfGameScreen().performHit((float) action.getForce());
    }
}