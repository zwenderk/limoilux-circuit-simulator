
package com.limoilux.circuit.core;

import java.awt.Rectangle;
import java.util.Vector;

import com.limoilux.circuit.ui.CircuitNode;
import com.limoilux.circuit.ui.RowInfo;

public class Circuit
{
	public Vector<CircuitNode> nodeList;
	public Vector<CircuitElm> elmList;

	public CircuitElm voltageSources[];

	public int circuitBottom;
	public int circuitMatrixSize;
	public int circuitMatrixFullSize;
	
	public RowInfo circuitRowInfo[];
	public double circuitMatrix[][];
	public double circuitRightSide[];
	public boolean circuitNonLinear;
	public boolean analyzeFlag;
	public boolean circuitNeedsMap;

	public Circuit()
	{

	}

	public CircuitNode getCircuitNode(int n)
	{
		if (n >= this.nodeList.size())
		{
			return null;
		}

		return this.nodeList.elementAt(n);
	}

	public CircuitElm getElement(int n)
	{
		if (n >= this.elmList.size())
		{
			return null;
		}

		return this.elmList.elementAt(n);
	}

	public int locateElm(CircuitElm elm)
	{
		for (int i = 0; i != this.elmList.size(); i++)
		{
			if (elm == this.elmList.elementAt(i))
			{
				return i;
			}
		}
		return -1;
	}

	public void clearSelection()
	{
		for (int i = 0; i != this.elmList.size(); i++)
		{
			CircuitElm ce = this.getElement(i);
			ce.setSelected(false);
		}
	}

	public void doSelectAll()
	{
		for (int i = 0; i != this.elmList.size(); i++)
		{
			CircuitElm ce = this.getElement(i);
			ce.setSelected(true);
		}
	}

	public void removeZeroLengthElements()
	{
		for (int i = this.elmList.size() - 1; i >= 0; i--)
		{
			CircuitElm ce = this.getElement(i);
			if (ce.x == ce.x2 && ce.y == ce.y2)
			{
				this.elmList.removeElementAt(i);
				ce.delete();
			}
		}
	}

	public void calcCircuitBottom()
	{
		Rectangle rect = null;
		int bottom = 0;

		this.circuitBottom = 0;
		for (int i = 0; i != this.elmList.size(); i++)
		{
			rect = this.getElement(i).boundingBox;
			bottom = rect.height + rect.y;
			if (bottom > this.circuitBottom)
			{
				this.circuitBottom = bottom;
			}
		}
	}

	public String createDump()
	{
		String dump = "";
		for (int i = 0; i < this.elmList.size(); i++)
		{
			dump += this.getElement(i).dump() + "\n";
		}

		return dump;
	}

	// indicate that the value on the right side of row i changes in doStep()
	public void stampRightSide(int i)
	{
		// System.out.println("rschanges true " + (i-1));
		if (i > 0)
		{
			this.circuitRowInfo[i - 1].rsChanges = true;
		}
	}
	
	// indicate that the values on the left side of row i change in doStep()
	public void stampNonLinear(int i)
	{
		if (i > 0)
		{
			this.circuitRowInfo[i - 1].lsChanges = true;
		}
	}
}
