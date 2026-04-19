package benchmark;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import jdk.incubator.vector.DoubleVector;
import jdk.incubator.vector.VectorSpecies;
import jmatrix.Matrix;


@BenchmarkMode({Mode.AverageTime})
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 3, time =1)
@Fork(value = 1, jvmArgs = {"--add-modules", "jdk.incubator.vector", "--enable-preview"})
public class BenchmarkMultiply {

    @Param({"2048"})
    public int rows;

    @Param({ "2048"})
    public int cols;

     double toSubtract;
    

    private double[][] a2d, b2d;
    private double[] a1d, b1d;

    private Matrix mat2d1, mat2d2, mat1d1, mat1d2;
    private static final VectorSpecies<Double> SPECIES = DoubleVector.SPECIES_PREFERRED;

    public static void main(String args[]) throws Exception{
       Options opt = new OptionsBuilder()
                .include(BenchmarkMultiply.class.getSimpleName())
                .jvmArgs("--add-modules", "jdk.incubator.vector", "--enable-preview")
                .build();

        new Runner(opt).run();

    }

    @Setup(Level.Iteration)
    public void setup() {
        Random rand = new Random();
        a2d = new double[rows][cols];
        b2d = new double[cols][rows];
        a1d = new double[rows * cols];
        b1d = new double[rows * cols];

        toSubtract = rand.nextDouble();

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                double valA = rand.nextDouble() + 1e-9;
                a2d[i][j] = valA;
                //a1d[i * cols + j] = valA;
            }
        }

        for(int i = 0;i<cols;i++){
            for(int j = 0;j<rows;j++){
                 double valB = rand.nextDouble() + 1e-9;
                  b2d[i][j] = valB;
                  //b1d[i * cols + j] = valB;
            }
        }

        mat2d1 = new Matrix(a2d);
        mat2d2 = new Matrix(b2d);

        mat1d1 = new Matrix(a1d, rows, cols);
        mat1d2 = new Matrix(b1d, rows, cols);
    }

    @Benchmark
    public Matrix multiplyNaive(){
        Matrix mat1 = mat2d1;
        Matrix mat2 = mat2d2;
        int n = mat2d1.getRowSize(), m = mat2.getColumnSize();
        double res[][] = new double[n][m];
        for(int i = 0;i<n;i++){
        	for(int k = 0;k<mat2.getRowSize();k++){
            	for(int j = 0;j<m;j++){
                    res[i][j] += mat1.get(i, k)*mat2.get(k,i);
                }
            }
        }

        return new Matrix(res);
    }
    
    @Benchmark
    public Matrix multiplyParallel() {
    	  Matrix mat1 = mat2d1;
          Matrix mat2 = mat2d2;
          
          return mat1.multiply(mat2);
    }
    
    @Benchmark
    public Matrix multiplyParallelSIMD() {

        List<Future<?>> futures = new ArrayList<>();
        
        int n = mat2d1.getRowSize(), m = mat2d2.getColumnSize();
        double res[][] = new double[n][m];

        double a[][] = a2d;
        double b[][] = b2d;
        for (int i = 0; i < n; i++) {
            final int row = i;
            futures.add(ForkJoinPool.commonPool().submit(() -> {
                for (int k = 0; k < mat2d1.getColumnSize(); k++) {
                    DoubleVector va = DoubleVector.broadcast(SPECIES, a[row][k]);
                    int j = 0;
                    for (; j < SPECIES.loopBound(m); j += SPECIES.length()) {
                        DoubleVector vb = DoubleVector.fromArray(SPECIES, b[k], j);
                        DoubleVector vr = DoubleVector.fromArray(SPECIES, res[row], j);
                        va.fma(vb, vr).intoArray(res[row], j);
                    }
                    for (; j < m; j++) {
                        res[row][j] += a[row][k] * b[k][j];
                    }
                }
            }));
        }

        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (Exception e) {
                throw new RuntimeException("Matrix multiplication interrupted", e);
            }
        }

        return new Matrix(res);
    }
}