package com.example.golfgame.bot;

import com.badlogic.gdx.graphics.Camera;
import com.example.golfgame.GolfGame;
import com.example.golfgame.screens.GolfGameScreen;
import com.example.golfgame.utils.BallState;

import java.awt.Robot;
import java.awt.AWTException;
import java.awt.event.KeyEvent;

public class WallE {

    private GolfGame game;
    private Robot robot;
    private volatile boolean hitAllowed = true;
    private volatile boolean gameOver = false;

    private static final float terrainAdjustmentConstant = 1;

    public WallE(GolfGame game){
        this.game = game;
        try {
            robot = new Robot();
        } catch (AWTException e) {
            System.out.println("Issue in Robot creation.");
            e.printStackTrace();
        }
    }

    public void setDirection() {
        // Get goal state and normalize
        BallState goal = game.getGolfGameScreen().getGoalState().copy();
        BallState ball = game.getGolfGameScreen().getBallState().copy();
        goal.setX(goal.getX() - ball.getX()); // Adjust relative to ball's position
        goal.setY(goal.getY() - ball.getY()); 
        goal.positionNor();
    
        // Get camera 
        Camera cam = game.getGolfGameScreen().getMainCamera();
       
    
        // Calculate the straight target angle using atan2 for accurate angle direction
        float straightTargetAngle = (float)Math.PI+(float) Math.atan2(goal.getY(), goal.getX());

        // As a heuristic on our knowledge of the terrain, let's use the average slope from ball to hole in orthogonal direction to the camera.
        // (To calculate the average slope, we assume the height function is differentialble, so the slope in orthogonal direction would be continuous, 
        // so the average slope in orthogonal direction is given by (orthogonalSlope(ballState)+orthgonalSlope(goalState))/2 ).
        
        double orthoSlopeAtStart = game.getGolfGameScreen().getPhysicsEngine().derivative(ball.getX(), ball.getY(), -cam.direction.z, cam.direction.x);

        double orthoSlopeAtEnd = game.getGolfGameScreen().getPhysicsEngine().derivative(goal.getX(), goal.getY() , -cam.direction.z, cam.direction.x);

        double avgSlope = (orthoSlopeAtEnd+orthoSlopeAtStart)/2;

        System.out.println(avgSlope);

        float targetAngle = straightTargetAngle+terrainAdjustmentConstant*(float)avgSlope;        

        
        // Get the current camera angle
        float currentAngle = game.getGolfGameScreen().getCameraAngel();
    
        // Smoothly adjust the camera angle
        float adjustedAngle = smoothAngleTransition(currentAngle, targetAngle);
    
        // Set the camera angle
        game.getGolfGameScreen().setCameraAngel(adjustedAngle);
    }
    
    private float smoothAngleTransition(float currentAngle, float targetAngle) {
        float deltaAngle = targetAngle - currentAngle;
    
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
    
    public synchronized void hit(){
        hitAllowed = false;
        Thread keyPressThread = new Thread(new Runnable() {
            @Override
            public void run() {
                if(gameOver){
                    return;
                }
                try {
                    Thread.sleep(2000);
                    for (int t = 0; t<100&&!gameOver&&game.getScreen() instanceof GolfGameScreen; t++){
                        robot.keyPress(KeyEvent.VK_SPACE);
                        Thread.sleep(10);
                    }
                    while (!gameOver&&game.getGolfGameScreen().getCurrentSpeedBar() < 9.9f&&game.getScreen() instanceof GolfGameScreen) {
                        robot.keyPress(KeyEvent.VK_SPACE);
                        Thread.sleep(10);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    robot.keyRelease(KeyEvent.VK_SPACE);
                    hitAllowed = true;
                }
            }
        });
        keyPressThread.start();
    }

    public boolean hitAllowed(){
        return hitAllowed;
    }

    public void gameOver(){
        gameOver = true;
    }
}
