package com.example.golfgame.bot.botsbehavior;

import com.badlogic.gdx.graphics.Camera;
import com.example.golfgame.GolfGame;
import com.example.golfgame.bot.BotBehavior;
import com.example.golfgame.utils.BallState;

public class RuleBasedBot implements BotBehavior {
    private float deltaAngle;


    @Override
    public float setDirection(GolfGame game) {
        // Get goal state and normalize
        BallState goal = game.getGolfGameScreen().getGoalState().copy();
        BallState ball = game.getGolfGameScreen().getBallState().copy();
        goal.setX(goal.getX() - ball.getX()); // Adjust relative to ball's position
        goal.setY(goal.getY() - ball.getY()); // Adjust relative to ball's position
        goal.positionNor();

        // Get camera direction and normalize
        Camera cam = game.getGolfGameScreen().getMainCamera();
        BallState camState = new BallState(cam.direction.x, cam.direction.z, 0, 0);
        camState.positionNor();

        // Calculate the target angle using atan2 for accurate angle direction
        float targetAngle = (float) Math.PI + (float) Math.atan2(goal.getY(), goal.getX());

        // Get the current camera angle
        float currentAngle = game.getGolfGameScreen().getCameraAngel();
        // Smoothly adjust the camera angle
        float adjustedAngle = smoothAngleTransition(currentAngle, targetAngle);;

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
        if (Math.abs(deltaAngle) < 0.005) {
            game.getGolfGameScreen().performHit(7f);
        }
    }

}