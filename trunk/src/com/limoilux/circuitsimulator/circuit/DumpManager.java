
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
}
