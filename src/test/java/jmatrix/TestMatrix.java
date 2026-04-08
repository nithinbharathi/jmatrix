package jmatrix;

public class TestMatrix {

	static Matrix mat;
	public static void main(String args[]){
			double a[][] = {{1,2,3},{1,4,5},{10,9,100}};
	 mat = new Matrix(a);
	
	mat.view();

	Matrix res1 = mat.subtract(10);
	mat.view();

	Matrix res2 = mat.subtract(1);

	res2.view();


		
	}

	 public static Matrix subtractFlat(){
        int r = mat.getRowSize();
        int c = mat.getColumnSize();
        int n = r*c;
        double res[] = new double[r*c];
        for(int i = 0;i<n;i++){
            res[i] = mat.temp[i] - 10;
        }

        return new Matrix(res, r, c);

    }

}
