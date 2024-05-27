package com.example.golfgame.utils;

public class MatrixUtils {
    public static double[][] addBias(double[][] matrix, double[] bias) {
        double[][] result = new double[matrix.length][matrix[0].length];
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[0].length; j++) {
                result[i][j] = matrix[i][j] + bias[j];
            }
        }
        return result;
    }

    public static double[][] multiplyMatrices(double[][] firstMatrix, double[][] secondMatrix) {
        int firstMatrixRows = firstMatrix.length;
        int firstMatrixCols = firstMatrix[0].length;
        int secondMatrixRows = secondMatrix.length;
        int secondMatrixCols = secondMatrix[0].length;

        if (firstMatrixCols != secondMatrixRows) {
            throw new IllegalArgumentException("Matrix multiplication dimensions do not match");
        }

        double[][] result = new double[firstMatrixRows][secondMatrixCols];
        for (int i = 0; i < firstMatrixRows; i++) {
            for (int j = 0; j < secondMatrixCols; j++) {
                for (int k = 0; k < firstMatrixCols; k++) {
                    result[i][j] += firstMatrix[i][k] * secondMatrix[k][j];
                }
            }
        }
        return result;
    }

    public static double[][] transpose(double[][] matrix) {
        int rows = matrix.length;
        int cols = matrix[0].length;
        double[][] transposedMatrix = new double[cols][rows];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                transposedMatrix[j][i] = matrix[i][j];
            }
        }

        return transposedMatrix;
    }

    public static double[][] hadamardProduct(double[][] firstMatrix, double[][] secondMatrix) {
        double[][] result = new double[firstMatrix.length][firstMatrix[0].length];
        for (int i = 0; i < firstMatrix.length; i++) {
            for (int j = 0; j < firstMatrix[0].length; j++) {
                result[i][j] = firstMatrix[i][j] * secondMatrix[i][j];
            }
        }
        return result;
    }

    public static double[][] matrixSubtraction(double[][] firstMatrix, double[][] secondMatrix) {
        double[][] result = new double[firstMatrix.length][firstMatrix[0].length];
        for (int i = 0; i < firstMatrix.length; i++) {
            for (int j = 0; j < firstMatrix[0].length; j++) {
                result[i][j] = firstMatrix[i][j] - secondMatrix[i][j];
            }
        }
        return result;
    }

    public static double[][] scalarMultiplyMatrix(double[][] matrix, double scalar) {
        double[][] result = new double[matrix.length][matrix[0].length];
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[0].length; j++) {
                result[i][j] = matrix[i][j] * scalar;
            }
        }
        return result;
    }

    public static double[] scalarMultiplyVector(double[] vector, double scalar) {
        double[] result = new double[vector.length];
        for (int i = 0; i < vector.length; i++) {
            result[i] = vector[i] * scalar;
        }
        return result;
    }

    public static double[] vectorSubtraction(double[] firstVector, double[] secondVector) {
        double[] result = new double[firstVector.length];
        for (int i = 0; i < firstVector.length; i++) {
            result[i] = firstVector[i] - secondVector[i];
        }
        return result;
    }

    public static double[] getOneDimensionalVector(double[][] x) {
        double[] result = new double[x.length];
        for (int i = 0; i < x.length; i++) {
            result[i] = x[i][0];
        }
        return result;
    }

    public static double[][] getTwoDimensionalVector(double[] x) {
        double[][] result = new double[x.length][1];
        for (int i = 0; i < x.length; i++) {
            result[i][0] = x[i];
        }
        return result;
    }

    public static double[][] sigmoidVector(double[][] x) {
        for (int i = 0; i < x.length; i++) {
            x[i][0] = sigmoid(x[i][0]);
        }
        return x;
    }

    public static double sigmoid(double x) {
        return 1 / (1 + Math.exp(-x));
    }

    public static double[][] primeSigmoidVector(double[][] x) {
        for (int i = 0; i < x.length; i++) {
            x[i][0] = primeSigmoid(x[i][0]);
        }
        return x;
    }

    public static double primeSigmoid(double x) {
        return sigmoid(x) * (1 - sigmoid(x));
    }
}
