package com.example.golfgame.bot.botsbehaviors;

import com.badlogic.gdx.graphics.Camera;
import com.example.golfgame.GolfGame;
import com.example.golfgame.bot.BotBehavior;
import com.example.golfgame.utils.BallState;

public class AdvancedBot implements BotBehavior {
    private float deltaAngle;

    private static final float firstOrderTerrainConstant = 1.4f;
    private static final float secondOrderTerrainConstant = firstOrderTerrainConstant / 2;
    private static final float MIN_HIT_POWER = 2f;
    private static final float MAX_HIT_POWER = 10f;
    private static final float DISTANCE_THRESHOLD = 50f;
    private static final float CLOSE_DISTANCE_THRESHOLD = 5f;
    private static final float ANGLE_TOLERANCE = 0.005f;
    private static final float SMOOTHING_FACTOR = 0.1f;
    private static final float SLOPE_POWER_ADJUSTMENT = 5.0f; // Adjustment factor for uphill slopes

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

        // Calculate the straight target angle using atan2 for accurate angle direction
        float straightTargetAngle = (float) Math.PI + (float) Math.atan2(goal.getY(), goal.getX());

        // Use the average slope from ball to hole in orthogonal direction to the camera
        double orthoSlopeAtStart = game.getGolfGameScreen().getPhysicsEngine().derivative(ball.getX(), ball.getY(), -Math.cos(straightTargetAngle), Math.sin(straightTargetAngle));
        double orthoSlopeAtEnd = game.getGolfGameScreen().getPhysicsEngine().derivative(goal.getX(), goal.getY(), -Math.cos(straightTargetAngle), Math.sin(straightTargetAngle));
        double avgSlope = (orthoSlopeAtEnd + orthoSlopeAtStart) / 2;

        // Use information about the second derivative
        double orthoSecondSlopeAtStart = game.getGolfGameScreen().getPhysicsEngine().secondDerivative(ball.getX(), ball.getY(), -Math.cos(straightTargetAngle + (float) avgSlope * firstOrderTerrainConstant), Math.sin(straightTargetAngle + (float) avgSlope * firstOrderTerrainConstant));
        double orthoSecondSlopeAtEnd = game.getGolfGameScreen().getPhysicsEngine().secondDerivative(goal.getX(), goal.getY(), -Math.cos(straightTargetAngle + (float) avgSlope * firstOrderTerrainConstant), Math.sin(straightTargetAngle + (float) avgSlope * firstOrderTerrainConstant));
        double avgSecondSlope = (orthoSecondSlopeAtEnd + orthoSecondSlopeAtStart) / 2;

        float targetAngle = straightTargetAngle + firstOrderTerrainConstant * (float) avgSlope + secondOrderTerrainConstant * (float) avgSecondSlope;

        // Get the current camera angle
        float currentAngle = game.getGolfGameScreen().getCameraAngel();
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
        return currentAngle + SMOOTHING_FACTOR * deltaAngle;
    }

    @Override
    public void hit(GolfGame game) {
        // Only hit if the angle is close to the target angle
        if (Math.abs(deltaAngle) < ANGLE_TOLERANCE) {
            BallState goal = game.getGolfGameScreen().getGoalState().copy();
            BallState ball = game.getGolfGameScreen().getBallState().copy();

            float distanceToGoal = (float) Math.hypot(goal.getX() - ball.getX(), goal.getY() - ball.getY());

            // Adjust hit power based on distance to goal
            float hitPower = calculateHitPower(distanceToGoal, ball, goal, game);

            // Perform a hit with calculated power
            game.getGolfGameScreen().performHit(hitPower);
        }
    }

    private float calculateHitPower(float distance, BallState ball, BallState goal, GolfGame game) {
        // Advanced calculation to determine hit power
        float basePower;
        if (distance < CLOSE_DISTANCE_THRESHOLD) {
            // If the ball is very close, use maximum power
            basePower = MAX_HIT_POWER;
        } else if (distance < DISTANCE_THRESHOLD) {
            basePower = MIN_HIT_POWER + (float) (Math.log(distance + 1) / Math.log(DISTANCE_THRESHOLD + 1)) * (MAX_HIT_POWER - MIN_HIT_POWER);
        } else {
            basePower = MAX_HIT_POWER;
        }

        // Adjust power if there's a slope uphill
        double slope = game.getGolfGameScreen().getPhysicsEngine().derivative(ball.getX(), ball.getY(), goal.getX() - ball.getX(), goal.getY() - ball.getY());
        if (slope > 0) { // If uphill, add more power
            basePower += slope * SLOPE_POWER_ADJUSTMENT;
        }

        // Ensure the hit power does not exceed MAX_HIT_POWER
        return Math.min(basePower, MAX_HIT_POWER);
    }
}