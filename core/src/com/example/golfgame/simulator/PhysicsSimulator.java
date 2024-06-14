package com.example.golfgame.simulator;

import java.util.Arrays;
import java.util.Random;

import com.example.golfgame.physics.PhysicsEngine;
import com.example.golfgame.physics.ODE.RungeKutta;
import com.example.golfgame.utils.BallState;
import com.example.golfgame.utils.Function;

public class PhysicsSimulator {

    private PhysicsEngine engine;
    private BallState ball;
    private static Random random = new Random(2024);

    public PhysicsSimulator(Function heightFunction){
        engine = new PhysicsEngine(new RungeKutta(), heightFunction);
        ball = new BallState(0, 0, 0, 0);
    }

    /**
     * performs hit simulation
     * @param velocityMagnitude
     * @param angle
     * @return
     */
    public BallState hit(float velocityMagnitude, float angle){
        // I will set the velocities properly later
        ball.setVx(velocityMagnitude);
        ball.setVy(velocityMagnitude);
        while (!engine.isAtRest(ball)){
            engine.update(ball, 0.01);
        }
        return ball;
    }

    /**
     * performs number of hit simulations from [0,0] or position set by setPosition method
     * @param velocityMagnitudes
     * @param angles
     * @return
     */
    public BallState[] hit(float[] velocityMagnitudes, float[] angles){
        // TODO: Multithreading
        BallState[] res = new BallState[velocityMagnitudes.length]; 
        for (int i = 0; i<velocityMagnitudes.length; i++){
            res[i] = hit(velocityMagnitudes[i], angles[i]);
            ball.setX(0);
            ball.setY(0);
        }
        return res;
    }

    /**
     * performs number of hit simulations within a certain unit radius of the goal
     * @param velocityMagnitudes
     * @param angles
     * @return
     */
    public BallState[] randomHits(int n, BallState goal, float radius){
        BallState[] res = new BallState[n];
        for (int i = 0; i<n; i++){
            float ballX = random.nextFloat(-radius, radius);
            float ballY = random.nextBoolean()? (float)Math.sqrt(radius*radius-ballX*ballX): -(float)Math.sqrt(radius*radius-ballX*ballX);
            ballX += goal.getX();
            ballY += goal.getY();
            ball.setX(ballX);
            ball.setY(ballY);
            float velocityMagnitude = random.nextFloat(1, 5);
            float angle = random.nextFloat(0f, (float)(2*Math.PI));
            res[i] = hit(velocityMagnitude, angle);
        }
        return res;
    }


    /**
     * sets ball Position
     * @param x
     * @param y
     */
    public void setPosition(float x, float y){
        ball.setX(x);
        ball.setY(y);
    }

    
    public static void main(String[] args){
        Function h = new Function("0.1x^2+0.1y^2+10", "x", "y");
        PhysicsSimulator sim = new PhysicsSimulator(h);
       
        
        System.out.println(Arrays.toString(sim.randomHits(10, new BallState(0, 0, 0, 0), 10)));
    }

    
}
