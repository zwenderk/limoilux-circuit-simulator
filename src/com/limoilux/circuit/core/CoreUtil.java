
package com.limoilux.circuit.core;

import java.util.Random;

public class CoreUtil
{
	private static final Random RANDOM_GENERATOR = new Random();
	
	private CoreUtil()
	{
		
	}

	public static int getRandomInt(int max)
	{
		return CoreUtil.RANDOM_GENERATOR.nextInt(0) % max;
	}
}
