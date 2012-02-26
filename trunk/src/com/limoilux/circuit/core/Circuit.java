
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

	public String stopMessage;
	public RowInfo circuitRowInfo[];
	public int circuitPermute[];
	public double origRightSide[];
	public double circuitMatrix[][];
	public double origMatrix[][];
	public double circuitRightSide[];
	public boolean circuitNonLinear;
	public boolean analyzeFlag;
	public boolean converged;
	public boolean circuitNeedsMap;
	public CircuitElm stopElm;

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
	
	public void updateVoltageSource(int n1, int n2, int vs, double v)
	{
		int vn = this.nodeList.size() + vs;
		this.stampRightSide(vn, v);
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

	// stamp value x on the right side of row i, representing an
	// independent current source flowing into node i
	public void stampRightSide(int i, double x)
	{
		if (i > 0)
		{
			if (this.circuitNeedsMap)
			{
				i = this.circuitRowInfo[i - 1].mapRow;
				// System.out.println("stamping " + i + " " + x);
			}
			else
			{
				i--;
			}
			this.circuitRightSide[i] += x;
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

	// stamp value x in row i, column j, meaning that a voltage change
	// of dv in node j will increase the current into node i by x dv.
	// (Unless i or j is a voltage source node.)
	public void stampMatrix(int i, int j, double x)
	{
		if (i > 0 && j > 0)
		{
			if (this.circuitNeedsMap)
			{
				i = this.circuitRowInfo[i - 1].mapRow;
				RowInfo ri = this.circuitRowInfo[j - 1];
				if (ri.type == RowInfo.ROW_CONST)
				{
					// System.out.println("Stamping constant " + i + " " + j +
					// " " + x);
					this.circuitRightSide[i] -= x * ri.value;
					return;
				}
				j = ri.mapCol;
				// System.out.println("stamping " + i + " " + j + " " + x);
			}
			else
			{
				i--;
				j--;
			}
			this.circuitMatrix[i][j] += x;
		}
	}
	
	// stamp a current source from n1 to n2 depending on current through vs
	public void stampCCCS(int n1, int n2, int vs, double gain)
	{
		int vn = this.nodeList.size() + vs;
		this.stampMatrix(n1, vn, gain);
		this.stampMatrix(n2, vn, -gain);
	}
	
	public void stampConductance(int n1, int n2, double r0)
	{
		this.stampMatrix(n1, n1, r0);
		this.stampMatrix(n2, n2, r0);
		this.stampMatrix(n1, n2, -r0);
		this.stampMatrix(n2, n1, -r0);
	}

	// current from cn1 to cn2 is equal to voltage from vn1 to 2, divided by g
	public void stampVCCurrentSource(int cn1, int cn2, int vn1, int vn2, double g)
	{
		this.stampMatrix(cn1, vn1, g);
		this.stampMatrix(cn2, vn2, g);
		this.stampMatrix(cn1, vn2, -g);
		this.stampMatrix(cn2, vn1, -g);
	}

	public void stampCurrentSource(int n1, int n2, double i)
	{
		this.stampRightSide(n1, -i);
		this.stampRightSide(n2, i);
	}
	
	public void stampResistor(int n1, int n2, double r)
	{
		double r0 = 1 / r;
		if (Double.isNaN(r0) || Double.isInfinite(r0))
		{
			System.out.print("bad resistance " + r + " " + r0 + "\n");
			int a = 0;
			a /= a;
		}
		this.stampMatrix(n1, n1, r0);
		this.stampMatrix(n2, n2, r0);
		this.stampMatrix(n1, n2, -r0);
		this.stampMatrix(n2, n1, -r0);
	}
	

	// control voltage source vs with voltage from n1 to n2 (must
	// also call stampVoltageSource())
	public void stampVCVS(int n1, int n2, double coef, int vs)
	{
		int vn = this.nodeList.size() + vs;
		this.stampMatrix(vn, n1, coef);
		this.stampMatrix(vn, n2, -coef);
	}

	// stamp independent voltage source #vs, from n1 to n2, amount v
	public void stampVoltageSource(int n1, int n2, int vs, double v)
	{
		int vn = this.nodeList.size() + vs;
		this.stampMatrix(vn, n1, -1);
		this.stampMatrix(vn, n2, 1);
		this.stampRightSide(vn, v);
		this.stampMatrix(n1, vn, 1);
		this.stampMatrix(n2, vn, -1);
	}

	// use this if the amount of voltage is going to be updated in doStep()
	public void stampVoltageSource(int n1, int n2, int vs)
	{
		int vn = this.nodeList.size() + vs;
		this.stampMatrix(vn, n1, -1);
		this.stampMatrix(vn, n2, 1);
		this.stampRightSide(vn);
		this.stampMatrix(n1, vn, 1);
		this.stampMatrix(n2, vn, -1);
	}
}
