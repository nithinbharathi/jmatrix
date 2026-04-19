package operations;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;

import jmatrix.Matrix;

public final class MultiplyUtils {
    private MultiplyUtils(){
        throw new UnsupportedOperationException("Utility class");
    }
    


    public static Matrix multiply(Matrix mat1, Matrix mat2){
        if (mat1.getColumnSize() != mat2.getRowSize()) {
            throw new IllegalArgumentException("Matrix dimensions do not match for multiplication.");
        }
        
        int n = mat1.getRowSize();
        int m = mat2.getColumnSize();
        
        double[][] res = new double[n][m];
        List<Future<?>>futures = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            int row = i;
            futures.add(
            ForkJoinPool.commonPool().submit(() -> {
            	for (int k = 0; k < mat1.getColumnSize(); k++) {
                    double sum = 0;
                    for (int col = 0; col < m; col++) {
                        res[row][col] += mat1.get(row,k) * mat2.get(k, col);
                    }
                }
            }));
        }

        for(Future<?>future:futures)
            try{
                future.get();
            } catch(Exception e){
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
