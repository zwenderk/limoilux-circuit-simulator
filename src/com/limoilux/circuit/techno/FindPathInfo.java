
package com.limoilux.circuit.techno;

import com.limoilux.circuit.CapacitorElm;
import com.limoilux.circuit.CurrentElm;
import com.limoilux.circuit.InductorElm;
import com.limoilux.circuit.VoltageElm;

public class FindPathInfo
{
	public static final int INDUCT = 1;
	public static final int VOLTAGE = 2;
	public static final int SHORT = 3;
	public static final int CAP_V = 4;
	public boolean used[];
	public int dest;
	public CircuitElm firstElm;
	public int type;
	public Circuit circuit;

	public FindPathInfo(int t, CircuitElm e, int d, Circuit circuit)
	{
		this.circuit = circuit;
		this.dest = d;
		this.type = t;
		this.firstElm = e;
		this.used = new boolean[this.circuit.nodeList.size()];
	}

	public boolean findPath(int n1)
	{
		return this.findPath(n1, -1);
	}

	public boolean findPath(int n1, int depth)
	{
		if (n1 == this.dest)
		{
			return true;
		}

		if (depth-- == 0)
		{
			return false;

		}

		if (this.used[n1])
		{
			return false;
		}

		this.used[n1] = true;

		for (int i = 0; i != this.circuit.getElementCount(); i++)
		{
			CircuitElm circuitElement = this.circuit.getElement(i);

			if (circuitElement == this.firstElm)
			{
				continue;
			}
			if (this.type == FindPathInfo.INDUCT)
			{
				if (circuitElement instanceof CurrentElm)
				{
					continue;
				}
			}
			if (this.type == FindPathInfo.VOLTAGE)
			{
				if (!(circuitElement.isWire() || circuitElement instanceof VoltageElm))
				{
					continue;
				}
			}
			if (this.type == FindPathInfo.SHORT && !circuitElement.isWire())
			{
				continue;
			}

			if (this.type == FindPathInfo.CAP_V)
			{
				if (!(circuitElement.isWire() || circuitElement instanceof CapacitorElm || circuitElement instanceof VoltageElm))
				{
					continue;
				}
			}

			if (n1 == 0)
			{
				// look for posts which have a ground connection;
				// our path can go through ground
				int j;
				for (j = 0; j != circuitElement.getPostCount(); j++)
				{
					if (circuitElement.hasGroundConnection(j) && this.findPath(circuitElement.getNode(j), depth))
					{
						this.used[n1] = false;
						return true;
					}
				}
			}

			int j;

			for (j = 0; j != circuitElement.getPostCount(); j++)
			{
				// System.out.println(ce + " " + ce.getNode(j));
				if (circuitElement.getNode(j) == n1)
				{
					break;
				}
			}

			if (j == circuitElement.getPostCount())
			{
				continue;
			}

			if (circuitElement.hasGroundConnection(j) && this.findPath(0, depth))
			{
				// System.out.println(ce + " has ground");
				this.used[n1] = false;
				return true;
			}

			if (this.type == FindPathInfo.INDUCT && circuitElement instanceof InductorElm)
			{
				double current = circuitElement.getCurrent();
				if (j == 0)
				{
					current = -current;
				}
				// System.out.println("matching " + c + " to " +
				// firstElm.getCurrent());
				// System.out.println(ce + " " + firstElm);
				if (Math.abs(current - this.firstElm.getCurrent()) > 1e-10)
				{
					continue;
				}
			}

			for (int k = 0; k != circuitElement.getPostCount(); k++)
			{
				if (j == k)
				{
					continue;
				}
				// System.out.println(ce + " " + ce.getNode(j) + "-" +
				// ce.getNode(k));
				if (circuitElement.getConnection(j, k) && this.findPath(circuitElement.getNode(k), depth))
				{
					// System.out.println("got findpath " + n1);
					this.used[n1] = false;
					return true;
				}
				// System.out.println("back on findpath " + n1);
			}
		}
		this.used[n1] = false;
		// System.out.println(n1 + " failed");
		return false;
	}
}
