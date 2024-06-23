package com.example.golfgame.bot.botsbehaviors;

import com.badlogic.gdx.graphics.Camera;
import com.example.golfgame.GolfGame;
import com.example.golfgame.bot.BotBehavior;
import com.example.golfgame.utils.BallState;

/**
 * The RuleBasedBot class implements the BotBehavior interface and defines
 * a rule-based approach for setting the direction and hitting the ball in the golf game.
 * It adjusts the camera angle and hits the ball based on terrain analysis and smooth angle transitions.
 */
public class RuleBasedBot implements BotBehavior {
    private float deltaAngle;

    private static final float firstOrderTerrainConstant = 1.4f;
    private static final float secondOrderTerrainConstant = firstOrderTerrainConstant / 2;

    /**
     * Sets the direction for the bot by adjusting the camera angle based on the target angle.
     *
     * @param game the GolfGame instance containing the game state and settings
     * @return the adjusted camera angle
     */
    @Override
    public float setDirection(GolfGame game) {
        float targetAngle = findTargetAngle(game);

        // Get the current camera angle
        float currentAngle = game.getGolfGameScreen().getCameraAngle();
        // Smoothly adjust the camera angle
        float adjustedAngle = smoothAngleTransition(currentAngle, targetAngle);

        return adjustedAngle;
    }

    /**
     * Finds the target angle based on the position of the goal and ball, 
     * taking into account the terrain slopes and average slopes.
     *
     * @param game the GolfGame instance containing the game state and settings
     * @return the target angle
     */
    public float findTargetAngle(GolfGame game) {
        // Get goal state and normalize
        BallState goal = game.getGolfGameScreen().getGoalState().copy();
        BallState ball = initiBallState;
        goal.setX(goal.getX() - ball.getX()); // Adjust relative to ball's position
        goal.setY(goal.getY() - ball.getY()); // Adjust relative to ball's position
        goal.positionNor();

        // Get camera direction and normalize
        Camera cam = game.getGolfGameScreen().getMainCamera();
        BallState camState = new BallState(cam.direction.x, cam.direction.z, 0, 0);
        camState.positionNor();

        // Calculate the straight target angle using atan2 for accurate angle direction
        float straightTargetAngle = (float) Math.PI + (float) Math.atan2(goal.getY(), goal.getX());

        // Calculate the average slope from ball to hole in orthogonal direction to the camera
        double orthoSlopeAtStart = game.getGolfGameScreen().getPhysicsEngine().derivative(ball.getX(), ball.getY(), -Math.cos(straightTargetAngle), Math.sin(straightTargetAngle));
        double orthoSlopeAtEnd = game.getGolfGameScreen().getPhysicsEngine().derivative(goal.getX(), goal.getY(), -Math.cos(straightTargetAngle), Math.sin(straightTargetAngle));
        double avgSlope = (orthoSlopeAtEnd + orthoSlopeAtStart) / 2;

        // Calculate the average second derivative slope
        double orthoSecondSlopeAtStart = game.getGolfGameScreen().getPhysicsEngine().secondDerivative(ball.getX(), ball.getY(), -Math.cos(straightTargetAngle + (float) avgSlope * firstOrderTerrainConstant), Math.sin(straightTargetAngle + (float) avgSlope * firstOrderTerrainConstant));
        double orthoSecondSlopeAtEnd = game.getGolfGameScreen().getPhysicsEngine().secondDerivative(goal.getX(), goal.getY(), -Math.cos(straightTargetAngle + (float) avgSlope * firstOrderTerrainConstant), Math.sin(straightTargetAngle + (float) avgSlope * firstOrderTerrainConstant));
        double avgSecondSlope = (orthoSecondSlopeAtEnd + orthoSecondSlopeAtStart) / 2;

        // Calculate the target angle based on the average slopes
        float targetAngle = straightTargetAngle + firstOrderTerrainConstant * (float) avgSlope + secondOrderTerrainConstant * (float) avgSecondSlope;

        return targetAngle;
    }

    /**
     * Smoothly transitions the current angle towards the target angle.
     *
     * @param currentAngle the current camera angle
     * @param targetAngle the target angle to transition towards
     * @return the adjusted angle for a smooth transition
     */
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

    /**
     * Hits the ball if the angle adjustment is within a small threshold.
     *
     * @param game the GolfGame instance containing the game state and settings
     */
    @Override
    public void hit(GolfGame game) {
        if (Math.abs(deltaAngle) < 0.005) {
            game.getGolfGameScreen().performHit(9.9f);
        }
    }
}