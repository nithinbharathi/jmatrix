package util;

import jmatrix.ArithmeticType;
import jmatrix.Matrix;

public final class utility {
    public static double performArithmetic(ArithmeticType operation,double operand1,double operand2){
		double result = 0;		
		switch(operation){
			case MUL:
				result = operand1 * operand2;
				break;
			case DIV:
				result = operand1/operand2;
				break;
			case SUB:
				result = operand1 - operand2;
				break;
			case ADD:
				result = operand1+operand2;
				break;
		}				
		return result;
	}

    public static boolean isBroadcastable(Matrix mat1,Matrix mat2){
		boolean isBroadcastable = false;
		if(mat1.getColumnSize() == mat2.getColumnSize()){
			isBroadcastable |= mat1.getRowSize() == 1 || mat2.getRowSize() == 1;
		}
		if(mat1.getRowSize() == mat2.getRowSize()) {
			isBroadcastable |= mat1.getColumnSize() == 1 || mat2.getColumnSize() == 1;
		}			
		return isBroadcastable;
	}
		
	public static Matrix broadcastedArithmetic(Matrix mat1,Matrix mat2, ArithmeticType operation, boolean reOrdered){				
		int rows = Math.max(mat1.getRowSize(),mat2.getRowSize());
		int cols  = Math.max(mat1.getColumnSize(), mat2.getColumnSize());
		
		if(mat2.getRowSize() != rows || mat2.getColumnSize() != cols){ // broadcasting is always done on mat2 so making sure mat2 is the larger matrix.
			return broadcastedArithmetic(mat2,mat1,operation,true); // note that the matrices are swapped.
		}
		
		double res[][] = new double[mat1.getRowSize()][mat1.getColumnSize()];
		
		if(mat1.getColumnSize() == mat2.getColumnSize() && mat1.getRowSize() == 1){  //expand across rows when the columns of two matrices are equal and mat1 is a row vector.
			for(int row = 0;row<rows;row++){
				for(int col = 0;col<cols;col++){					
					res[row][col] = performArithmetic(operation,
									reOrdered?mat2.get(row,col):mat1.get(0,col), // a-b != b-a
									reOrdered?mat1.get(0,col):mat2.get(row,col));
				}
			}
		}else{
			for(int col = 0;col<cols;col++){
				for(int row = 0;row<rows;row++){
					res[row][col] = performArithmetic(operation,
									reOrdered?mat2.get(row,col):mat1.get(row,0),
									reOrdered?mat1.get(row,0):mat2.get(row,col));
				}
			}		
		}
		return new Matrix(res);		
	}
}
