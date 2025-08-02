package jmatrix;

import java.util.Random;

public class Benchmark {
	private static int r = 100, c = 100;
	public static void main(String[] args) {
		Matrix m = new Matrix(5,5);

		m.view();

	}

	private static void testMemoryConsumption(){
		int memoryChoice[][] = {{5000,5000}};

		for(int times = 0;times<1;times++){
			for(int i = 0;i<memoryChoice.length;i++){
				int rowSize = memoryChoice[i][0];
				int colSize = memoryChoice[i][0];
				memory2(rowSize,colSize);
				printRuntimeMemoryUsage();
			}
		}
	}

	private static void memory1(int rowSize, int colSize){
		System.out.println("declaring a matrix using primitive double type....\n rowSize: "+rowSize+" colSize: "+colSize +"\n");
		double matrix[][] = new double[rowSize][colSize];
		Random random = new Random();
		for(int i=0;i<rowSize;i++){
			for(int j=0;j<colSize;j++){
				matrix[i][j] = random.nextDouble();
			}
		}

	}

	private static void memory2(int rowSize, int colSize){
		System.out.println("Declaring a matrix using wrapper Double type...\n rowSize: "+rowSize+" colSize: "+colSize +"\n");
		Double matrix[][] = new Double[rowSize][colSize];
		Random random = new Random();
		for(int i = 0;i<rowSize;i++){
			for(int j =0;j<colSize;j++){
				matrix[i][j] = random.nextDouble();
			}
		}
	}
		
	
	private static Double[][] generateRandomArray(int r, int c) {
		Double arr[][] = new Double[r][c];
		Random random = new Random();
		for(int i = 0;i<r;i++) {
			for(int j = 0;j<c;j++) {
				arr[i][j] = random.nextDouble();
			}
		}
		
		return arr;
	}
	
	private static void printRuntimeMemoryUsage() {
		
		Runtime runtime = Runtime.getRuntime();

		long totalMemory = runtime.totalMemory(); 
		long freeMemory = runtime.freeMemory();  
		long usedMemory = totalMemory - freeMemory;

		
		
		System.out.println("Used memory: " + usedMemory/1e6 + "MB");
	}
	
	private static double v1() {
		double a[][] = new double[r][c];
		
		double b[][] = new double[r][c];
		
		
		Random rand = new Random();
		
		for(int i = 0;i<r;i++) {
			for(int j = 0;j<c;j++) {
				a[i][j] = rand.nextDouble();
				b[i][j] = rand.nextDouble();
			}
		}
		
		long startTime = System.nanoTime();
		for(int i = 0;i<r;i++) {
			for(int j = 0;j<c;j++) {
				a[i][j] = a[i][j] / b[i][j];
			}
		}
		long endTime = System.nanoTime();
		
		double time = (endTime-startTime)/1e9;
		
		System.out.println("v1 time: "+time);
		
		return time;
	}
	
	private static double v2() {
		
		double a[] = new double[r*c];
		double b[] = new double[r*c];
		
		Random rand = new Random();
		
		for(int i = 0;i<r*c;i++) {
			a[i] = rand.nextDouble();
			b[i] = rand.nextDouble();
		}
		
		long startTime = System.nanoTime();
		for(int i = 0;i<r*c;i++) {
			a[i] = a[i]/b[i];
		}
		long endTime = System.nanoTime();
		double time = (endTime-startTime)/1e9;
		System.out.println("v2 time: "+time);
		
		return time;
		
	}

}