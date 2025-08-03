package jmatrix;

import operations.AddUtils;
import operations.AggregateUtils;
import operations.MultiplyUtils;
import operations.SubtractUtils;

/*
 * This class contains various methods for performing numerical 
 * calculations on 2 dimensional arrays. Some of the operations 
 * support parallelism.
 *  
 * @author : Nithin Bharathi 17-Jul-2023
 */

public class Matrix{
	private double mat[][];
	
	private final int colSize, rowSize;
	
	private StringBuilder matrixRepresentation = null;
	
	/*
	 * Accepts a single dimensional array and transforms it into a matrix 
	 * based on the dimensions specified.
	 * @param mat 1d double array
	 * @param rowSize the number of rows in the matrix
	 * @param colSize the number of columns in the matrix
	 */	
	public Matrix(double mat[],int rowSize, int colSize){		
		this.colSize = colSize;
		this.rowSize = rowSize;
		this.mat = transform(mat);
	}
	
	/*
	 * Creates a matrix using the 2d array specified.
	 * @param mat 2d double array
	 */
	public Matrix(double mat[][]){
		this.rowSize = mat.length;
		this.colSize = mat[0].length;
		this.mat  = mat;
	}
	
	/*
	 * Creates matrix with the given dimensions, filled with zeros.
	 * @param rowSize the number of rows in the matrix
	 * @param colSize the number of columns in the matrix
	 */
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

	public void set(int row, int col, double val){
		mat[row][col] = val;
	}

	private void validateDimensions(int len){
		if(rowSize*colSize != len || len == 0)
			throw new IllegalArgumentException("Given dimensions do not match the input matrix");
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
	
	/*
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
		return AddUtils.sum(this);
	}
	

	public Matrix subtract(Number val){
		return SubtractUtils.subtract(this, val);
	}
	
	public Matrix add(Number val){
		return AddUtils.add(this, val);
	}


	public Matrix multiply(Number val){
		return MultiplyUtils.multiply(this, val);	
	}
	
	public double max(){
		return AggregateUtils.max(this);
	}
	
	public Matrix square() {
		return MultiplyUtils.square(this);
	}
	
	public Matrix multiply(Matrix other){
		return MultiplyUtils.multiply(this,other);
	}
}
