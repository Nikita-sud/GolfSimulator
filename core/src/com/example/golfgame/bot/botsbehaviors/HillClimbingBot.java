package com.example.golfgame.bot.botsbehaviors;

import java.util.List;

import com.example.golfgame.GolfGame;
import com.example.golfgame.bot.BotBehavior;
import com.example.golfgame.simulator.PhysicsSimulator;
import com.example.golfgame.utils.BallState;

public class HillClimbingBot implements BotBehavior{

    private float hitPower;
    private float angle;

    private static final float DELTAHITPOWER = 0.2f;
    private static final float DELTAANGLE = 0.2f;

    public HillClimbingBot(){
        hitPower = 3;
        angle = 0;
    }

    @Override
    public float setDirection(GolfGame game){
        climb(game);
        return angle;
    }

    @Override 
    public void hit(GolfGame game){
        game.getGolfGameScreen().performHit(hitPower);
    }

    private void climb(GolfGame game){
        BallState goal = game.getGolfGameScreen().getGoalState();
        PhysicsSimulator simulator = new PhysicsSimulator(game.getGolfGameScreen().getHeightFunction(), goal);
        while (true){
            BallState curSimResult = simulator.hit(hitPower, angle);
            // Try climbing in 4 directions: increasing/decreasing angle/hitPower
            // Increasing angle && Increasing hitPower
            BallState doubleIncreaseResult = simulator.hit(hitPower+DELTAHITPOWER, angle+DELTAANGLE);
            // Increasing angle && Decreasing hit Power
            BallState angleIncreaseResult = simulator.hit(hitPower-DELTAHITPOWER, angle+DELTAANGLE);
            // Increasing hitPower && Decreasing Anglae
            BallState hitPowerIncreaseResult = simulator.hit(hitPower+DELTAHITPOWER, angle-DELTAANGLE);
            // Decreasing hitPower && decreasing angle
            BallState doubleDecreaseResult = simulator.hit(hitPower-DELTAHITPOWER, angle-DELTAANGLE);
            
            BallState bestState = bestState(new BallState[]{doubleDecreaseResult, doubleIncreaseResult, angleIncreaseResult, hitPowerIncreaseResult, curSimResult}, goal);
            if (bestState.epsilonEquals(doubleIncreaseResult, 0.0000001)){
                angle+=DELTAANGLE;
                hitPower+=DELTAHITPOWER;
            }
            else if(bestState.epsilonEquals(doubleIncreaseResult, 0.00000001)){
                angle-=DELTAANGLE;
                hitPower-=DELTAHITPOWER;
            }
            else if(bestState.epsilonEquals(angleIncreaseResult, 0.00000001)){
                angle+=DELTAANGLE;
                hitPower-=DELTAHITPOWER;
            }
            else if(bestState.epsilonEquals(hitPowerIncreaseResult, 0.00000001)){
                angle-=DELTAANGLE;
                hitPower+=DELTAHITPOWER;
            }
            else{
                break;
            }
        }
    }

    private BallState bestState(BallState[] states, BallState goal){
        double bestCloseness = Integer.MIN_VALUE;
        BallState best = null;
        for (BallState state: states){
            if (closeness(state, goal)>bestCloseness){
                bestCloseness = closeness(state, goal);
                best = state;
            }
        }
        return best;
    }

    private float closeness(BallState simResult, BallState goal){
        return (float)(1/simResult.distanceTo(goal));
    }
}
