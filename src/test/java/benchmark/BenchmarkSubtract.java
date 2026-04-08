package benchmark;

import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

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
@Measurement(iterations = 5, time =1)
@Fork(value = 1, jvmArgs = {"--add-modules", "jdk.incubator.vector", "--enable-preview"})
public class BenchmarkSubtract {

    @Param({"1024","4096", "2048" })
    public int rows;

    @Param({ "2048", "10000"})
    public int cols;

     double toSubtract;
     
     private final VectorSpecies<Double> SPECIES = DoubleVector.SPECIES_PREFERRED;


    private double[][] a2d, b2d;
    private double[] a1d, b1d;

    private Matrix mat1, mat2;

    public static void main(String args[]) throws Exception{
       Options opt = new OptionsBuilder()
                .include(BenchmarkSubtract.class.getSimpleName())
                .jvmArgs("--add-modules", "jdk.incubator.vector", "--enable-preview")
                .build();

        new Runner(opt).run();

    }

    @Setup(Level.Iteration)
    public void setup() {
        Random rand = new Random();
        a2d = new double[rows][cols];
        b2d = new double[rows][cols];
        a1d = new double[rows * cols];
        b1d = new double[rows * cols];

        toSubtract = rand.nextDouble();

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                double valA = rand.nextDouble() + 1e-9;
                double valB = rand.nextDouble() + 1e-9;
                a2d[i][j] = valA;
                b2d[i][j] = valB;
                a1d[i * cols + j] = valA;
                b1d[i * cols + j] = valB;
            }
        }

        mat1 = new Matrix(a2d);
        mat2 = new Matrix(b2d);
    }
    
    // Wanted to test the performance of vector API. No big difference. Should try testing on a bigger machine.
   @Benchmark
    public void simd() {
    	int i = 0;
    	 int r = mat1.getRowSize();
         int c = mat1.getColumnSize();
        int n = r*c;
        DoubleVector subVector = DoubleVector.broadcast(SPECIES, toSubtract);

        // Process in vector chunks
        for (; i <= n - SPECIES.length(); i += SPECIES.length()) {
        	DoubleVector vec = DoubleVector.fromArray(SPECIES, mat1.temp, i);
            vec = vec.sub(subVector);
            vec.intoArray(mat1.temp, i);
        }

        // Process remaining elements
        for (; i < n; i++) {
            mat1.temp[i] -= toSubtract;
        }
    }
    

    @Benchmark
    public Matrix subtractParallel1(){
        return mat1.subtract(toSubtract);
    }

    @Benchmark
    public Matrix subtractParallel2(){
        int n = mat1.temp.length;
		
        IntStream.range(0, n).parallel().forEach(i -> mat1.temp[i] -= toSubtract);

		return mat1;
    }


    @Benchmark
    public Matrix subtractFlat(){
        int r = mat1.getRowSize();
        int c = mat1.getColumnSize();
        int n = r*c;
       
        for(int i = 0;i<n;i++){
            mat1.temp[i] -= toSubtract;
        }

        return mat1;

    }

}