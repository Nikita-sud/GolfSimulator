package com.example.golfgame;
public class BallState {
    private double x=0, y=0; 
    private double vx=0, vy=0;

    public BallState(double x, double y, double vx, double vy) {
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
    }

    public void setX(double x){
        this.x = x;
    }
    public void setY(double y){
        this.y = y;
    }
    public void setVx(double vx){
        this.vx = vx;
    }
    public void setVy(double vy){
        this.vy = vy;
    }

    public double getX(){
        return x;
    }
    public double getY(){
        return y;
    }
    public double getVx(){
        return vx;
    }
    public double getVy(){
        return vy;
    }

}
