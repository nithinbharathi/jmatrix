package jmatrix;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import operations.Add;

/**
 * This class contains various methods for performing numerical 
 * calculations on 2 dimensional arrays. Some of the operations 
 * support parallelism for faster computation by utilizing multiple
 * threads available during runtime. The class is applicable to all
 * the Numerical types that extend the Number class in java (Integer, 
 * Long, Float, and Double).
 *  
 * @author : Nithin Bharathi 17-Jul-2023
 */

public class Matrix{

	private double mat[][];
	
	private final int colSize,rowSize;
	
	private StringBuilder matrixRepresentation = null;
		
	public static enum Arithmetic{
		MUL,
		DIV,
		SUB,
		ADD
	}
	
	/**
	 * Accepts a single dimensional array and transforms it into a matrix 
	 * based on the dimensions specified during instantiation.
	 * @param mat
	 * @param rowSize
	 * @param colSize
	 */	
	public Matrix(double mat[],int rowSize, int colSize){		
		this.colSize = colSize;
		this.rowSize = rowSize;
		this.mat = transform(mat);
	}
	
	/**
	 * Accepts a 2d array as specified during instantiation. This constructor 
	 * does not require the dimensions to be specified explicitly as the length 
	 * property of the array will be used to arrive at those values.
	 * @param mat
	 */
	public Matrix(double mat[][]){
		this.rowSize = mat.length;
		this.colSize = mat[0].length;
		this.mat  = mat;
	}
	
	public Matrix(int rowSize, int colSize) {
		this.rowSize = rowSize;
		this.colSize = colSize;
		this.mat = new double[rowSize][colSize];
	}
	
	/*
	 * Converts a linear array of numbers into a matrix with dimensions specified 
	 * by the user during the time of instantiation.
	 */
	private double[][] transform(double mat[]){
		validateDimensions(mat.length);
		double matrix[][] = new double[this.rowSize][this.colSize];
		int row = 0,col=0;
		for(int i = 0;i<mat.length;i++){
			matrix[row][col++] = mat[i];
			col%=colSize;
			row = col==0?row+1:row;
		}
		return matrix;
	}
	
	public double get(int row, int col) {
		return mat[row][col];
	}

