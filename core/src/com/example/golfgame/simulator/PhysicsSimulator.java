package com.example.golfgame.simulator;

import java.util.Arrays;

import com.badlogic.gdx.physics.bullet.dynamics.btMultibodyLink.eFeatherstoneJointType;
import com.example.golfgame.physics.PhysicsEngine;
import com.example.golfgame.physics.ODE.RungeKutta;
import com.example.golfgame.utils.BallState;
import com.example.golfgame.utils.Function;

public class PhysicsSimulator {

    private PhysicsEngine engine;
    private BallState ball;

    public PhysicsSimulator(Function heightFunction){
        engine = new PhysicsEngine(new RungeKutta(), heightFunction );
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
    public BallState[] randomHits(float velocityMagnitudes, float[] angles, BallState goal, float radius){
        //TODO: implement method
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
        Function h = new Function("10", "x", "y");
        PhysicsSimulator sim = new PhysicsSimulator(h);
        System.out.println(sim.hit(4f, (float)Math.PI));
        
        System.out.println(Arrays.toString(sim.hit(null, null)));
    }

    
}
