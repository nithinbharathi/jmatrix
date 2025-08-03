package operations;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import jmatrix.Matrix;

public final class AddUtils{
	private AddUtils()	{
		throw new UnsupportedOperationException("utility class");
	}

	public static Matrix add(Matrix matrix, Number val){
		double res[][] = new double[matrix.getRowSize()][matrix.getColumnSize()];
		
		for(int row = 0;row<matrix.getRowSize();row++){
			for(int col = 0;col<matrix.getColumnSize();col++){
				res[row][col] = matrix.get(row,col) + val.doubleValue();
			}
		}
		
		return new Matrix(res);
	}

	public static Matrix add(Matrix matrix1, Matrix matrix2) {
	    if (matrix1.getRowSize() != matrix2.getRowSize() || matrix1.getColumnSize() != matrix2.getColumnSize())
	        throw new IllegalArgumentException("Matrix dimensions must match.");

	    double[][] res = new double[matrix1.getRowSize()][matrix1.getColumnSize()];

	    ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

	    for (int row = 0; row < matrix1.getRowSize(); row++) {
	        final int r = row;
	        executor.submit(() -> {
	            for (int col = 0; col < matrix1.getColumnSize(); col++) {
	                res[r][col] = matrix1.get(r,col)+ matrix2.get(r,col);
	            }
	        });
	    }

	    executor.shutdown();
	    try {
	        executor.awaitTermination(1, TimeUnit.HOURS);
	    } catch (InterruptedException e) {
	        Thread.currentThread().interrupt();
	    }

	    return new Matrix(res);
	}

	public static double sum(Matrix mat){
		double sum = 0;

		for(int i = 0;i<mat.getRowSize();i++){
			for(int j =0;j<mat.getColumnSize();j++){
				sum += mat.get(i,j);
			}
		}
		
		return sum;
	}
}
