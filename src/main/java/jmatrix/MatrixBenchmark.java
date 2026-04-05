package jmatrix;

import java.util.Random;
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
import org.openjdk.jmh.profile.GCProfiler;
import org.openjdk.jmh.profile.StackProfiler;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
@Warmup(iterations = 3)
@Measurement(iterations = 3)
@Fork(1)
public class MatrixBenchmark {

    @Param({"2048"})
    public int rows;

    @Param({"2048"})
    public int cols;

     @Param({"2d","123938749374d"})
     double toSubtract;

    private double[][] a2d, b2d;
    private double[] a1d, b1d;

    private Matrix mat1, mat2;

    public static void main(String args[]) throws Exception{
        Options opt = new OptionsBuilder()
                .include(MatrixBenchmark.class.getSimpleName())
                .jvmArgs("-Xmx1024m")
                //.addProfiler(StackProfiler.class)
                .addProfiler(GCProfiler.class)
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

    @Benchmark
    public void subtractParallel(){
        mat1.subtract(toSubtract);
    }

    @Benchmark
    public void subtractNormal(){
        int r = mat1.getRowSize();
        int c = mat1.getColumnSize();
        double res[][] = new double[r][c];
        for(int i = 0;i<r;i++){
            for(int j = 0;j<c;j++){
                res[i][j] = mat1.get(i, j) - toSubtract;
            }
        }

    }

    //@Benchmark
    public double divide2D() {
        double checksum = 0;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                a2d[i][j] = a2d[i][j] / b2d[i][j];
                checksum += a2d[i][j];
            }
        }
        return checksum;
    }

    //@Benchmark
    public double divide1D() {
        double checksum = 0;
        for (int i = 0; i < rows * cols; i++) {
            a1d[i] = a1d[i] / b1d[i];
            checksum += a1d[i];
        }
        return checksum;
    }
}