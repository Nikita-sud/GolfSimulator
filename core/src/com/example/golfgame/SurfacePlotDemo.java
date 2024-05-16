package com.example.golfgame;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Path2D;

public class SurfacePlotDemo extends JFrame {

    public SurfacePlotDemo() {
        // Set up the JFrame
        setTitle("3D Surface Plot");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Create the SurfaceModel
        SurfaceModel model = new SurfaceModel();
        model.setCalcDivisions(50);  // Set the number of divisions for the calculation grid
        model.setXMin(-10);          // Set the minimum x value
        model.setXMax(10);           // Set the maximum x value
        model.setYMin(-10);          // Set the minimum y value
        model.setYMax(10);           // Set the maximum y value

        // Define the function to be plotted: sin(0.3x) * cos(0.3y) + 0.8
        model.setMapper((x, y) -> Math.sin(0.3 * x) * Math.cos(0.3 * y) + 0.8);

        // Create the SurfaceCanvas and add it to the JFrame
        SurfaceCanvas canvas = new SurfaceCanvas(model);
        add(canvas);
    }

    public static void main(String[] args) {
        // Create and display the SurfacePlotDemo
        SurfacePlotDemo plot = new SurfacePlotDemo();
        plot.setVisible(true);
    }
}

// Define the SurfaceModel class
class SurfaceModel {
    private int calcDivisions;
    private double xMin, xMax, yMin, yMax;
    private SurfaceMapper mapper;

    public void setCalcDivisions(int calcDivisions) {
        this.calcDivisions = calcDivisions;
    }

    public int getCalcDivisions() {
        return calcDivisions;
    }

    public void setXMin(double xMin) {
        this.xMin = xMin;
    }

    public void setXMax(double xMax) {
        this.xMax = xMax;
    }

    public void setYMin(double yMin) {
        this.yMin = yMin;
    }

    public void setYMax(double yMax) {
        this.yMax = yMax;
    }

    public void setMapper(SurfaceMapper mapper) {
        this.mapper = mapper;
    }

    public double[][] calculateSurface() {
        double[][] surface = new double[calcDivisions + 1][calcDivisions + 1];
        double xStep = (xMax - xMin) / calcDivisions;
        double yStep = (yMax - yMin) / calcDivisions;

        for (int i = 0; i <= calcDivisions; i++) {
            double x = xMin + i * xStep;
            for (int j = 0; j <= calcDivisions; j++) {
                double y = yMin + j * yStep;
                surface[i][j] = mapper.map(x, y);
            }
        }

        return surface;
    }
}

// Define the SurfaceMapper interface
interface SurfaceMapper {
    double map(double x, double y);
}

// Define the SurfaceCanvas class
class SurfaceCanvas extends JPanel {
    private SurfaceModel model;

    public SurfaceCanvas(SurfaceModel model) {
        this.model = model;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // Enable anti-aliasing for smoother lines
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        double[][] surface = model.calculateSurface();
        int width = getWidth();
        int height = getHeight();
        double xStep = (double) width / model.getCalcDivisions();
        double yStep = (double) height / model.getCalcDivisions();

        // 3D perspective parameters
        double scale = 0.5;
        double angleX = Math.PI / 4;
        double angleY = Math.PI / 4;

        // Draw the surface with perspective transformation
        for (int i = 0; i < model.getCalcDivisions(); i++) {
            for (int j = 0; j < model.getCalcDivisions(); j++) {
                double z1 = surface[i][j];
                double z2 = surface[i + 1][j];
                double z3 = surface[i][j + 1];
                double z4 = surface[i + 1][j + 1];

                double x1 = i * xStep;
                double y1 = j * yStep;
                double x2 = (i + 1) * xStep;
                double y2 = j * yStep;
                double x3 = i * xStep;
                double y3 = (j + 1) * yStep;
                double x4 = (i + 1) * xStep;
                double y4 = (j + 1) * yStep;

                // Apply perspective transformation
                Point p1 = project(x1, y1, z1, scale, angleX, angleY, width, height);
                Point p2 = project(x2, y2, z2, scale, angleX, angleY, width, height);
                Point p3 = project(x3, y3, z3, scale, angleX, angleY, width, height);
                Point p4 = project(x4, y4, z4, scale, angleX, angleY, width, height);

                // Draw the surface polygon
                Path2D path = new Path2D.Double();
                path.moveTo(p1.x, p1.y);
                path.lineTo(p2.x, p2.y);
                path.lineTo(p4.x, p4.y);
                path.lineTo(p3.x, p3.y);
                path.closePath();

                g2d.setColor(new Color(100, 150, 200));
                g2d.fill(path);
                g2d.setColor(Color.BLACK);
                g2d.draw(path);
            }
        }
    }

    private Point project(double x, double y, double z, double scale, double angleX, double angleY, int width, int height) {
        double cx = width / 2;
        double cy = height / 2;

        double xRot = x * Math.cos(angleX) - y * Math.sin(angleX);
        double yRot = x * Math.sin(angleX) + y * Math.cos(angleX);

        double xProj = xRot * scale / (scale + z);
        double yProj = yRot * scale / (scale + z);

        return new Point((int) (cx + xProj), (int) (cy - yProj));
    }
}
