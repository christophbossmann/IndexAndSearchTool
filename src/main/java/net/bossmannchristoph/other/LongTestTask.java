package net.bossmannchristoph.other;

import net.bossmannchristoph.indexerAndSearchTool.TechnicalException;

public class LongTestTask implements Runnable {

	public LongTestTask(long timeInMillis) {
		this.timeInMillis = timeInMillis;
	}
	
	long timeInMillis;
	
	@Override
	public void run() {
		try {
			Thread.sleep(timeInMillis);
		} catch (Exception e) {
			throw new TechnicalException(e);
		}
	}
}
