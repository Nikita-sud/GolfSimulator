package com.example.golfgame;

import java.util.Random;

public class Weather {

    private double[] wind;
    private double rain;
    private double sun;
    private double windMagnitude;
    private static final Random windRandom = new Random(2024);

    public Weather(double wind, double rain, double sun){
        this.rain = rain;
        this.wind = generateWind(wind);
        this.sun = sun;
        this.windMagnitude = wind;
    }

    public Weather(double wind){
        this.rain = 0;
        this.sun = 1;
        this.wind = generateWind(wind);
        this.windMagnitude = wind;
    }

    /**
     * generates 3-dimensional wind-vector 
     * @param magnitude magnitude of vector
     * @return wind-vector
     */
    private double[] generateWind(double magnitude){
        double x = windRandom.nextDouble(-1,1);
        double y = windRandom.nextDouble(-1,1);
        double z = windRandom.nextDouble(-1,1);
        double total = x+y+z;
        // normalize for given magnitude
        x*=(magnitude/total);
        y*=(magnitude/total);
        z*=(magnitude/total);
        return new double[]{x,y,z};
    }

    public double[] getWind(){
        return wind;
    }

    public double getRain(){
        return rain;
    }

    public double getSun(){
        return sun;
    }

    public double getWindMagnitude(){
        return windMagnitude;
    }

    public Random getRandom(){
        return windRandom;
    }

    public void setWind(double newWind){
        wind = generateWind(newWind);
    }

    public void setRain(double newRain){
        rain = newRain;
    }

    public void setSun(double newSun){
        sun = newSun;
    }




}
