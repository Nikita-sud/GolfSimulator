package com.example.golfgame.utils;

/**
 * Utility class for various matrix operations commonly used in neural network computations.
 */
public class MatrixUtils {

    /**
     * Adds a bias vector to each column of a matrix.
     *
     * @param matrix The input matrix.
     * @param bias   The bias vector.
     * @return A new matrix with the bias added to each column.
     */
    public static double[][] addBias(double[][] matrix, double[] bias) {
        double[][] result = new double[matrix.length][matrix[0].length];
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[0].length; j++) {
                result[i][j] = matrix[i][j] + bias[j];
            }
        }
        return result;
    }

    /**
     * Multiplies two matrices.
     *
     * @param firstMatrix  The first matrix.
     * @param secondMatrix The second matrix.
     * @return The product of the two matrices.
     * @throws IllegalArgumentException If the matrices cannot be multiplied due to incompatible dimensions.
     */
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

    /**
     * Transposes a matrix.
     *
     * @param matrix The input matrix.
     * @return The transposed matrix.
     */
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

    /**
     * Computes the Hadamard product (element-wise multiplication) of two matrices.
     *
     * @param firstMatrix  The first matrix.
     * @param secondMatrix The second matrix.
     * @return The Hadamard product of the two matrices.
     */
    public static double[][] hadamardProduct(double[][] firstMatrix, double[][] secondMatrix) {
        double[][] result = new double[firstMatrix.length][firstMatrix[0].length];
        for (int i = 0; i < firstMatrix.length; i++) {
            for (int j = 0; j < firstMatrix[0].length; j++) {
                result[i][j] = firstMatrix[i][j] * secondMatrix[i][j];
            }
        }
        return result;
    }

    /**
     * Subtracts the second matrix from the first matrix.
     *
     * @param firstMatrix  The first matrix.
     * @param secondMatrix The second matrix.
     * @return The result of the matrix subtraction.
     */
    public static double[][] matrixSubtraction(double[][] firstMatrix, double[][] secondMatrix) {
        double[][] result = new double[firstMatrix.length][firstMatrix[0].length];
        for (int i = 0; i < firstMatrix.length; i++) {
            for (int j = 0; j < firstMatrix[0].length; j++) {
                result[i][j] = firstMatrix[i][j] - secondMatrix[i][j];
            }
        }
        return result;
    }

    /**
     * Multiplies each element of a matrix by a scalar.
     *
     * @param matrix The input matrix.
     * @param scalar The scalar value.
     * @return A new matrix with each element multiplied by the scalar.
     */
    public static double[][] scalarMultiplyMatrix(double[][] matrix, double scalar) {
        double[][] result = new double[matrix.length][matrix[0].length];
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[0].length; j++) {
                result[i][j] = matrix[i][j] * scalar;
            }
        }
        return result;
    }

    /**
     * Multiplies each element of a vector by a scalar.
     *
     * @param vector The input vector.
     * @param scalar The scalar value.
     * @return A new vector with each element multiplied by the scalar.
     */
    public static double[] scalarMultiplyVector(double[] vector, double scalar) {
        double[] result = new double[vector.length];
        for (int i = 0; i < vector.length; i++) {
            result[i] = vector[i] * scalar;
        }
        return result;
    }

    /**
     * Subtracts the second vector from the first vector.
     *
     * @param firstVector  The first vector.
     * @param secondVector The second vector.
     * @return The result of the vector subtraction.
     */
    public static double[] vectorSubtraction(double[] firstVector, double[] secondVector) {
        double[] result = new double[firstVector.length];
        for (int i = 0; i < firstVector.length; i++) {
            result[i] = firstVector[i] - secondVector[i];
        }
        return result;
    }

    /**
     * Converts a two-dimensional matrix to a one-dimensional vector.
     *
     * @param x The input two-dimensional matrix.
     * @return The resulting one-dimensional vector.
     */
    public static double[] getOneDimensionalVector(double[][] x) {
        double[] result = new double[x.length];
        for (int i = 0; i < x.length; i++) {
            result[i] = x[i][0];
        }
        return result;
    }

    /**
     * Converts a one-dimensional vector to a two-dimensional matrix.
     *
     * @param x The input one-dimensional vector.
     * @return The resulting two-dimensional matrix.
     */
    public static double[][] getTwoDimensionalVector(double[] x) {
        double[][] result = new double[x.length][1];
        for (int i = 0; i < x.length; i++) {
            result[i][0] = x[i];
        }
        return result;
    }

    /**
     * Applies the sigmoid function to each element of a matrix.
     *
     * @param x The input matrix.
     * @return A new matrix with the sigmoid function applied to each element.
     */
    public static double[][] sigmoidVector(double[][] x) {
        for (int i = 0; i < x.length; i++) {
            x[i][0] = sigmoid(x[i][0]);
        }
        return x;
    }

    /**
     * Applies the sigmoid function to a single value.
     *
     * @param x The input value.
     * @return The result of applying the sigmoid function to the input value.
     */
    public static double sigmoid(double x) {
        return 1 / (1 + Math.exp(-x));
    }

    /**
     * Applies the derivative of the sigmoid function to each element of a matrix.
     *
     * @param x The input matrix.
     * @return A new matrix with the derivative of the sigmoid function applied to each element.
     */
    public static double[][] primeSigmoidVector(double[][] x) {
        for (int i = 0; i < x.length; i++) {
            x[i][0] = primeSigmoid(x[i][0]);
        }
        return x;
    }

    /**
     * Computes the derivative of the sigmoid function for a single value.
     *
     * @param x The input value.
     * @return The result of applying the derivative of the sigmoid function to the input value.
     */
    public static double primeSigmoid(double x) {
        return sigmoid(x) * (1 - sigmoid(x));
    }

    /**
     * Computes the delta for cross-entropy loss.
     *
     * @param outputActivations The output activations from the neural network.
     * @param y                 The true labels.
     * @return The computed delta for the cross-entropy loss.
     */
    public static double[][] crossEntropyDelta(double[][] outputActivations, double[][] y) {
        double[][] delta = new double[outputActivations.length][outputActivations[0].length];
        for (int i = 0; i < outputActivations.length; i++) {
            for (int j = 0; j < outputActivations[i].length; j++) {
                delta[i][j] = outputActivations[i][j] - y[i][j];
            }
        }
        return delta;
    }

    public static double scaleToRange(double value, double min, double max) {
        return min + (max - min) * sigmoid(value);
    }
}
