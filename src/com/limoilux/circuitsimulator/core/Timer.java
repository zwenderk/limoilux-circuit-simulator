
package com.limoilux.circuitsimulator.core;

public class Timer
{
	private static final int MAX_FPS = 50;
	public double time;

	public long lastTime = 0;

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

	public void reset()
	{
		this.lastTime = 0;
	}

	public void doTimeStep()
	{
		this.time += Configs.TIME_STEP;
	}

	public long getLastFrameTime()
	{
		return this.lastFrameTime;
	}
}
