package benchmark;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.RecursiveAction;
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
import org.jblas.DoubleMatrix;
import org.jblas.SimpleBlas;
import jdk.incubator.vector.DoubleVector;
import jdk.incubator.vector.VectorSpecies;
import jmatrix.Matrix;


@BenchmarkMode({Mode.AverageTime})
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
@Warmup(iterations = 5, time = 3)
@Measurement(iterations = 3, time =3)
@Fork(value = 1, jvmArgs = {"--add-modules", "jdk.incubator.vector", "--enable-preview"})
public class BenchmarkMultiply {

    @Param({"2048"})
    public int rows;

    @Param({ "2048"})
    public int cols;

     double toSubtract;
    

    private double[][] a2d, b2d;
    private double[] a1d, b1d;
    
    DoubleMatrix a,b;

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
        b1d = new double[cols * rows];

        toSubtract = rand.nextDouble();

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                double valA = rand.nextDouble() + 1e-9;
                a2d[i][j] = valA;
                a1d[i * cols + j] = valA;
            }
        }

        for(int i = 0;i<cols;i++){
            for(int j = 0;j<rows;j++){
                 double valB = rand.nextDouble() + 1e-9;
                  b2d[i][j] = valB;
                  b1d[i * rows + j] = valB;
            }
        }

        mat2d1 = new Matrix(a2d);
        mat2d2 = new Matrix(b2d);

        mat1d1 = new Matrix(a1d, rows, cols);
        mat1d2 = new Matrix(b1d, cols, rows);
        
        
		a = new DoubleMatrix(rows, cols, a1d);
		b = new DoubleMatrix(cols, rows, b1d);
    }

    //@Benchmark
    public Matrix multiplyNaive(){
        Matrix mat1 = mat2d1;
        Matrix mat2 = mat2d2;
        int n = mat2d1.getRowSize(), m = mat2.getColumnSize();
        double res[][] = new double[n][m];
        for(int i = 0;i<n;i++){
        	for(int j = 0;j<m;j++){
            	for(int k = 0;k<mat2.getRowSize();k++){
                    res[i][j] += mat1.get(i, k)*mat2.get(k,i);
                }
            }
        }

        return new Matrix(res);
    }
    
    //@Benchmark
    public Matrix multiplyParallel() {
    	  Matrix mat1 = mat2d1;
          Matrix mat2 = mat2d2;
          
          return mat1.multiply(mat2);
    }
    
    //@Benchmark
    public Matrix multiplyParallelSIMD() {

        List<Future<?>> futures = new ArrayList<>();
        
        int n = mat2d1.getRowSize(), m = mat2d2.getColumnSize();
        double res[][] = new double[n][m];

        double a[][] = a2d;
        double b[][] = b2d;
        futures.add(ForkJoinPool.commonPool().submit(() -> {
        	for (int i = 0; i < n; i++) {
            final int row = i;
            
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
            };
        }));

        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (Exception e) {
                throw new RuntimeException("Matrix multiplication interrupted", e);
            }
        }

        return new Matrix(res);
    }
    
    //@Benchmark
    public Matrix multiplyFlatParallelSIMD() {
        int n = mat2d1.getRowSize(), m = mat2d2.getColumnSize();

        double[] res = new double[n * m];
        double a[] = a1d;
        double b[] = b1d;
        int inner = mat2d1.getColumnSize();
        List<Future<?>> futures = new ArrayList<>();
        futures.add(ForkJoinPool.commonPool().submit(() -> {
        for (int i = 0; i < n; i++) {
        	final int row = i;
        	
            for (int k = 0; k < inner; k++) {
                DoubleVector va = DoubleVector.broadcast(SPECIES, a[row * inner + k]);
                int j = 0;
                for (; j < SPECIES.loopBound(m); j += SPECIES.length()) {
                    DoubleVector vb = DoubleVector.fromArray(SPECIES, b, k * m + j);
                    DoubleVector vr = DoubleVector.fromArray(SPECIES, res, row * m + j);
                    va.fma(vb, vr).intoArray(res, row * m + j);
                }
                // tail
                for (; j < m; j++) {
                    res[row * m + j] += a[row * inner + k] * b[k * m + j];
                }
            }
        }}));
        
        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (Exception e) {
                throw new RuntimeException("Matrix multiplication interrupted", e);
            }
        }

        return new Matrix(res, rows, cols);
    }
    
    //@Benchmark
    public Matrix multiplyTiled() {
        int rows  = mat2d1.getRowSize();
        int cols  = mat2d2.getColumnSize();
        int inner = mat2d1.getColumnSize();

        double[] a   = a1d;
        double[] b   = b1d;
        double[] res = new double[rows * cols];

        int TILE = 32; 

   
		IntStream.range(0,rows).parallel().forEach(i0->{
			final int r =i0;
			IntStream.iterate(0, x->x<inner, x->x+=TILE).parallel().forEach(k0->{
                for (int j0 = 0; j0 < cols; j0 += TILE) {

                    int iMax = Math.min(r + TILE, rows);
                    int kMax = Math.min(k0 + TILE, inner);
                    int jMax = Math.min(j0 + TILE, cols);

                    for (int i = r; i < iMax; i++) {
                        for (int k = k0; k < kMax; k++) {
                            double aik = a[i * inner + k];
                            for (int j = j0; j < jMax; j++) {
                                res[i * cols + j] += aik * b[k * cols + j];
                            }
                        }
                    }

                }
            });
        });

        return new Matrix(res, rows, cols);
    }
    
    
    
