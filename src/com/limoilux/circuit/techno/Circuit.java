
package com.limoilux.circuit.techno;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;

import com.limoilux.circuit.CapacitorElm;
import com.limoilux.circuit.CurrentElm;
import com.limoilux.circuit.GroundElm;
import com.limoilux.circuit.InductorElm;
import com.limoilux.circuit.RailElm;
import com.limoilux.circuit.VoltageElm;
import com.limoilux.circuit.WireElm;
import com.limoilux.circuit.core.CoreUtil;
import com.limoilux.circuit.ui.CircuitNode;
import com.limoilux.circuit.ui.CircuitNodeLink;
import com.limoilux.circuit.ui.RowInfo;

public class Circuit
{
	private final ArrayList<CircuitElm> elementList;
	private final ArrayList<CircuitNode> nodeList;

	private boolean circuitNonLinear;
	private boolean analyzeFlag;
	private boolean circuitNeedsMap;

	public int circuitBottom;
	public int circuitMatrixSize;

	public boolean converged;

	public CircuitElm[] voltageSources;
	public RowInfo[] circuitRowInfo;

	public final Matrix matrix;

	public Circuit()
	{
		this.elementList = new ArrayList<CircuitElm>();
		this.nodeList = new ArrayList<CircuitNode>();
		this.matrix = new Matrix();
	}

	public int getNodeCount()
	{
		return this.nodeList.size();
	}

	public int getMatrixFullSize()
	{
		return this.matrix.circuitMatrixFullSize;
	}

	public boolean isNonLinear()
	{
		return this.circuitNonLinear;
	}

	public boolean needAnalysis()
	{
		return this.analyzeFlag;
	}

	public boolean setNeedAnalysis(boolean analyzeFlag)
	{
		this.analyzeFlag = analyzeFlag;
		return true;
	}

	public int getElementCount()
	{
		return this.elementList.size();
	}

	public void removeElementAt(int index)
	{
		this.elementList.remove(index);
	}

	public void removeAllElements()
	{
		this.elementList.clear();
	}

	public void addElement(CircuitElm element)
	{
		this.elementList.add(element);
	}

	public CircuitNode getNodeAt(int index)
	{
		return this.nodeList.get(index);

	}

	/**
	 * @param index the index of the element
	 * @return a element at the specied index.
	 */
	public CircuitElm getElementAt(int index)
	{
		return this.elementList.get(index);
	}

	/**
	 * @param n the index of the element
	 * @return a element at the specied n.
	 * @deprecated use {@link #getElementAt(int)}
	 */
	@Deprecated
	public CircuitElm getElement(int n)
	{
		return this.getElementAt(n);
	}

	public int locateElement(CircuitElm elm)
	{
		for (int i = 0; i != this.elementList.size(); i++)
		{
			if (elm == this.elementList.get(i))
			{
				return i;
			}
		}
		return -1;
	}

	public void clearSelection()
	{
		for (int i = 0; i != this.elementList.size(); i++)
		{
			CircuitElm ce = this.getElementAt(i);
			ce.setSelected(false);
		}
	}

	public void doSelectAll()
	{
		for (int i = 0; i != this.elementList.size(); i++)
		{
			CircuitElm ce = this.getElementAt(i);
			ce.setSelected(true);
		}
	}

	public void removeZeroLengthElements()
	{
		for (int i = this.elementList.size() - 1; i >= 0; i--)
		{
			CircuitElm ce = this.getElementAt(i);
			if (ce.x == ce.x2 && ce.y == ce.y2)
			{
				this.elementList.remove(i);
				ce.delete();
			}
		}
	}

	public void calcCircuitBottom()
	{
		Rectangle rect = null;
		int bottom = 0;

		this.circuitBottom = 0;
		for (int i = 0; i != this.elementList.size(); i++)
		{
			rect = this.getElementAt(i).boundingBox;
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
		for (int i = 0; i < this.elementList.size(); i++)
		{
			dump += this.getElement(i).dump() + "\n";
		}

		return dump;
	}

	public void updateVoltageSource(int n1, int n2, int vs, double v)
	{
		int vn = this.getNodeCount() + vs;
		this.stampRightSide(vn, v);
	}

	// indicate that the value on the right side of row i changes in doStep()
	public void stampRightSide(int i)
	{
		// System.out.println("rschanges true " + (i-1));
		if (i > 0)
		{
			this.matrix.circuitRowInfo[i - 1].rsChanges = true;
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
				i = this.matrix.circuitRowInfo[i - 1].mapRow;
				// System.out.println("stamping " + i + " " + x);
			}
			else
			{
				i--;
			}
			this.matrix.circuitRightSide[i] += x;
		}
	}

