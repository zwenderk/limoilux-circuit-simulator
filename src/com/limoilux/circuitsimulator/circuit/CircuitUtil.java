
package com.limoilux.circuitsimulator.circuit;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import com.limoilux.circuit.techno.CircuitElm;
import com.limoilux.circuitsimulator.core.App;

public class CircuitUtil
{
	private CircuitUtil()
	{

	}
	

	public static CircuitElm constructElement(Class<?> classType, int x0, int y0)
	{
		// find element class
		Class<?> carr[] = null;
		// carr[0] = getClass();

		Object oarr[] = null;
		Constructor<?> constructor = null;
		CircuitElm elem = null;

		carr = new Class[2];
		carr[1] = int.class;
		carr[0] = int.class;

		try
		{
			App.printDebugMsg("constructing " + classType.toString());
			constructor = classType.getConstructor(carr);

			// invoke constructor with starting coordinates
			oarr = new Object[2];
			oarr[0] = new Integer(x0);
			oarr[1] = new Integer(y0);

			elem = (CircuitElm) constructor.newInstance(oarr);
		}
		catch (NoSuchMethodException e)
		{
			System.out.println("caught NoSuchMethodException " + classType);
		}
		catch (IllegalArgumentException e)
		{
			e.printStackTrace();
		}
		catch (InstantiationException e)
		{
			e.printStackTrace();
		}
		catch (IllegalAccessException e)
		{
			e.printStackTrace();
		}
		catch (InvocationTargetException e)
		{
			e.printStackTrace();
		}

		return elem;
	}
}
