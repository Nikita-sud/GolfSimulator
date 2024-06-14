package com.example.golfgame.utils;

public class GolfEnvironment {
    private TerrainManager terrainManager;
    private BallState ballState;
    private BallState goalState;

    public GolfEnvironment(TerrainManager terrainManager, BallState ballState, BallState goalState) {
        this.terrainManager = terrainManager;
        this.ballState = ballState;
        this.goalState = goalState;
    }

    /**
     * Returns the current environment state as a heightmap with marked positions and numeric features.
     *
     * @return An array where the first element is the heightmap and the second element is the numeric features.
     */
    public Object[] getState() {
        double[][] heightMap = terrainManager.getNormalizedMarkedHeightMap(
            (float) ballState.getX(), (float) ballState.getY(),
            (float) goalState.getX(), (float) goalState.getY()
        );

        double[] numericFeatures = {
            ballState.getX(), ballState.getY(),
            goalState.getX() - ballState.getX(), goalState.getY() - ballState.getY()
        };

        return new Object[]{heightMap, numericFeatures};
    }

    /**
     * Updates the ball's state.
     *
     * @param newX The new x-coordinate of the ball.
     * @param newY The new y-coordinate of the ball.
     */
    public void updateBallState(BallState newBallState) {
        ballState = newBallState.copy();
    }

    /**
     * Updates the goal's state.
     *
     * @param newX The new x-coordinate of the goal.
     * @param newY The new y-coordinate of the goal.
     */
    public void updateGoalState(double newX, double newY) {
        goalState.setX(newX);
        goalState.setY(newY);
    }
}