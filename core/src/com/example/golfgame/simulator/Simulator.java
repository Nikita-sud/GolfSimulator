package com.example.golfgame.simulator;
import com.badlogic.gdx.*;
import java.util.Random;

import com.badlogic.gdx.assets.AssetManager;
import com.example.golfgame.GolfGame;
import com.example.golfgame.screens.GolfGameScreen;
import com.example.golfgame.utils.BallState;
import com.example.golfgame.utils.Function;

public class Simulator {

    private final Random random = new Random(15306132024l);
    
    private GolfGame game;  
    private GolfGameScreen screen;
    private float maxSpeedThreshold = 100;
    private int lastScore = 0;
    
    public Simulator(GolfGame game){
        this.game = game;
        this.screen = game.getGolfGameScreen();
    }

    /**
     * Simulates hit
     * @param initialVelocity initial velocity of the ball
     * @param angle camera angle
     * @return state of the ball at rest post-hit
     * @throws InfiniteGameException if (suspectedly) the ball will never come to a rest
     */
    public BallState hit(float initialVelocity, float angle) throws InfiniteGameException{
        screen.setCameraAngle(angle);
        screen.performHit(initialVelocity);

        while (lastScore==screen.getScore()){

            if (Math.abs(screen.getBallState().getVx())>maxSpeedThreshold||Math.abs(screen.getBallState().getVy())>maxSpeedThreshold){
                throw new InfiniteGameException("Ball too fast for simulation");
            }
            // Sleep for short time to avoid busy waiting
            try{
                Thread.sleep(10);
            }
            catch(InterruptedException e){
                Thread.currentThread().interrupt();
                throw new RuntimeException("Thread was interrupted", e);
            }
        }
        BallState ballState = screen.getBallState();
        return ballState;
    }

    /**
     * Simulates a number of hits from random positions in a radius of 30 units within the hole
     * Mainly for training data generation purposes
     * 
     * @param intialVelocities intialVelocities of hits
     * @param angle angles of hits
     * @return
     */
    public BallState[] hitRandom(float[] intialVelocities, float[] angle){
        float[] initalXs = new float[intialVelocities.length];
        float[] initialYs = new float[intialVelocities.length];
        for (int i = 0; i<intialVelocities.length; i++){
            initalXs[i] = random.nextFloat(-30, 30);
            initialYs[i] = random.nextBoolean()? -(float)(Math.sqrt(900-initalXs[i])):(float)(Math.sqrt(900-initalXs[i])); // 30^2=900 for pythagoras
            initalXs[i] += screen.getGoalState().getX();
            initialYs[i] += screen.getGoalState().getY();
        }
        return hit(intialVelocities, angle, initalXs, initialYs);
    }

    /**
     * Simulates a number of hits for specified initial positions
     * @param intialVelocities initial velocity after hit
     * @param angles angle at hit-time
     * @param initalXs initial x components of position vectors
     * @param initialYs initial y components of position vectors
     * @return
     */
    public BallState[] hit(float[] intialVelocities, float[] angles, float[] initalXs, float[] initialYs){
        BallState[] results = new BallState[intialVelocities.length];
        for (int i = 0; i<intialVelocities.length; i++){
            setBall(initalXs[i], initialYs[i]);
            try {
                results[i] = hit(intialVelocities[i], angles[i]);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return results;
    }

    /**
     * prepares hit simulation by setting ball to rest at specified position
     * @param x
     * @param y
     */
    public void setBall(float x, float y){
        try {
            screen.setBallState(new BallState(x, y, 0, 0));
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
