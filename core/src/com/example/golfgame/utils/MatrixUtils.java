package com.example.golfgame.utils;

import java.util.Arrays;

/**
 * Utility class for various matrix operations commonly used in neural network computations.
 */
public class MatrixUtils {

    /**
     * Adds a bias vector to each element of a vector.
     *
     * @param vector The input vector.
     * @param bias   The bias vector.
     * @return A new vector with the bias added to each element.
     * @throws IllegalArgumentException if the vector and bias dimensions do not match.
     */
    public static double[] addBias(double[] vector, double[] bias) {
        if (vector.length != bias.length) {
            throw new IllegalArgumentException("Vector and bias dimensions do not match");
        }
        double[] result = new double[vector.length];
        for (int i = 0; i < vector.length; i++) {
            result[i] = vector[i] + bias[i];
        }
        return result;
    }

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
                result[i][j] = matrix[i][j] + bias[i];
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
            throw new IllegalArgumentException("Matrix multiplication dimensions do not match: " +
                                               firstMatrixRows + "," + firstMatrixCols + " and " +
                                               secondMatrixRows + "," + secondMatrixCols + " not compatible");
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
     * Applies the softplus function to a single value.
     *
     * @param x The input value.
     * @return The result of applying the softplus function to the input value.
     */
    public double softplus(double x) {
        return Math.log(1 + Math.exp(x));
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
     * Applies the sigmoid function to a single value.
     *
     * @param x The input value.
     * @return The result of applying the sigmoid function to the input value.
     */
    public static double sigmoid(double z) {
        if (z < -45.0) return 0.0;
        if (z > 45.0) return 1.0;
        return 1.0 / (1.0 + Math.exp(-z));
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
     * Flattens a two-dimensional array into a one-dimensional array.
     *
     * @param array The input two-dimensional array.
     * @return The flattened one-dimensional array.
     */
    public static double[] flattenArray(double[][] array) {
        int rows = array.length;
        int cols = array[0].length;
        double[] flatArray = new double[rows * cols];
        int index = 0;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                flatArray[index++] = array[i][j];
            }
        }
        return flatArray;
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

    /**
     * Scales a value to a specified range using the sigmoid function.
     *
     * @param value The input value.
     * @param min   The minimum value of the range.
     * @param max   The maximum value of the range.
     * @return The scaled value.
     */
    public static double scaleToRange(double value, double min, double max) {
        return min + (max - min) * sigmoid(value);
    }

    /**
     * Sums the columns of a matrix.
     *
     * @param matrix The input matrix.
     * @return A vector containing the sum of each column.
     */
    public static double[] sumColumns(double[][] matrix) {
        int rows = matrix.length;
        int cols = matrix[0].length;
        double[] columnSums = new double[cols];

        for (int j = 0; j < cols; j++) {
            for (int i = 0; i < rows; i++) {
                columnSums[j] += matrix[i][j];
            }
        }

        return columnSums;
    }

    /**
     * Multiplies a matrix by a vector.
     *
     * @param matrix The input matrix.
     * @param vector The input vector.
     * @return The resulting vector.
     * @throws IllegalArgumentException if the matrix and vector dimensions do not match.
     */
    public static double[] multiplyMatrixAndVector(double[][] matrix, double[] vector) {
        int rows = matrix.length;
        int cols = matrix[0].length;
        if (vector.length != cols) {
            throw new IllegalArgumentException("Matrix and vector dimensions do not match");
        }
        double[] result = new double[rows];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                result[i] += matrix[i][j] * vector[j];
            }
        }
        return result;
    }

    /**
     * Applies the sigmoid function to each element of a vector.
     *
     * @param vector The input vector.
     * @return A new vector with the sigmoid function applied to each element.
     */
    public static double[] sigmoidVector(double[] vector) {
        double[] result = new double[vector.length];
        for (int i = 0; i < vector.length; i++) {
            result[i] = sigmoid(vector[i]);
        }
        return result;
    }

    /**
     * Converts a two-dimensional matrix to a one-dimensional vector.
     *
     * @param x The input two-dimensional matrix.
     * @return The resulting one-dimensional vector.
     */
    public static double[] getOneDimentionalVector(double[][] x) {
        double[] result = new double[x.length];
        for (int i = 0; i < x.length; i++) {
            result[i] = x[i][0];
        }
        return result;
    }

    /**
     * Adds two matrices element-wise.
     *
     * @param firstMatrix  The first matrix.
     * @param secondMatrix The second matrix.
     * @return The result of the element-wise addition.
     */
    public static double[][] vectorAddition(double[][] firstMatrix, double[][] secondMatrix) {
        if (firstMatrix.length != secondMatrix.length || firstMatrix[0].length != secondMatrix[0].length) {
            System.out.println("The matrices' sizes don't match");
            return null;
        }

        double[][] result = new double[firstMatrix.length][firstMatrix[0].length];
        for (int i = 0; i < firstMatrix.length; i++) {
            for (int j = 0; j < firstMatrix[0].length; j++) {
                result[i][j] = firstMatrix[i][j] + secondMatrix[i][j];
            }
        }
        return result;
    }

    /**
     * Finds the index of the maximum value in a two-dimensional array.
     *
     * @param array The input array.
     * @return The index of the maximum value.
     */
    public static int argMax(double[][] array) {
        int maxIndex = 0;
        double max = array[0][0];
        for (int i = 1; i < array.length; i++) {
            if (array[i][0] > max) {
                max = array[i][0];
                maxIndex = i;
            }
        }
        return maxIndex;
    }

    /**
     * Finds the index of the maximum value in a one-dimensional array.
     *
     * @param array The input array.
     * @return The index of the maximum value.
     */
    public static int argMax(double[] array) {
        int maxIndex = 0;
        double max = array[0];
        for (int i = 1; i < array.length; i++) {
            if (array[i] > max) {
                max = array[i];
                maxIndex = i;
            }
        }
        return maxIndex;
    }

    /**
     * Applies the softmax function to each element of a matrix.
     *
     * @param z The input matrix.
     * @return A new matrix with the softmax function applied to each element.
     */
    public static double[][] softmaxVector(double[][] z) {
        double max = Double.NEGATIVE_INFINITY;
        for (double[] value : z) {
            if (value[0] > max) {
                max = value[0];
            }
        }

        double sum = 0.0;
        double[][] softmax = new double[z.length][1];
        for (int i = 0; i < z.length; i++) {
            softmax[i][0] = Math.exp(z[i][0] - max);
            sum += softmax[i][0];
        }

        for (int i = 0; i < softmax.length; i++) {
            softmax[i][0] /= sum;
        }
        return softmax;
    }

    /**
     * Computes the Jacobian matrix of the softmax function.
     *
     * @param softmax The softmax vector.
     * @return The Jacobian matrix of the softmax function.
     */
    public static double[][] primeSoftmaxVector(double[][] softmax) {
        int n = softmax.length;
        double[][] jacobianMatrix = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i == j) {
                    jacobianMatrix[i][j] = softmax[i][0] * (1 - softmax[i][0]);
                } else {
                    jacobianMatrix[i][j] = -softmax[i][0] * softmax[j][0];
                }
            }
        }
        return jacobianMatrix;
    }
    
    /**
     * Subtracts one matrix from another.
     *
     * @param a The first matrix.
     * @param b The second matrix.
     * @return The result of the matrix subtraction.
     */
    public static double[][] subtract(double[][] a, double[][] b) {
        int rows = a.length;
        int cols = a[0].length;
        double[][] result = new double[rows][cols];
        
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                result[i][j] = a[i][j] - b[i][j];
            }
        }
        
        return result;
    }

    /**
     * Subtracts a vector from each row of a matrix.
     *
     * @param a The input matrix.
     * @param b The vector to subtract.
     * @return The resulting matrix.
     */
    public static double[][] subtract(double[][] a, double[] b) {
        int rows = a.length;
        int cols = a[0].length;
        double[][] result = new double[rows][cols];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                result[i][j] = a[i][j] - b[i];
            }
        }

        return result;
    }

    public static double sigmoidPrime(double z) {
        double sig = sigmoid(z);
        return sig * (1 - sig);
    }

    public static double[][] sigmoidVector(double[][] z) {
        double[][] result = new double[z.length][z[0].length];
        for (int i = 0; i < z.length; i++) {
            for (int j = 0; j < z[0].length; j++) {
                result[i][j] = sigmoid(z[i][j]);
            }
        }
        return result;
    }
    /**
     * Applies the derivative of the sigmoid function to each element of a matrix.
     *
     * @param z The input matrix.
     * @return A new matrix with the derivative of the sigmoid function applied to each element.
     */
    public static double[][] sigmoidPrimeVector(double[][] z) {
        double[][] result = new double[z.length][z[0].length];
        for (int i = 0; i < z.length; i++) {
            for (int j = 0; j < z[0].length; j++) {
                result[i][j] = sigmoidPrime(z[i][j]);
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
    public static double[][] scalarMultiply(double[][] matrix, double scalar) {
        double[][] result = new double[matrix.length][matrix[0].length];
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                result[i][j] = matrix[i][j] * scalar;
            }
        }
        return result;
    }
    public static double relu(double z) {
        return Math.max(0, z);
    }

    public static double reluPrime(double z) {
        return z > 0 ? 1.0 : 0.0;
    }

    public static double[][] reluVector(double[][] z) {
        double[][] result = new double[z.length][z[0].length];
        for (int i = 0; i < z.length; i++) {
            for (int j = 0; j < z[0].length; j++) {
                result[i][j] = relu(z[i][j]);
            }
        }
        return result;
    }

    public static double[][] reluPrimeVector(double[][] z) {
        double[][] result = new double[z.length][z[0].length];
        for (int i = 0; i < z.length; i++) {
            for (int j = 0; j < z[0].length; j++) {
                result[i][j] = reluPrime(z[i][j]);
            }
        }
        return result;
    }

    // --- Tanh ---
    public static double tanh(double z) {
        return Math.tanh(z);
    }

    public static double tanhPrime(double z) {
        double tanh_z = Math.tanh(z);
        return 1.0 - tanh_z * tanh_z;
    }

    public static double[][] tanhVector(double[][] z) {
        double[][] result = new double[z.length][z[0].length];
        for (int i = 0; i < z.length; i++) {
            for (int j = 0; j < z[0].length; j++) {
                result[i][j] = tanh(z[i][j]);
            }
        }
        return result;
    }

     public static double[][] tanhPrimeVector(double[][] z) {
        double[][] result = new double[z.length][z[0].length];
        for (int i = 0; i < z.length; i++) {
            for (int j = 0; j < z[0].length; j++) {
                result[i][j] = tanhPrime(z[i][j]);
            }
        }
        return result;
    }

    // --- Linear (для единообразия, хотя он ничего не делает) ---
    public static double linear(double z) {
        return z;
    }

     public static double linearPrime(double z) {
        return 1.0;
     }

     public static double[][] linearVector(double[][] z) {
        // Возвращает копию, чтобы избежать изменения оригинала при необходимости
        return MatrixUtils.copyMatrix(z);
     }

     public static double[][] linearPrimeVector(double[][] z) {
        // Возвращает матрицу единиц той же размерности
        double[][] result = new double[z.length][z[0].length];
        for(int i=0; i<z.length; i++) {
            Arrays.fill(result[i], 1.0);
        }
        return result;
     }

     // Добавим вспомогательный метод копирования матрицы
     public static double[][] copyMatrix(double[][] original) {
         if (original == null) {
             return null;
         }
         double[][] copy = new double[original.length][];
         for (int i = 0; i < original.length; i++) {
             copy[i] = Arrays.copyOf(original[i], original[i].length);
         }
         return copy;
     }
}