	// indicate that the values on the left side of row i change in doStep()
	public void stampNonLinear(int i)
	{
		if (i > 0)
		{
			this.matrix.circuitRowInfo[i - 1].lsChanges = true;
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
				i = this.matrix.circuitRowInfo[i - 1].mapRow;
				RowInfo ri = this.matrix.circuitRowInfo[j - 1];
				if (ri.type == RowInfo.ROW_CONST)
				{
					// System.out.println("Stamping constant " + i + " " + j +
					// " " + x);
					this.matrix.circuitRightSide[i] -= x * ri.value;
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
			this.matrix.circuitMatrix[i][j] += x;
		}
	}

	// stamp a current source from n1 to n2 depending on current through vs
	public void stampCCCS(int n1, int n2, int vs, double gain)
	{
		int vn = this.getNodeCount() + vs;
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
		int vn = this.getNodeCount() + vs;
		this.stampMatrix(vn, n1, coef);
		this.stampMatrix(vn, n2, -coef);
	}

	// stamp independent voltage source #vs, from n1 to n2, amount v
	public void stampVoltageSource(int n1, int n2, int vs, double v)
	{
		int vn = this.getNodeCount() + vs;
		this.stampMatrix(vn, n1, -1);
		this.stampMatrix(vn, n2, 1);
		this.stampRightSide(vn, v);
		this.stampMatrix(n1, vn, 1);
		this.stampMatrix(n2, vn, -1);
	}

	// use this if the amount of voltage is going to be updated in doStep()
	public void stampVoltageSource(int n1, int n2, int vs)
	{
		int vn = this.getNodeCount() + vs;
		this.stampMatrix(vn, n1, -1);
		this.stampMatrix(vn, n2, 1);
		this.stampRightSide(vn);
		this.stampMatrix(n1, vn, 1);
		this.stampMatrix(n2, vn, -1);
	}

	public void analyzeCircuit() throws CircuitAnalysisException
	{

		System.out.println("Analysing");
		CircuitElm element;
		CircuitElm volt = null;
		CircuitNode circuitNode;
		PathInfoFinder pathInfoFinder;

		int j;
		int vscount = 0;
		boolean gotGround = false;
		boolean gotRail = false;

		this.calcCircuitBottom();

		if (this.elementList.isEmpty())
		{
			return;
		}

		this.nodeList.clear();


		// System.out.println("ac1");
		// look for voltage or ground element
		for (int i = 0; i != this.elementList.size(); i++)
		{
			element = this.getElementAt(i);
			
			if (element instanceof GroundElm)
			{
				gotGround = true;
				break;
			}
			
			if (element instanceof RailElm)
			{
				gotRail = true;
			}
			
			if (volt == null && element instanceof VoltageElm)
			{
				volt = element;
			}
		}

		// if no ground, and no rails, then the voltage elm's first terminal
		// is ground
		if (!gotGround && volt != null && !gotRail)
		{
			circuitNode = new CircuitNode(false);
			Point pt = volt.getPost(0);
			circuitNode.x = pt.x;
			circuitNode.y = pt.y;
			this.nodeList.add(circuitNode);
		}
		else
		{
			// otherwise allocate extra node for ground
			circuitNode = new CircuitNode(false);
			circuitNode.x = circuitNode.y = -1;

			this.nodeList.add(circuitNode);
		}
		// System.out.println("ac2");

		// allocate nodes and voltage sources
		for (int i = 0; i < this.elementList.size(); i++)
		{
			CircuitElm ce = this.getElementAt(i);
			int inodes = ce.getInternalNodeCount();
			int ivs = ce.getVoltageSourceCount();
			int posts = ce.getPostCount();

			// allocate a node for each post and match posts to nodes
			for (j = 0; j != posts; j++)
			{
				Point pt = ce.getPost(j);
				int k;
				for (k = 0; k != this.getNodeCount(); k++)
				{
					circuitNode = this.getNodeAt(k);
					if (pt.x == circuitNode.x && pt.y == circuitNode.y)
					{
						break;
					}
				}
				if (k == this.getNodeCount())
				{
					circuitNode = new CircuitNode(false);
					circuitNode.x = pt.x;
					circuitNode.y = pt.y;
					CircuitNodeLink cnl = new CircuitNodeLink(j, ce);
					circuitNode.addElement(cnl);
					ce.setNode(j, this.getNodeCount());
					this.nodeList.add(circuitNode);
				}
				else
				{
					CircuitNodeLink cnl = new CircuitNodeLink(j, ce);
					this.getNodeAt(k).addElement(cnl);
					ce.setNode(j, k);
					// if it's the ground node, make sure the node voltage is 0,
					// cause it may not get set later
					if (k == 0)
					{
						ce.setNodeVoltage(j, 0);
					}
				}
			}

			for (j = 0; j != inodes; j++)
			{
				circuitNode = new CircuitNode(true);
				circuitNode.y = -1;
				circuitNode.x = -1;

				CircuitNodeLink cnl = new CircuitNodeLink(j + posts, ce);

				circuitNode.addElement(cnl);
				ce.setNode(cnl.num, this.getNodeCount());
				this.nodeList.add(circuitNode);
			}
			vscount += ivs;
		}

		this.voltageSources = new CircuitElm[vscount];
		vscount = 0;
		this.circuitNonLinear = false;
		// System.out.println("ac3");

		// determine if circuit is nonlinear
		for (int i = 0; i != this.elementList.size(); i++)
		{
			CircuitElm ce = this.getElementAt(i);
			if (ce.nonLinear())
			{
				this.circuitNonLinear = true;
			}
			int ivs = ce.getVoltageSourceCount();
			for (j = 0; j != ivs; j++)
			{
				this.voltageSources[vscount] = ce;
				ce.setVoltageSource(j, vscount++);
			}
		}
		// voltageSourceCount = vscount;

		int matrixSize = this.getNodeCount() - 1 + vscount;

		this.matrix.init(matrixSize);

		// int vs = 0;

		this.circuitNeedsMap = false;

		// stamp linear circuit elements
		for (int i = 0; i < this.elementList.size(); i++)
		{
			element = this.getElementAt(i);
			element.stamp();
		}
		// System.out.println("ac4");

		// determine nodes that are unconnected
		boolean closure[] = new boolean[this.getNodeCount()];
		boolean changed = true;
		closure[0] = true;

		while (changed)
		{
			changed = false;
			for (int i = 0; i != this.elementList.size(); i++)
			{
				CircuitElm ce = this.getElementAt(i);
				// loop through all ce's nodes to see if they are connected
				// to other nodes not in closure
				for (j = 0; j < ce.getPostCount(); j++)
				{
					if (!closure[ce.getNode(j)])
					{
						if (ce.hasGroundConnection(j))
						{
							closure[ce.getNode(j)] = changed = true;
						}
						continue;
					}
					int k;
					for (k = 0; k != ce.getPostCount(); k++)
					{
						if (j == k)
						{
							continue;
						}
						int kn = ce.getNode(k);
						if (ce.getConnection(j, k) && !closure[kn])
						{
							closure[kn] = true;
							changed = true;
						}
					}
				}
			}

			if (changed)
			{
				continue;
			}

			// connect unconnected nodes
			for (int i = 0; i != this.getNodeCount(); i++)
			{
				if (!closure[i] && !this.getNodeAt(i).isInternal())
				{
					System.out.println("node " + i + " unconnected");
					this.stampResistor(0, i, 1e8);
					closure[i] = true;
					changed = true;
					break;
				}
			}
		}
		// System.out.println("ac5");

		for (int i = 0; i != this.elementList.size(); i++)
		{
			CircuitElm ce = this.getElementAt(i);
			// look for inductors with no current path
			if (ce instanceof InductorElm)
			{
				pathInfoFinder = new PathInfoFinder(PathInfoFinder.INDUCT, ce, ce.getNode(1), this);
				// first try findPath with maximum depth of 5, to avoid
				// slowdowns
				if (!pathInfoFinder.findPath(ce.getNode(0), 5) && !pathInfoFinder.findPath(ce.getNode(0)))
				{
					System.out.println(ce + " no path");
					ce.reset();
				}
			}
			// look for current sources with no current path
			if (ce instanceof CurrentElm)
			{
				pathInfoFinder = new PathInfoFinder(PathInfoFinder.INDUCT, ce, ce.getNode(1), this);
				if (!pathInfoFinder.findPath(ce.getNode(0)))
				{
					throw new CircuitAnalysisException("No path for current source!", ce);
				}
			}

			// look for voltage source loops
			if (ce instanceof VoltageElm && ce.getPostCount() == 2 || ce instanceof WireElm)
			{
				pathInfoFinder = new PathInfoFinder(PathInfoFinder.VOLTAGE, ce, ce.getNode(1), this);

				if (pathInfoFinder.findPath(ce.getNode(0)))
				{
					throw new CircuitAnalysisException("Voltage source/wire loop with no resistance!", ce);
				}
			}
			// look for shorted caps, or caps w/ voltage but no R
			if (ce instanceof CapacitorElm)
			{

				pathInfoFinder = new PathInfoFinder(PathInfoFinder.SHORT, ce, ce.getNode(1), this);
				if (pathInfoFinder.findPath(ce.getNode(0)))
				{
					System.out.println(ce + " shorted");
					ce.reset();
				}
				else
				{
					pathInfoFinder = new PathInfoFinder(PathInfoFinder.CAP_V, ce, ce.getNode(1), this);
					if (pathInfoFinder.findPath(ce.getNode(0)))
					{
						throw new CircuitAnalysisException("Capacitor loop with no resistance!", ce);
					}
				}
			}
		}
		// System.out.println("ac6");

		// simplify the matrix; this speeds things up quite a bit
		for (int i = 0; i != matrixSize; i++)
		{
			int qm = -1, qp = -1;
			double qv = 0;
			RowInfo re = this.matrix.circuitRowInfo[i];
			/*
			 * System.out.println("row " + i + " " + re.lsChanges + " " +
			 * re.rsChanges + " " + re.dropRow);
			 */
			if (re.lsChanges || re.dropRow || re.rsChanges)
			{
				continue;
			}
			double rsadd = 0;

			// look for rows that can be removed
			for (j = 0; j != matrixSize; j++)
			{
				double q = this.matrix.circuitMatrix[i][j];

				if (this.matrix.circuitRowInfo[j].type == RowInfo.ROW_CONST)
				{
					// keep a running total of const values that have been
					// removed already
					rsadd -= this.matrix.circuitRowInfo[j].value * q;
					continue;
				}

				if (q == 0)
				{
					continue;
				}

				if (qp == -1)
				{
					qp = j;
					qv = q;
					continue;
				}

				if (qm == -1 && q == -qv)
				{
					qm = j;
					continue;
				}
				break;
			}
			// System.out.println("line " + i + " " + qp + " " + qm + " " + j);
			/*
			 * if (qp != -1 && circuitRowInfo[qp].lsChanges) {
			 * System.out.println("lschanges"); continue; } if (qm != -1 &&
			 * circuitRowInfo[qm].lsChanges) { System.out.println("lschanges");
			 * continue; }
			 */
			if (j == matrixSize)
			{
				if (qp == -1)
				{
					throw new CircuitAnalysisException("Matrix error");
				}
				RowInfo elt = this.matrix.circuitRowInfo[qp];
				if (qm == -1)
				{
					// we found a row with only one nonzero entry; that value
					// is a constant
					int k;

					for (k = 0; elt.type == RowInfo.ROW_EQUAL && k < 100; k++)
					{
						// follow the chain
						/*
						 * System.out.println("following equal chain from " + i
						 * + " " + qp + " to " + elt.nodeEq);
						 */
						qp = elt.nodeEq;
						elt = this.matrix.circuitRowInfo[qp];
					}

					if (elt.type == RowInfo.ROW_EQUAL)
					{
						// break equal chains
						// System.out.println("Break equal chain");
						elt.type = RowInfo.ROW_NORMAL;
						continue;
					}

					if (elt.type != RowInfo.ROW_NORMAL)
					{
						System.out.println("type already " + elt.type + " for " + qp + "!");
						continue;
					}

					elt.type = RowInfo.ROW_CONST;
					elt.value = (this.matrix.circuitRightSide[i] + rsadd) / qv;
					this.matrix.circuitRowInfo[i].dropRow = true;
					// System.out.println(qp + " * " + qv + " = const " +
					// elt.value);
					i = -1; // start over from scratch
				}
				else if (this.matrix.circuitRightSide[i] + rsadd == 0)
				{
					// we found a row with only two nonzero entries, and one
					// is the negative of the other; the values are equal
					if (elt.type != RowInfo.ROW_NORMAL)
					{
						// System.out.println("swapping");
						int qq = qm;
						qm = qp;
						qp = qq;
						elt = this.matrix.circuitRowInfo[qp];
						if (elt.type != RowInfo.ROW_NORMAL)
						{
							// we should follow the chain here, but this
							// hardly ever happens so it's not worth worrying
							// about
							System.out.println("swap failed");
							continue;
						}
					}
					elt.type = RowInfo.ROW_EQUAL;
					elt.nodeEq = qm;
					this.matrix.circuitRowInfo[i].dropRow = true;
					// System.out.println(qp + " = " + qm);
				}
			}
		}
		// System.out.println("ac7");

		// find size of new matrix
		int nn = 0;

		for (int i = 0; i < matrixSize; i++)
		{
			RowInfo elt = this.matrix.circuitRowInfo[i];
			if (elt.type == RowInfo.ROW_NORMAL)
			{
				elt.mapCol = nn++;
				// System.out.println("col " + i + " maps to " + elt.mapCol);
				continue;
			}

			if (elt.type == RowInfo.ROW_EQUAL)
			{
				RowInfo e2 = null;
				// resolve chains of equality; 100 max steps to avoid loops
				for (j = 0; j != 100; j++)
				{
					e2 = this.matrix.circuitRowInfo[elt.nodeEq];

					if (e2.type != RowInfo.ROW_EQUAL)
					{
						break;
					}

					if (i == e2.nodeEq)
					{
						break;
					}
					elt.nodeEq = e2.nodeEq;
				}
			}
			if (elt.type == RowInfo.ROW_CONST)
			{
				elt.mapCol = -1;
			}
		}

		for (int i = 0; i != matrixSize; i++)
		{
			RowInfo elt = this.matrix.circuitRowInfo[i];
			if (elt.type == RowInfo.ROW_EQUAL)
			{
				RowInfo e2 = this.matrix.circuitRowInfo[elt.nodeEq];

				if (e2.type == RowInfo.ROW_CONST)
				{
					// if something is equal to a const, it's a const
					elt.type = e2.type;
					elt.value = e2.value;
					elt.mapCol = -1;
					// System.out.println(i + " = [late]const " + elt.value);
				}
				else
				{
					elt.mapCol = e2.mapCol;
					// System.out.println(i + " maps to: " + e2.mapCol);
				}
			}
		}
		// System.out.println("ac8");

		/*
		 * System.out.println("matrixSize = " + matrixSize);
		 * 
		 * for (j = 0; j != circuitMatrixSize; j++) { System.out.println(j +
		 * ": "); for (i = 0; i != circuitMatrixSize; i++)
		 * System.out.print(circuitMatrix[j][i] + " "); System.out.print("  " +
		 * circuitRightSide[j] + "\n"); } System.out.print("\n");
		 */

		// make the new, simplified matrix

		int newsize = nn;

		matrixSize = this.matrix.simplifyMatrix(newsize, matrixSize);

		this.matrix.recopyMatrixToOrginal(matrixSize);

		this.circuitNeedsMap = true;

		/*
		 * System.out.println("matrixSize = " + matrixSize + " " +
		 * circuitNonLinear); for (j = 0; j != circuitMatrixSize; j++) { for (i
		 * = 0; i != circuitMatrixSize; i++)
		 * System.out.print(circuitMatrix[j][i] + " "); System.out.print("  " +
		 * circuitRightSide[j] + "\n"); } System.out.print("\n");
		 */

		// if a matrix is linear, we can do the lu_factor here instead of
		// needing to do it every frame
		if (!this.circuitNonLinear && !this.matrix.doLuFactor())
		{
			throw new CircuitAnalysisException("Singular matrix!");
		}
	}

	@Deprecated
	public void recopyMatrix()
	{
		this.matrix.recopyMatrix();
	}

	@Deprecated
	public void clearMatrix()
	{
		this.matrix.clearMatrix();
	}

	@Deprecated
	public boolean matrixIsNull()
	{
		return this.matrix.matrixIsNull();
	}

	@Deprecated
	public boolean matrixIsInfiniteOrNAN()
	{
		return this.matrix.matrixIsInfiniteOrNAN();
	}

	@Deprecated
	public String matrixToString()
	{
		return this.matrix.matrixToString();
	}
}
