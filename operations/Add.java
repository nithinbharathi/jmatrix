package operations;

import jmatrix.Matrix;
import jmatrix.Matrix.Arithmetic;

public class Add<T> {	
	/**
	 * Adds the given scalar to all the elements of the matrix 
	 * and returns a new instance of the resultant matrix.
	 */
	public static <T extends Number>Matrix<Double> add(Matrix<T> matrix, Number scalarValue){
		Double res[][] = new Double[matrix.getRowSize()][matrix.getColumnSize()];
		
		for(int row = 0;row<matrix.getRowSize();row++){
			for(int col = 0;col<matrix.getColumnSize();col++){
				res[row][col] = matrix.get(row,col).doubleValue() + scalarValue.doubleValue();
			}
		}
		
		return new Matrix<>(res);
	}
	
	/**
	 * Performs matrix addition. If the matrices involved in the operation 
	 * differ in their dimensions but follow the broadcasting rules, the operation
	 * is performed by "broadcasting" the smaller matrix across the larger one. This
	 * is done without creating additional copies of the matrix.
	 *
	public Matrix<Double> add(Matrix other){			
		if(isBroadcastable(this,other)){
			return broadcastedArithmetic(this,other,Arithmetic.ADD,false);			
		}	
		if(standardArithmeticApplicable(this, other,Arithmetic.ADD)){
			return performStandardArithmetic(other,Arithmetic.ADD);
		}
		if(other.rowSize == 1 && other.colSize == 1){
			return add(other.mat[0][0]);
		}
		if(this.rowSize == 1 && this.colSize == 1){
			return other.add(this.mat[0][0]);
		}
		throwInvalidDimensionException();
		
		return other;
	}*/

}
