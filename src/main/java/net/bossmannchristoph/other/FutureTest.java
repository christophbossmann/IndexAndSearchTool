package net.bossmannchristoph.other;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class FutureTest {

	
	public static void main(String[] args) {
		ExecutorService executor = Executors.newSingleThreadExecutor();
		Future<?> future = executor.submit(new LongTestTask(8000));
		try {
		    future.get(10, TimeUnit.SECONDS);
		    System.out.println("Task finished successfully");
		} catch (TimeoutException te) {
			System.out.println("Time Over");
		    future.cancel(true);
		} catch (Exception e) {
		    // handle other exceptions
		} finally {
		    executor.shutdownNow();
		}
	}
	
	
}
