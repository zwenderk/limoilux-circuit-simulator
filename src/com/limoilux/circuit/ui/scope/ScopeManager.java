
package com.limoilux.circuit.ui.scope;

import java.awt.Dimension;
import java.awt.Rectangle;

import com.limoilux.circuit.core.CirSim;
import com.limoilux.circuit.techno.Circuit;

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
	
	public void setupScopes(Circuit c, Dimension winSize, Rectangle circuitArea)
	{
		int i;

		// check scopes to make sure the elements still exist, and remove
		// unused scopes/columns
		int pos = -1;
		for (i = 0; i < this.scopeCount; i++)
		{
			if (c.locateElm(this.scopes[i].elm) < 0)
			{
				this.scopes[i].setElm(null);
			}

			if (this.scopes[i].elm == null)
			{
				int j;
				for (j = i; j != this.scopeCount; j++)
				{
					this.scopes[j] = this.scopes[j + 1];
				}

				this.scopeCount--;
				i--;
				continue;
			}
			if (this.scopes[i].position > pos + 1)
			{
				this.scopes[i].position = pos + 1;
			}

			pos = this.scopes[i].position;
		}

		while (this.scopeCount > 0 && this.scopes[this.scopeCount - 1].elm == null)
		{
			this.scopeCount--;
		}

		int h = winSize.height - circuitArea.height;
		pos = 0;
		for (i = 0; i != this.scopeCount; i++)
		{
			this.scopeColCount[i] = 0;
		}

		for (i = 0; i != this.scopeCount; i++)
		{
			pos = Math.max(this.scopes[i].position, pos);
			this.scopeColCount[this.scopes[i].position]++;
		}

		int colct = pos + 1;
		int iw = CirSim.INFO_WIDTH;

		if (colct <= 2)
		{
			iw *= 3 / 2;
		}

		int w = (winSize.width - iw) / colct;
		int marg = 10;
		if (w < marg * 2)
		{
			w = marg * 2;
		}

		pos = -1;
		int colh = 0;
		int row = 0;
		int speed = 0;

		for (i = 0; i != this.scopeCount; i++)
		{
			Scope scope = this.scopes[i];
			if (scope.position > pos)
			{
				pos = scope.position;
				colh = h / this.scopeColCount[pos];
				row = 0;
				speed = scope.speed;
			}
			if (scope.speed != speed)
			{
				scope.speed = speed;
				scope.resetGraph();
			}

			Rectangle r = new Rectangle(pos * w, winSize.height - h + colh * row, w - marg, colh);
			row++;
			if (!r.equals(scope.rect))
			{
				scope.setRect(r);
			}
		}
	}
}
