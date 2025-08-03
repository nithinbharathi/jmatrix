package operations;

import jmatrix.Matrix;

public final class AggregateUtils {
    private AggregateUtils(){
        throw new UnsupportedOperationException("Utility Class");
    }
    
    public static double max(Matrix mat){
       double max = Double.MIN_VALUE;
        for(int i = 0;i<mat.getRowSize();i++){
            for(int j =0;j<mat.getColumnSize();j++){
                max = Math.max(mat.get(i,j), max);
            }
        }

       return max;
    }
}
