package com.example.golfgame;

import com.badlogic.gdx.graphics.Camera;
import com.example.golfgame.utils.BallState;

import java.awt.Robot;
import java.awt.AWTException;
import java.awt.event.KeyEvent;

import javax.security.auth.kerberos.KeyTab;

public class WallE {

    private GolfGame game;
    private Robot robot;
    private boolean hitAllowed = true;

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
        goal.setY(goal.getY() - ball.getY()); // Adjust relative to ball's position
        goal.positionNor();
    
        // Get camera direction and normalize
        Camera cam = game.getGolfGameScreen().getMainCamera();
        BallState camState = new BallState(cam.direction.x, cam.direction.z, 0, 0);
        camState.positionNor();
    
        // Calculate the target angle using atan2 for accurate angle direction
        float targetAngle = (float)Math.PI+(float) Math.atan2(goal.getY(), goal.getX());
    
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
    
    public void hit(){
        hitAllowed = false;
        Thread keyPressThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                    for (int t = 0; t<1000; t++){
                        robot.keyPress(KeyEvent.VK_SPACE);
                    }
                    while (game.getGolfGameScreen().getCurrentSpeedBar() < 9.9f) {
                        robot.keyPress(KeyEvent.VK_SPACE);
                        Thread.sleep(10); // Adding sleep to prevent excessive CPU usage
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
    
}
