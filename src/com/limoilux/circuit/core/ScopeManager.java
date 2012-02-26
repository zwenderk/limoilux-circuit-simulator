
package com.limoilux.circuit.core;

import com.limoilux.circuit.ui.scope.Scope;

public class ScopeManager
{
	public int scopeCount;
	public Scope scopes[];
	public int scopeColCount[];

	public void stackAll()
	{
		for (int i = 0; i != this.scopeCount; i++)
		{
			this.scopes[i].position = 0;
			this.scopes[i].showMax = this.scopes[i].showMin = false;
		}
	}

	public void unstackAll()
	{
		for (int i = 0; i != this.scopeCount; i++)
		{
			this.scopes[i].position = i;
			this.scopes[i].showMax = true;
		}
	}

	public void unstackScope(int s)
	{
		if (s == 0)
		{
			if (this.scopeCount < 2)
			{
				return;
			}
			s = 1;
		}
		if (this.scopes[s].position != this.scopes[s - 1].position)
		{
			return;
		}
		for (; s < this.scopeCount; s++)
		{
			this.scopes[s].position++;
		}
	}

	public void stackScope(int s)
	{
		if (s == 0)
		{
			if (this.scopeCount < 2)
			{
				return;
			}
			s = 1;
		}

		if (this.scopes[s].position == this.scopes[s - 1].position)
		{
			return;
		}

		this.scopes[s].position = this.scopes[s - 1].position;
		for (s++; s < this.scopeCount; s++)
		{
			this.scopes[s].position--;
		}
	}
}
