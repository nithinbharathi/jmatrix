package operations;

import jmatrix.Matrix;

public final class SubtractUtils {
	private SubtractUtils(){
		throw new UnsupportedOperationException("utility class");
	}
	
    public static Matrix subtract(Matrix mat, Number val){
		double res[][] = new double[mat.getRowSize()][mat.getColumnSize()];
		for(int row = 0;row<mat.getRowSize();row++){
			for(int col = 0;col<mat.getColumnSize();col++){
				res[row][col] = mat.get(row,col) - val.doubleValue();
			}
		}
		return new Matrix(res);
	}
}
