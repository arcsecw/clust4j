package com.clust4j.utils.parallel;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

import com.clust4j.GlobalState;
import static com.clust4j.GlobalState.MAX_PARALLEL_CHUNK_SIZE;

abstract class DistributedVectorTask<T> extends RecursiveTask<T> {
	private static final long serialVersionUID = -7986981765361158408L;

    public final double[] array;
	public final int low;
    public final int high;
	
	DistributedVectorTask(double[] arr, int lo, int hi) {
		array = arr;
		low = lo;
		high = hi;
	}
	
	public static int getChunkSize() {
		return MAX_PARALLEL_CHUNK_SIZE;
	}
	
	public static ForkJoinPool getThreadPool() {
		return GlobalState.FJ_THREADPOOL;
	}
}