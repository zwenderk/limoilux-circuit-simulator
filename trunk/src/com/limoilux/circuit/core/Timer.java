
package com.limoilux.circuit.core;

public class Timer
{
	private static final int MAX_FPS = 50;
	public double time;

	public long lastTime = 0;

	public double timeStep;

	private long lastFrameTime;
	public long lastIterTime;
	public long secTime = 0;

	public Timer()
	{

	}

	public long calculateDelay()
	{
		// Limit to 50 fps (thanks to J�rgen Kl�tzer for this)
		return 1000 / Timer.MAX_FPS - (System.currentTimeMillis() - this.lastFrameTime);
	}

	public void nextCycle()
	{
		this.lastFrameTime = this.lastTime;
	}

	public void doTimeStep()
	{
		this.time += this.timeStep;
	}

	public long getLastFrameTime()
	{
		return this.lastFrameTime;
	}
}
