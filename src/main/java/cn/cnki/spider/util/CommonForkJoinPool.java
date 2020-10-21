package cn.cnki.spider.util;

import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.ForkJoinWorkerThread;

public class CommonForkJoinPool {

	protected ForkJoinPool pool;

	public CommonForkJoinPool(int parallelism, String threadPrefix) {

		final ForkJoinPool.ForkJoinWorkerThreadFactory factory = po -> {
			final ForkJoinWorkerThread worker = ForkJoinPool.defaultForkJoinWorkerThreadFactory.newThread(po);
			worker.setName(threadPrefix + worker.getPoolIndex());
			return worker;
		};

		pool = new ForkJoinPool(parallelism, factory, null, false);
	}

	public ForkJoinTask<?> submit(Runnable task) {
		return pool.submit(task);
	}

	public <T> ForkJoinTask<T> submit(Callable<T> task) {
		return pool.submit(task);
	}

	public <T> ForkJoinTask<T> submit(ForkJoinTask<T> task) {
		return pool.submit(task);
	}

	public ForkJoinPool getPool() {
		return pool;
	}

}
