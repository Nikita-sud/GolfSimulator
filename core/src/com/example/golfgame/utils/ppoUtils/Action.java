package com.example.golfgame.utils.ppoUtils;

public class Action {
    private double angle;
    private double force;
    public Action(double angle,double force){
        this.angle = angle;
        this.force = force;
    }
    
    public void setAngle(double angle){
        this.angle = angle;
    }

    public void setForce(double force){
        this.force = force;
    }

    public double getAngle(){
        return angle;
    }

    public double getForce(){
        return force;
    }

    @Override
    public String toString() {
        return "Action{" +
                "angle=" + angle +
                ", force=" + force +
                '}';
    }
}
