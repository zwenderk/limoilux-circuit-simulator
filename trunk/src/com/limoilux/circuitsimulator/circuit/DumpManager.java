
package com.limoilux.circuitsimulator.circuit;

import com.limoilux.circuitsimulator.scope.Scope;

public class DumpManager
{
	public final Class<?> dumpTypes[];

	public DumpManager()
	{

		// Init the dumpTypes
		this.dumpTypes = new Class[300];

		// these characters are reserved
		this.dumpTypes['o'] = Scope.class;
		this.dumpTypes['h'] = Scope.class;
		this.dumpTypes['$'] = Scope.class;
		this.dumpTypes['%'] = Scope.class;
		this.dumpTypes['?'] = Scope.class;
		this.dumpTypes['B'] = Scope.class;

	}
	

	public void register(CircuitElm element)
	{
		Class<?> dumpClass = null;
		int elementId = element.getElementId();

		if (elementId == 0)
		{
			System.out.println("no dump type: ");
			return;
		}

		dumpClass = element.getDumpClass();
		if (this.dumpTypes[elementId] == dumpClass)
		{
			return;
		}

		if (this.dumpTypes[elementId] != null)
		{
			System.out.println("dump type conflict: " + this.dumpTypes[elementId]);
			return;
		}

		this.dumpTypes[elementId] = dumpClass;
	}

}
