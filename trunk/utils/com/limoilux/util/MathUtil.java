
package com.limoilux.util;

public class MathUtil
{
	private MathUtil()
	{

	}


	public static int sign(int nb)
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
