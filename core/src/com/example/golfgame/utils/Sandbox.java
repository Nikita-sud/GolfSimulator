package com.example.golfgame.utils;

public class Sandbox {

    private float xLowBound;
    private float xHighBound;
    private float yLowBound;
    private float yHighBound;

    /**
     * Stores abstract representation of a sandbox. 
     * Bounds are read-only.
     * @param xLowBound start of Sandbox on spacial x axis
     * @param xHighBound end of Sandbox in spacial x axis
     * @param yLowBound start of Sandbox on spacial y axis
     * @param yHightBound end of Sandbox on spacial y axis
     */
    public Sandbox(float xLowBound, float xHighBound, float yLowBound, float yHighBound){
        this.xLowBound = xLowBound;
        this.xHighBound = xHighBound;
        this.yLowBound = yLowBound;
        this.yHighBound = yHighBound;
    }

    public float getXLowBound(){
        return xLowBound;
    }

    public float getXHighBound(){
        return xHighBound;
    }

    public float getYLowBound(){
        return yLowBound;
    }

    public float getYHighBound(){
        return yHighBound;
    }
}
