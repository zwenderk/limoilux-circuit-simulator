
package com.limoilux.circuitsimulator.scope;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;

import com.limoilux.circuitsimulator.circuit.Circuit;
import com.limoilux.circuitsimulator.circuit.CircuitElm;
import com.limoilux.circuitsimulator.core.CircuitSimulator;

public class ScopeManager
{
	private static final int SIZE = 20;
	public int scopeCount;
	public int scopeColCount[];

	public final Scope scopes[];

	private final ScopePane scopePane;
	private final Circuit circuit;

	public ScopeManager(Circuit circuit)
	{

		this.circuit = circuit;
		this.scopePane = new ScopePane();

		this.scopes = new Scope[20];
		this.scopeColCount = new int[ScopeManager.SIZE];
		this.scopeCount = 0;

	}

	public ScopePane getScopePane()
	{
		return this.scopePane;
	}

	public void drawScope(Graphics g)
	{
		// Dessinage des scopes
		for (int i = 0; i < this.scopeCount; i++)
		{
			this.scopes[i].draw(g);
			this.scopes[i].repaint();
		}
	}

	public void doTimeStep()
	{
		for (int i = 0; i < this.scopeCount; i++)
		{
			this.scopes[i].doTimeStep();
		}
	}

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
		Scope scope;
		for (int i = 0; i < this.scopeCount; i++)
		{
			scope = this.scopes[i];
			scope.position = i;
			scope.showMax = true;
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

	public void manageActionCommand(String ac, int menuScope)
	{
		if (ac.compareTo("remove") == 0)
		{
			this.scopes[menuScope].setElement(null);
		}
		if (ac.compareTo("speed2") == 0)
		{
			this.scopes[menuScope].speedUp();
		}
		if (ac.compareTo("speed1/2") == 0)
		{
			this.scopes[menuScope].slowDown();
		}
		if (ac.compareTo("scale") == 0)
		{
			this.scopes[menuScope].adjustScale(.5);
		}
		if (ac.compareTo("maxscale") == 0)
		{
			this.scopes[menuScope].adjustScale(1e-50);
		}
		if (ac.compareTo("stack") == 0)
		{
			this.stackScope(menuScope);
		}
		if (ac.compareTo("unstack") == 0)
		{
			this.unstackScope(menuScope);
		}
		if (ac.compareTo("selecty") == 0)
		{
			this.scopes[menuScope].selectY();
		}
		if (ac.compareTo("reset") == 0)
		{
			this.scopes[menuScope].resetGraph();
		}
	}

	private void removeUnused()
	{
		// check scopes to make sure the elements still exist, and remove
		// unused scopes/columns
		CircuitElm element;
		int pos = -1;
		for (int i = 0; i < this.scopeCount; i++)
		{
			element = this.scopes[i].element;
			if (this.circuit.locateElement(element) < 0)
			{
				this.scopes[i].setElement(null);
			}

			if (this.scopes[i].element == null)
			{

				for (int j = i; j != this.scopeCount; j++)
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

		while (this.scopeCount > 0 && this.scopes[this.scopeCount - 1].element == null)
		{
			this.scopeCount--;
		}
	}

	public void setupScopes(Dimension winSize)
	{
		this.removeUnused();

		int height = winSize.height - this.circuit.circuitArea.height;

		for (int i = 0; i < this.scopeCount; i++)
		{
			this.scopeColCount[i] = 0;
		}

		int pos = 0;
		for (int i = 0; i < this.scopeCount; i++)
		{
			pos = Math.max(this.scopes[i].position, pos);
			this.scopeColCount[this.scopes[i].position]++;
		}

		int colct = pos + 1;
		int iw = CircuitSimulator.INFO_WIDTH;

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

		for (int i = 0; i != this.scopeCount; i++)
		{
			Scope scope = this.scopes[i];
			if (scope.position > pos)
			{
				pos = scope.position;
				colh = height / this.scopeColCount[pos];
				row = 0;
				speed = scope.speed;
			}
			if (scope.speed != speed)
			{
				scope.speed = speed;
				scope.resetGraph();
			}

			Rectangle r = new Rectangle(pos * w, winSize.height - height + colh * row, w - marg, colh);
			row++;
			if (!r.equals(scope.rect))
			{
				scope.setRect(r);
			}
		}
	}

	public String createDump()
	{
		String dump = "";
		String tempDump = "";

		for (int i = 0; i < this.scopeCount; i++)
		{
			tempDump = this.scopes[i].dump();
			if (tempDump != null)
			{
				dump += tempDump + "\n";
			}
		}

		return dump;
	}

}
