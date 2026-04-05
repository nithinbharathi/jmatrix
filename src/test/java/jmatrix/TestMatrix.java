package jmatrix;

public class TestMatrix {
	public static void main(String args[]){
			double a[][] = {{1,2,3},{1,4,5},{10,9,100}};
	Matrix mat = new Matrix(a);

	Matrix res = mat.subtract(10);
	res.view();
	
	}

}
