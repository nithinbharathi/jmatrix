package jmatrix;
import java.util.Arrays;
import java.util.Random;

import jdk.incubator.vector.DoubleVector;

import jdk.incubator.vector.VectorSpecies;

public class Benchmark {
	private static int r = 10000, c = 10000;
    static final VectorSpecies<Double> SPECIES = DoubleVector.SPECIES_PREFERRED;


	public static void main(String[] args) {
		double time = 0;
		for(int i =0;i<10;i++)
			time += v3();
		
		System.out.println("Avg Time: "+(time/10));
		

		
		
		/*Double a[][] = new Double[r][c];
		
		for (int i = 0; i < r; i++) {
		    for (int j = 0; j < c; j++) {
		        a[i][j] = Double.valueOf(0.0);
		    }
		}*/
		


		
		
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
	
	private static double v3() {
		double[] a = new double[1024];
		double[] b = new double[1024];
		double[] result = new double[1024];

        // Initialize
        for (int i = 0; i < a.length; i++) {
            a[i] = i;
            b[i] = 2 * i;
        }

        int i = 0;
        int length = a.length;
        
        long startTime = System.nanoTime();

        // SIMD addition
        for (; i < SPECIES.loopBound(length); i += SPECIES.length()) {
            // Load vectors
            var va = DoubleVector.fromArray(SPECIES, a, i);
            var vb = DoubleVector.fromArray(SPECIES, b, i);
            var vc = va.add(vb);
            vc.intoArray(result, i);
        }

        // Handle tail elements (not SIMD-compatible)
        for (; i < length; i++) {
            result[i] = a[i] + b[i];
        }
        
        long endTime = System.nanoTime();

        System.out.println("Result[0] = " + result[0]);
        
        return (endTime - startTime)/1e9;
	}

}
