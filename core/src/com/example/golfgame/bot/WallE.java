package com.example.golfgame.bot;

import com.example.golfgame.GolfGame;
import com.example.golfgame.screens.GolfGameScreen;
import com.example.golfgame.utils.BallState;
import org.jnativehook.GlobalScreen;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

import java.util.logging.Level;
import java.util.logging.Logger;

public class WallE implements NativeKeyListener {

    private volatile GolfGame game;
    private volatile boolean hitAllowed = true;
    private volatile boolean gameOver = false;

    private static final float terrainAdjustmentConstant = 1.2f;

    public WallE(GolfGame game){
        this.game = game;
        try {
            // Отключение логирования jnativehook
            Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
            logger.setLevel(Level.OFF);
            logger.setUseParentHandlers(false);

            GlobalScreen.registerNativeHook();
            GlobalScreen.addNativeKeyListener(this);
        } catch (Exception e) {
            System.out.println("Issue in jnativehook initialization.");
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
    
        // Calculate the straight target angle using atan2 for accurate angle direction
        float straightTargetAngle = (float)Math.PI+(float) Math.atan2(goal.getY(), goal.getX());

        // As a heuristic on our knowledge of the terrain, let's use the average slope from ball to hole in orthogonal direction to the camera.
        // (To calculate the average slope, we assume the height function is differentialble, so the slope in orthogonal direction would be continuous, 
        // so the average slope in orthogonal direction is given by (orthogonalSlope(ballState)+orthgonalSlope(goalState))/2 ).
        
        double orthoSlopeAtStart = game.getGolfGameScreen().getPhysicsEngine().derivative(ball.getX(), ball.getY(), -Math.cos(straightTargetAngle), Math.sin(straightTargetAngle));

        double orthoSlopeAtEnd = game.getGolfGameScreen().getPhysicsEngine().derivative(goal.getX(), goal.getY() , -Math.cos(straightTargetAngle), Math.sin(straightTargetAngle));

        double avgSlope = (orthoSlopeAtEnd+orthoSlopeAtStart)/2;

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
                    for (int t = 0; t < 100 && !gameOver && game.getScreen() instanceof GolfGameScreen; t++) {
                        pressSpaceKey();
                        Thread.sleep(10);
                    }
                    while (!gameOver && game.getGolfGameScreen().getCurrentSpeedBar() < 9.9f && game.getScreen() instanceof GolfGameScreen) {
                        pressSpaceKey();
                        Thread.sleep(10); // Adding sleep to prevent excessive CPU usage
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    releaseSpaceKey();
                    hitAllowed = true;
                }
            }
        });
        keyPressThread.start();
    }

    private void pressSpaceKey() {
        NativeKeyEvent keyPress = new NativeKeyEvent(
            NativeKeyEvent.NATIVE_KEY_PRESSED, 
            System.currentTimeMillis(), 
            0, 
            0,  // rawCode is not always necessary
            NativeKeyEvent.VC_SPACE, 
            NativeKeyEvent.CHAR_UNDEFINED);
        GlobalScreen.postNativeEvent(keyPress);
    }

    private void releaseSpaceKey() {
        NativeKeyEvent keyRelease = new NativeKeyEvent(
            NativeKeyEvent.NATIVE_KEY_RELEASED, 
            System.currentTimeMillis(), 
            0, 
            0,  // rawCode is not always necessary
            NativeKeyEvent.VC_SPACE, 
            NativeKeyEvent.CHAR_UNDEFINED);
        GlobalScreen.postNativeEvent(keyRelease);
    }

    public boolean hitAllowed(){
        return hitAllowed;
    }

    public void gameOver(){
        gameOver = true;
    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent e) {
        // Not used in this implementation
    }

    @Override
    public void nativeKeyReleased(NativeKeyEvent e) {
        // Not used in this implementation
    }

    @Override
    public void nativeKeyTyped(NativeKeyEvent e) {
        // Not used in this implementation
    }
}
