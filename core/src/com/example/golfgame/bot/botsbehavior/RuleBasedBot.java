package com.example.golfgame.bot.botsbehavior;


import com.badlogic.gdx.graphics.Camera;
import com.example.golfgame.GolfGame;
import com.example.golfgame.bot.BotBehavior;
import com.example.golfgame.utils.BallState;

public class RuleBasedBot implements BotBehavior {
    private float deltaAngle;

    private static final float firstOrderTerrainConstant = 1.4f;
    private static final float secondOrderTerrainConstant = firstOrderTerrainConstant/2;

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
        float straightTargetAngle = (float)Math.PI+(float) Math.atan2(goal.getY(), goal.getX());

        // As a heuristic on our knowledge of the terrain, let's use the average slope from ball to hole in orthogonal direction to the camera.
        // (To calculate the average slope, we assume the height function is differentialble, so the slope in orthogonal direction would be continuous, 
        // so the average slope in orthogonal direction is given by (orthogonalSlope(ballState)+orthgonalSlope(goalState))/2 ).
        
        double orthoSlopeAtStart = game.getGolfGameScreen().getPhysicsEngine().derivative(ball.getX(), ball.getY(), -Math.cos(straightTargetAngle), Math.sin(straightTargetAngle));

        double orthoSlopeAtEnd = game.getGolfGameScreen().getPhysicsEngine().derivative(goal.getX(), goal.getY() , -Math.cos(straightTargetAngle), Math.sin(straightTargetAngle));

        double avgSlope = (orthoSlopeAtEnd+orthoSlopeAtStart)/2;

        // Let's try also using information about the second derivative (Second slope means second derivative)
        // Like in a taylor approximation, the derivatives on one line should encode a good amount of information about the terrain in general

        double orthoSecondSlopeAtStart = game.getGolfGameScreen().getPhysicsEngine().secondDerivative(ball.getX(), ball.getY(), -Math.cos(straightTargetAngle+(float)avgSlope*firstOrderTerrainConstant), Math.sin(straightTargetAngle+(float)avgSlope*firstOrderTerrainConstant));

        double orthoSecondSlopeAtEnd = game.getGolfGameScreen().getPhysicsEngine().secondDerivative(goal.getX(), goal.getY(), -Math.cos(straightTargetAngle+(float)avgSlope*firstOrderTerrainConstant), Math.sin(straightTargetAngle+(float)avgSlope*firstOrderTerrainConstant));

        double avgSecondSlope = (orthoSecondSlopeAtEnd+orthoSecondSlopeAtStart)/2;

        float targetAngle = straightTargetAngle+firstOrderTerrainConstant*(float)avgSlope+secondOrderTerrainConstant*(float)avgSecondSlope; 

// Get the current camera angle
        float currentAngle = game.getGolfGameScreen().getCameraAngel();

        System.out.println("Target Angle: " + targetAngle);
        System.out.println("Current Angle: " + currentAngle);

        // Smoothly adjust the camera angle
        float adjustedAngle = smoothAngleTransition(currentAngle, targetAngle);
        System.out.println("Adjusted Angle: " + adjustedAngle);

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
        if(Math.abs(deltaAngle)<0.005){
            game.getGolfGameScreen().setBotHitTriggered(true);
        }
    }

}