@Benchmark
public Matrix multiplyForkJoin() {
	 int rows  = mat2d1.getRowSize();
     int cols  = mat2d2.getColumnSize();
     int inner = mat2d1.getColumnSize();

     double[] a   = a1d;
     double[] b   = b1d;
     double[] res = new double[rows * cols];

    ForkJoinPool.commonPool().invoke(new MultiplyTask(a, b, res, 0, rows, inner, cols));

    return new Matrix(res, rows, cols);
}

static class MultiplyTask extends RecursiveAction {
    private static final int THRESHOLD = 32; // split until chunks <= 64 rows

    final double[] a, b, res;
    final int startRow, endRow, inner, cols;

    MultiplyTask(double[] a, double[] b, double[] res,
                 int startRow, int endRow, int inner, int cols) {
        this.a = a; this.b = b; this.res = res;
        this.startRow = startRow; this.endRow = endRow;
        this.inner = inner; this.cols = cols;
    }

    @Override
    protected void compute() {
        if (endRow - startRow <= THRESHOLD) {
            // base case — do the actual work
            for (int i = startRow; i < endRow; i++) {
                for (int k = 0; k < inner; k++) {
                    DoubleVector va = DoubleVector.broadcast(SPECIES, a[i * inner + k]);
                    int j = 0;
                    for (; j < SPECIES.loopBound(cols); j += SPECIES.length()) {
                        DoubleVector vb = DoubleVector.fromArray(SPECIES, b, k * cols + j);
                        DoubleVector vr = DoubleVector.fromArray(SPECIES, res, i * cols + j);
                        va.fma(vb, vr).intoArray(res, i * cols + j);
                    }
                    for (; j < cols; j++) {
                        res[i * cols + j] += a[i * inner + k] * b[k * cols + j];
                    }
                }
            }
        } else {
            // split in half and fork
            int mid = (startRow + endRow) / 2;
            MultiplyTask left  = new MultiplyTask(a, b, res, startRow, mid, inner, cols);
            MultiplyTask right = new MultiplyTask(a, b, res, mid, endRow, inner, cols);
            left.fork();   // run left asynchronously
            right.compute(); // run right on current thread
            left.join();   // wait for left to finish
        }
    }
}

@Benchmark
public Matrix multiplyParallelStreamSIMD() {
	 int rows  = mat2d1.getRowSize();
     int cols  = mat2d2.getColumnSize();
     int inner = mat2d1.getColumnSize();

     double[] a   = a1d;
     double[] b   = b1d;
    double[] res = new double[rows * cols];

    IntStream.range(0, rows).parallel().forEach(i -> {
        for (int k = 0; k < inner; k++) {
            DoubleVector va = DoubleVector.broadcast(SPECIES, a[i * inner + k]);
            int j = 0;
            for (; j < SPECIES.loopBound(cols); j += SPECIES.length()) {
                DoubleVector vb = DoubleVector.fromArray(SPECIES, b, k * cols + j);
                DoubleVector vr = DoubleVector.fromArray(SPECIES, res, i * cols + j);
                va.fma(vb, vr).intoArray(res, i * cols + j);
            }
            for (; j < cols; j++) {
                res[i * cols + j] += a[i * inner + k] * b[k * cols + j];
            }
        }
    });

    return new Matrix(res, rows, cols);
}


	@Benchmark
	public DoubleMatrix blas() {
		DoubleMatrix c = a.mmul(b);
		
		return c;
	}
	
	@Benchmark
	public DoubleMatrix simpleblas() {
		 int n = mat2d1.getRowSize(), m = mat2d2.getColumnSize();
		
		DoubleMatrix c = new DoubleMatrix(n,m);
		SimpleBlas.gemm(1.0, a, b, 0.0, c);
		
		return c;
	}

}