	private void validateDimensions(int len){
		if(rowSize*colSize != len || len == 0){
			try {
				throw new Exception("Given dimensions do not match the input matrix");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	
	public int getRowSize() {
		return rowSize;
	}
	
	public int getColumnSize() {
		return colSize;
	}

	private StringBuilder buildMatrix(){
		matrixRepresentation = new StringBuilder();
		for(int i =0;i<rowSize;i++){
			for(int j =0;j<colSize;j++){
				matrixRepresentation.append(mat[i][j]);
				matrixRepresentation.append(" ");
			}
			matrixRepresentation.append("\n");
		}
		return matrixRepresentation;
	}
	
	/**
	 * Prints out the matrix representation of the current instance. Note that the running 
	 * time  of this method is always constant after the first time this method is invoked 
	 * because the representation is cached using a stringbuilder object for the subsequent 
	 * calls. However, The first invocation costs O(row*col).
	 */
	public void view(){	
		if(matrixRepresentation == null)buildMatrix();		
		System.out.println(matrixRepresentation);
		
	}
	
	public double sum(){
		double sum = Arrays.stream(mat)
					.flatMapToDouble(Arrays::stream)
					.sum();
		return sum;
	}
	
	private <E extends Number>Matrix subtract(E scalarValue, boolean reOrdered){
		double res[][] = new double[this.rowSize][this.colSize];
		for(int row = 0;row<rowSize;row++){
			for(int col = 0;col<colSize;col++){
				res[row][col] = reOrdered?performArithmetic(Arithmetic.SUB,scalarValue.doubleValue(),mat[row][col])
								: performArithmetic(Arithmetic.SUB,mat[row][col],scalarValue.doubleValue());
			}
		}
		return new Matrix(res);
	}
	
	/**
	 * Substracts the scalar value from all the numbers of the matrix
	 * and returns a new instance of the resultant matrix.
	 */
	public <E extends Number> Matrix subtract(E scalarValue){
		return subtract(scalarValue,false);
	}
	
	public Matrix add(Number val){
		return Add.add(this, val);
	}

	/**
	 * Multiplies the matrix numbers with the scalar value passed as input
	 * and returns a new instance of the resultant matrix.
	 */
	public <E extends Number>Matrix multiply(E scalarValue){
		double res[][] = new double[this.rowSize][this.colSize];
		for(int row = 0;row<rowSize;row++){
			for(int col = 0;col<colSize;col++){
				res[row][col] = mat[row][col]*scalarValue.doubleValue();
			}
		}
		return new Matrix(res);		
	}
	
	private double performArithmetic(Arithmetic operation,double operand1,double operand2){
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
	
	
	private boolean isBroadcastable(Matrix mat1,Matrix mat2){
		boolean isBroadcastable = false;
		if(mat1.colSize == mat2.colSize){
			isBroadcastable |= mat1.rowSize == 1 || mat2.rowSize == 1;
		}
		if(mat1.rowSize == mat2.rowSize) {
			isBroadcastable |= mat1.colSize == 1 || mat2.colSize == 1;
		}			
		return isBroadcastable;
	}
		
	private Matrix broadcastedArithmetic(Matrix mat1,Matrix mat2, Arithmetic operation, boolean reOrdered){				
		int rows = Math.max(mat1.rowSize,mat2.rowSize);
		int cols  = Math.max(mat1.colSize, mat2.colSize);
		
		if(mat2.rowSize != rows || mat2.colSize != cols){ // broadcasting is always done on mat2 so making sure mat2 is the larger matrix.
			return broadcastedArithmetic(mat2,mat1,operation,true); // note that the matrices are swapped.
		}
		
		double res[][] = new double[this.rowSize][this.colSize];
		
		if(mat1.colSize == mat2.colSize && mat1.rowSize == 1){  //expand across rows when the columns of two matrices are equal and mat1 is a row vector.
			for(int row = 0;row<rows;row++){
				for(int col = 0;col<cols;col++){					
					res[row][col] = performArithmetic(operation,
									reOrdered?mat2.mat[row][col]:mat1.mat[0][col], // a-b != b-a
									reOrdered?mat1.mat[0][col]:mat2.mat[row][col]);
				}
			}
		}else{
			for(int col = 0;col<cols;col++){
				for(int row = 0;row<rows;row++){
					res[row][col] = performArithmetic(operation,
									reOrdered?mat2.mat[row][col]:mat1.mat[row][0],
									reOrdered?mat1.mat[row][0]:mat2.mat[row][col]);
				}
			}		
		}
		return new Matrix(res);		
	}
	
	/**
	 * Returns the maximum value across the entire matrix.
	 */
	public double max(){	
		return Arrays.stream(mat)
						.flatMapToDouble(Arrays::stream)
						.max().getAsDouble();			
	}
	
	/**
	 * Computes the maximum value across the specified axis of a 2d matrix.
	 * A 2 dimensional matrix has 2 axes: vertical axis that runs along the 
	 * columns and a horizontal axis that runs along each row. An axis value
	 * of 1 computes the max across all the columns for each row and a value
	 * of 0 computes the max across all the rows of for each column.
	 */
	public ArrayList<Double> max(int axis){
		ArrayList<Double>maxNumbers = new ArrayList<>();
		if(axis == 1){
			for(int row = 0;row<rowSize;row++){
				double maxNumber = Double.MIN_VALUE;
				for(int col = 0;col<colSize;col++){
					maxNumber = Math.max(mat[row][col],maxNumber);
				}
				maxNumbers.add(maxNumber);
			}
		}else if(axis == 0){			
			for(int col = 0;col<colSize;col++){
				double maxNumber = Double.MIN_VALUE;
				for(int row = 0;row<rowSize;row++){
					maxNumber = Math.max(mat[row][col],maxNumber);
				}
				maxNumbers.add(maxNumber);
			}
		}else {
			try {
				throw new Exception("Invalid parameter value for axis");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return maxNumbers;
	}
	
	/**
	 * Returns a new instance of the matrix with all the numbers squared.
	 */
	public Matrix square() {
		double res[][] = new double[rowSize][colSize];
		for(int row = 0;row<rowSize;row++) {
			for(int col = 0;col<colSize;col++) {
				res[row][col] = mat[row][col]*mat[row][col];
			}
		}
		return new Matrix(res);
		
	}
	
	/**
	 * Computes the matrix multiplication between two matrices parallely. The number of the threads
	 * that operate on the matrix is determined by the number of processors that system has.
	 */
	public Matrix parallelMultiply(Matrix other){
    if (this.colSize != other.rowSize) {
        throw new IllegalArgumentException("Matrix dimensions do not match for multiplication.");
    }

    double[][] res = new double[this.rowSize][other.colSize];

    ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    for (int i = 0; i < rowSize; i++) {
        int row = i;
        executor.submit(() -> {
            for (int col = 0; col < other.colSize; col++) {
                double sum = 0;
                for (int k = 0; k < this.colSize; k++) {
                    sum += this.mat[row][k] * other.mat[k][col];
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

	
	//todo
	public void parallelSubtract(Matrix other){
		
	}
	
	//todo
	public void parallelDivide(Matrix other){
		
	}
}
