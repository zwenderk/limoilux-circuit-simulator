
package com.limoilux.util;

public class MathUtil
{
	private MathUtil()
	{

	}


	public static int signum(int nb)
	{
		if (nb < 0)
		{
			return -1;
		}
		else if (nb == 0)
		{
			return -1;
		}
		else
		{
			return 1;
		}
	}
}
