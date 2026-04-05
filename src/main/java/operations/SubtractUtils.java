package operations;

import java.util.concurrent.ForkJoinPool;

import jmatrix.Matrix;

public final class SubtractUtils {
	private SubtractUtils(){
		throw new UnsupportedOperationException("utility class");
	}
	
    public static Matrix subtract(Matrix mat, double val){
		double res[][] = new double[mat.getRowSize()][mat.getColumnSize()];

		for(int row = 0;row<mat.getRowSize();row++){
			int r = row;
			ForkJoinPool.commonPool().submit(()->{
				for(int col = 0;col<mat.getColumnSize();col++){
					int c = col;
					res[r][c] = mat.get(r,c) - val;	
				}
			});
		}

		return new Matrix(res);
	}
}
