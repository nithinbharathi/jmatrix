package operations;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.Future;
import java.util.stream.IntStream;

import jmatrix.Matrix;

public final class SubtractUtils {
	private SubtractUtils(){
		throw new UnsupportedOperationException("utility class");
	}
	
    public static Matrix subtract(Matrix mat, double val){
		int n = mat.temp.length;

		int cores = Runtime.getRuntime().availableProcessors();
		//int chunkSize = (n + cores - 1)/cores;
		//int numChunks = (n + chunkSize - 1)/chunkSize;
		List<ForkJoinTask<?>>tasks = new ArrayList<>(cores);

		int cacheLineBytes = 64;           // typical CPU
		int elementBytes = 8;              // double
		int cacheLineElements = cacheLineBytes / elementBytes;
				int rawChunk = n / cores;
		int chunkSize = (rawChunk / cacheLineElements) * cacheLineElements;

	/* 	for(int i = 0;i<numChunks;i++){
			int start = i*chunkSize;
			int end = Math.min(start+chunkSize,n);
			tasks.add(ForkJoinPool.commonPool().submit(()->{
				for(int j = start;j<end;j++){
					mat.temp[j] -= val;
				}
			}));

		}*/
		if (chunkSize < cacheLineElements) chunkSize = cacheLineElements;
		int start = 0, end = n;
		for(int i = 0;i<cores && start < end;i++){
			start = i*chunkSize;
			end = Math.min(start+chunkSize,n);

			final int s = start;
			final int e = end;
			tasks.add(ForkJoinPool.commonPool().submit(()->{
				for(int j = s;j<e;j++){
					mat.temp[j] -= val;
				}
			}));

		}

		for(ForkJoinTask<?>task:tasks)task.join();

		return mat;
	}
}
