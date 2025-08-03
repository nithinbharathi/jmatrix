package operations;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import jmatrix.Matrix;

public final class MultiplyUtils {
    private MultiplyUtils(){
        throw new UnsupportedOperationException("Utility class");
    }

    public static Matrix multiply(Matrix mat1, Matrix mat2){
        if (mat1.getColumnSize() != mat2.getRowSize()) {
            throw new IllegalArgumentException("Matrix dimensions do not match for multiplication.");
        }

        double[][] res = new double[mat1.getRowSize()][mat2.getColumnSize()];

        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        for (int i = 0; i < mat1.getRowSize(); i++) {
            int row = i;
            executor.submit(() -> {
                for (int col = 0; col < mat2.getColumnSize(); col++) {
                    double sum = 0;
                    for (int k = 0; k < mat1.getColumnSize(); k++) {
                        sum += mat1.get(row,k) * mat2.get(k, col);
                    }
                    res[row][col] = sum;
                }
            });
        }

        executor.shutdown();
        try {
            executor.awaitTermination(1, TimeUnit.HOURS); 
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Matrix multiplication interrupted", e);
        }
	    	return new Matrix(res);
	}

    public static Matrix multiply(Matrix mat, Number val){
		double res[][] = new double[mat.getRowSize()][mat.getColumnSize()];
		for(int row = 0;row<mat.getRowSize();row++){
			for(int col = 0;col<mat.getColumnSize();col++){
				res[row][col] = mat.get(row,col)*val.doubleValue();
			}
		}
		return new Matrix(res);		
	}

    public static Matrix square(Matrix mat) {
		double res[][] = new double[mat.getRowSize()][mat.getColumnSize()];
		for(int row = 0;row<mat.getRowSize();row++) {
			for(int col = 0;col<mat.getColumnSize();col++) {
				res[row][col] = mat.get(row, col)*mat.get(row,col);
			}
		}
		return new Matrix(res);
	}
}
