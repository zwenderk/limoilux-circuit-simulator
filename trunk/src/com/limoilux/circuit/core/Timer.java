
package com.limoilux.circuit.core;

public class Timer
{
	public double time;

	public long lastTime = 0;

	public double timeStep;

	private long lastFrameTime;
	public long lastIterTime;
	public long secTime = 0;
	
	public long calculateDelay()
	{
		// Limit to 50 fps (thanks to J�rgen Kl�tzer for this)
		return 1000 / 50 - (System.currentTimeMillis() - this.lastFrameTime);
	}
	
	public void nextCycle()
	{
		this.lastFrameTime = this.lastTime;
	}
	
	public long getLastFrameTime()
	{
		return this.lastFrameTime;
	}
}
